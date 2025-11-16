package com.shop.fperfume.controller.client;

import com.shop.fperfume.config.VNPayConfig;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.service.client.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Sửa lỗi import không dùng đến (URLDecoder, StandardCharsets)
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private VnPayService vnPayService; // Dùng để gọi hàm hmacSHA512

    /**
     * Đây là URL mà VNPay sẽ gọi về sau khi khách thanh toán.
     * Đường dẫn: /payment/vnpay/return
     */
    @GetMapping("/vnpay/return") // <<< SỬA 1: Khớp với đường dẫn VNPay gọi về
    public String vnpayReturn(HttpServletRequest request, Model model) {

        // 1. Lấy tất cả tham số VNPay trả về
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        // 2. Lấy chữ ký
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // 3. Sắp xếp và tạo lại chuỗi hash
        java.util.List<String> fieldNames = new java.util.ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName); // fieldValue đã được request.getParameter() tự động decode
            if (fieldValue != null && !fieldValue.isEmpty()) {

                // === SỬA 2: XÓA BỎ URLDECODER.DECODE ===
                // Không cần decode lại, dùng trực tiếp fieldValue
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                // === KẾT THÚC SỬA 2 ===

                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String vnp_HashSecret = vnPayConfig.getHashSecret();
        String secureHash = vnPayService.hmacSHA512(vnp_HashSecret, hashData.toString());

        // 4. Kiểm tra chữ ký và kết quả
        if (vnp_SecureHash.equals(secureHash)) {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Đây là Mã Hóa Đơn

            if ("00".equals(vnp_ResponseCode)) {
                // === THANH TOÁN THÀNH CÔNG ===
                HoaDon hoaDon = hoaDonRepository.findByMa(vnp_TxnRef).orElse(null);

                if (hoaDon != null) {
                    // Chỉ cập nhật nếu đơn hàng đang "CHỜ THANH TOÁN"
                    if ("ĐANG CHỜ THANH TOÁN".equals(hoaDon.getTrangThai())) {
                        hoaDon.setTrangThai("ĐÃ XÁC NHẬN"); // Cập nhật trạng thái
                        hoaDon.setNgayThanhToan(java.time.LocalDateTime.now());
                        hoaDonRepository.save(hoaDon);
                    }
                    // Chuyển hướng về trang thành công
                    return "redirect:/order/success/" + hoaDon.getId();
                } else {
                    // Lỗi: Không tìm thấy đơn hàng
                    model.addAttribute("errorMessage", "Lỗi: Không tìm thấy đơn hàng: " + vnp_TxnRef);
                    return "client/payment_failure";
                }
            } else {
                // === THANH TOÁN THẤT BẠI (Do người dùng hủy, hết tiền, v.v.) ===
                // Lấy thông báo lỗi từ VNPay
                String vnp_Message = request.getParameter("vnp_Message");
                if (vnp_Message != null) {
                    model.addAttribute("errorMessage", "Thanh toán VNPay không thành công: " + vnp_Message);
                } else {
                    model.addAttribute("errorMessage", "Thanh toán VNPay không thành công. Mã lỗi: " + vnp_ResponseCode);
                }
                return "client/payment_failure";
            }
        } else {
            // === SAI CHỮ KÝ (CÓ DẤU HIỆU GIẢ MẠO) ===
            model.addAttribute("errorMessage", "Thanh toán thất bại: Sai chữ ký VNPay!");
            return "client/payment_failure";
        }
    }
}