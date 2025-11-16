package com.shop.fperfume.controller.client;

import com.shop.fperfume.config.VNPayConfig;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.service.client.VnPayService; // Import service để dùng hàm hash
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // <<< SỬA: Dùng @Controller
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller // <<< SỬA: Dùng @Controller để trả về trang HTML/redirect
@RequestMapping("/api/payment") // (Bỏ / cuối)
public class PaymentController {

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private VnPayService vnPayService; // Dùng để gọi hàm hmacSHA512

    // Đây là URL bạn đã khai báo trong file VNPayConfig (vnp.returnUrl)
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {

        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Sắp xếp theo alphabet
        java.util.List<String> fieldNames = new java.util.ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    // Quan trọng: Phải decode giá trị trước khi hash
                    hashData.append(URLDecoder.decode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) { e.printStackTrace(); }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String vnp_HashSecret = vnPayConfig.getHashSecret(); //
        String secureHash = vnPayService.hmacSHA512(vnp_HashSecret, hashData.toString());

        // Bắt đầu kiểm tra kết quả
        if (vnp_SecureHash.equals(secureHash)) {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Đây là Mã Hóa Đơn

            if ("00".equals(vnp_ResponseCode)) {
                // === THÀNH CÔNG ===
                HoaDon hoaDon = hoaDonRepository.findByMa(vnp_TxnRef).orElse(null);

                if (hoaDon != null) {
                    // Chỉ cập nhật nếu đơn hàng đang "CHỜ THANH TOÁN"
                    if ("ĐANG CHỜ THANH TOÁN".equals(hoaDon.getTrangThai())) {

                        // SỬA TRẠNG THÁI (THEO YÊU CẦU CỦA BẠN)
                        hoaDon.setTrangThai("ĐÃ XÁC NHẬN");
                        hoaDon.setNgayThanhToan(java.time.LocalDateTime.now());
                        hoaDonRepository.save(hoaDon);
                    }
                    // Chuyển hướng về trang thành công
                    return "redirect:/order/success/" + hoaDon.getId();
                } else {
                    model.addAttribute("errorMessage", "Không tìm thấy đơn hàng: " + vnp_TxnRef);
                    return "client/payment_failure"; // (Bạn cần tạo trang này)
                }
            } else {
                // === THẤT BẠI (Do người dùng hủy, hết tiền, v.v.) ===
                model.addAttribute("errorMessage", "Thanh toán VNPay không thành công. Mã lỗi: " + vnp_ResponseCode);
                return "client/payment_failure"; // (Bạn cần tạo trang này)
            }
        } else {
            // === SAI CHỮ KÝ ===
            model.addAttribute("errorMessage", "Sai chữ ký VNPay!");
            return "client/payment_failure"; // (Bạn cần tạo trang này)
        }
    }
}