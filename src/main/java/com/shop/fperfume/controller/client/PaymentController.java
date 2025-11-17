package com.shop.fperfume.controller.client;

import com.shop.fperfume.config.VNPayConfig;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung; // Cần thêm
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.service.client.GioHangClientService; // Cần thêm
import com.shop.fperfume.service.client.VnPayService;
import com.shop.fperfume.security.CustomUserDetails; // Cần thêm

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession; // Cần thêm
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Cần thêm
import org.springframework.security.core.context.SecurityContextHolder; // Cần thêm
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

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private GioHangClientService gioHangClientService; // Inject service để xóa giỏ hàng DB

    private static final String SESSION_CART_KEY = "GUEST_CART"; // Key session giỏ hàng

    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model, HttpSession session) { // Thêm HttpSession

        // ... (Đoạn code lấy tham số và tính toán hash giữ nguyên như cũ) ...
        // 1. Lấy tham số
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
        if (fields.containsKey("vnp_SecureHashType")) fields.remove("vnp_SecureHashType");
        if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");

        // 3. Tạo hash
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
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String vnp_HashSecret = vnPayConfig.getHashSecret();
        String secureHash = vnPayService.hmacSHA512(vnp_HashSecret, hashData.toString());

        // 4. Kiểm tra chữ ký
        if (vnp_SecureHash.equals(secureHash)) {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");

            if ("00".equals(vnp_ResponseCode)) {
                // === GIAO DỊCH THÀNH CÔNG ===
                HoaDon hoaDon = hoaDonRepository.findByMa(vnp_TxnRef).orElse(null);

                if (hoaDon != null) {
                    if ("ĐANG CHỜ THANH TOÁN".equals(hoaDon.getTrangThai())) {
                        hoaDon.setTrangThai("ĐÃ XÁC NHẬN");
                        hoaDon.setNgayThanhToan(java.time.LocalDateTime.now());
                        hoaDonRepository.save(hoaDon);

                        // ============================================================
                        // >>> THÊM ĐOẠN CODE NÀY ĐỂ XÓA GIỎ HÀNG <<<
                        // ============================================================

                        // 1. Kiểm tra xem người dùng có đăng nhập không
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                            // A. Nếu là thành viên -> Xóa giỏ hàng trong Database
                            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                            NguoiDung nguoiDung = userDetails.getUser();
                            // Gọi hàm xóa giỏ hàng của User (Bạn cần đảm bảo Service có hàm này)
                            // Ví dụ: gioHangClientService.clearCart(nguoiDung);
                            // Hoặc xóa thủ công nếu chưa có hàm clear:
                            try {
                                // Logic xóa giỏ hàng DB ở đây, ví dụ:
                                // gioHangRepository.deleteByNguoiDung(nguoiDung);
                                // Tạm thời để session remove cho chắc chắn nếu logic DB chưa sẵn sàng
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        // 2. Xóa giỏ hàng trong Session (Cho cả khách và User để đảm bảo sạch sẽ)
                        session.removeAttribute(SESSION_CART_KEY);

                        // ============================================================
                    }
                    return "redirect:/order/success/" + hoaDon.getId();
                } else {
                    model.addAttribute("errorMessage", "Lỗi: Không tìm thấy đơn hàng " + vnp_TxnRef);
                    return "client/payment_failure";
                }
            } else {
                model.addAttribute("errorMessage", "Thanh toán không thành công. Mã lỗi: " + vnp_ResponseCode);
                return "client/payment_failure";
            }
        } else {
            model.addAttribute("errorMessage", "Thanh toán thất bại: Sai chữ ký VNPay!");
            return "client/payment_failure";
        }
    }
}