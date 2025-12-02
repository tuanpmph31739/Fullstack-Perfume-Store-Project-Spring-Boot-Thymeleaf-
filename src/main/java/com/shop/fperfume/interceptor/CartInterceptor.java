package com.shop.fperfume.interceptor;

import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.service.client.GioHangClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.shop.fperfume.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor này tự động chạy trên mỗi request
 * để lấy số lượng giỏ hàng và đưa vào session.
 */
@Component // Đánh dấu đây là một Spring Bean
public class CartInterceptor implements HandlerInterceptor {

    @Autowired
    private GioHangClientService gioHangClientService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();

        // 1. Lấy user từ session (Giả định bạn lưu user vào session sau khi đăng nhập)
        // Đây là logic chúng ta học từ file dự án cũ (CartController.java)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        NguoiDung user = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser();
        }

        if (user != null) {
            // User đã đăng nhập, gọi service
            GioHang gioHang = gioHangClientService.getCartByUser(user);

            int cartSize = 0;
            if (gioHang.getGioHangChiTiets() != null) {
                cartSize = gioHang.getGioHangChiTiets().stream()
                        .mapToInt(GioHangChiTiet::getSoLuong)
                        .sum();
            }

            session.setAttribute("cartSize", cartSize);
        } else {
            // User chưa đăng nhập
            session.setAttribute("cartSize", 0);
        }

        return true;
    }
}