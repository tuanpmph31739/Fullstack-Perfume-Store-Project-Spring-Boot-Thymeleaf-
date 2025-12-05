package com.shop.fperfume.controller.client;

import com.shop.fperfume.config.VNPayConfig;
import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.repository.GiamGiaRepository;
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

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private GiamGiaRepository giamGiaRepository;

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private GioHangClientService gioHangClientService;

    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model, HttpSession session) {

        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();

        try {
            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                sb.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                if (itr.hasNext()) sb.append('&');
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String secureHash = vnPayService.hmacSHA512(vnPayConfig.getHashSecret(), sb.toString());

        if (!secureHash.equals(vnp_SecureHash)) {
            model.addAttribute("errorMessage", "Lỗi bảo mật: Chữ ký không hợp lệ!");
            return "client/payment_failure";
        }

        // ------------ VALID HASH → XỬ LÝ THANH TOÁN -------------- //
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderCode = request.getParameter("vnp_TxnRef");

        HoaDon hoaDon = hoaDonRepository.findByMa(orderCode).orElse(null);

        if (hoaDon == null) {
            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "client/payment_failure";
        }

        if ("00".equals(responseCode)) {

            // CHỈ cập nhật khi đơn đang chờ thanh toán
            if ("DANG_CHO_THANH_TOAN".equals(hoaDon.getTrangThai())) {

                hoaDon.setTrangThai("HOAN_THANH");
                hoaDon.setNgayThanhToan(java.time.LocalDateTime.now());
                hoaDonRepository.save(hoaDon);

                // 🔥🔥 TRỪ MÃ GIẢM GIÁ
                GiamGia giamGia = hoaDon.getGiamGia();
                if (giamGia != null && giamGia.getSoLuong() > 0) {
                    giamGia.setSoLuong(giamGia.getSoLuong() - 1);
                    giamGiaRepository.save(giamGia);
                }

                // Xóa giỏ hàng
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
                    gioHangClientService.clearCart(user.getUser());
                }
                session.removeAttribute("GUEST_CART");
            }

            return "redirect:/order/success/" + hoaDon.getId();
        }

        model.addAttribute("errorMessage", "Thanh toán thất bại! Mã lỗi: " + responseCode);
        return "client/payment_failure";
    }
}
