package com.shop.fperfume.controller.client;

import com.shop.fperfume.config.VNPayConfig;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.service.client.VnPayService;
import com.shop.fperfume.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired private VNPayConfig vnPayConfig;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private VnPayService vnPayService;
    @Autowired private GioHangClientService gioHangClientService;

    private static final String SESSION_CART_KEY = "GUEST_CART";

    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model, HttpSession session) {
        // 1. Lấy tham số từ VNPay
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        // 2. Xử lý chữ ký bảo mật (Hash)
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) fields.remove("vnp_SecureHashType");
        if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        try {
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    if (itr.hasNext()) hashData.append('&');
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String vnp_HashSecret = vnPayConfig.getHashSecret();
        String secureHash = vnPayService.hmacSHA512(vnp_HashSecret, hashData.toString());

        // 3. Kiểm tra kết quả
        if (vnp_SecureHash.equals(secureHash)) {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");

            if ("00".equals(vnp_ResponseCode)) {
                // === THANH TOÁN THÀNH CÔNG ===

                // A. Tìm đơn hàng trong Database
                HoaDon hoaDon = hoaDonRepository.findByMa(vnp_TxnRef).orElse(null);

                if (hoaDon != null) {
                    // B. Kiểm tra trạng thái hiện tại để tránh cập nhật trùng lặp
                    if ("DANG_CHO_THANH_TOAN".equals(hoaDon.getTrangThai())) {

                        // --- QUAN TRỌNG: CẬP NHẬT DATABASE TẠI ĐÂY ---
                        hoaDon.setTrangThai("HOAN_THANH"); // Cập nhật trạng thái
                        hoaDon.setNgayThanhToan(java.time.LocalDateTime.now()); // Cập nhật ngày giờ

                        hoaDonRepository.save(hoaDon); // <--- LỆNH LƯU QUAN TRỌNG NHẤT
                        System.out.println("Đã cập nhật đơn hàng " + vnp_TxnRef + " thành HOÀN THÀNH");

                        // C. Xóa giỏ hàng (Code cũ của bạn)
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                            gioHangClientService.clearCart(userDetails.getUser());
                        }
                        session.removeAttribute("GUEST_CART");
                    }

                    // Chuyển hướng sang trang thành công
                    return "redirect:/order/success/" + hoaDon.getId();
                } else {
                    model.addAttribute("errorMessage", "Lỗi: Không tìm thấy đơn hàng có mã " + vnp_TxnRef);
                    return "client/payment_failure";
                }
            } else {
                // Thanh toán thất bại/Hủy
                model.addAttribute("errorMessage", "Giao dịch thất bại. Mã lỗi: " + vnp_ResponseCode);
                return "client/payment_failure";
            }
        } else {
            model.addAttribute("errorMessage", "Lỗi bảo mật: Chữ ký không hợp lệ!");
            return "client/payment_failure";
        }
    }
}