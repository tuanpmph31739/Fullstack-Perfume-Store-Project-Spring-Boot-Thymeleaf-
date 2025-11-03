package com.shop.fperfume.interceptor;

import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.service.client.GioHangClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
        NguoiDung user = (NguoiDung) session.getAttribute("user");

        if (user != null) {
            // 2. User đã đăng nhập, gọi service để lấy giỏ hàng
            // (Service này đã bao gồm logic "Tìm hoặc Tạo")
            GioHang gioHang = gioHangClientService.getCartByUser(user);

            // 3. Tính toán tổng số lượng
            // (Chúng ta lấy logic tính tổng từ GioHangController)
            int cartSize = 0;
            if (gioHang.getGioHangChiTiets() != null) {
                cartSize = gioHang.getGioHangChiTiets().stream()
                        .mapToInt(GioHangChiTiet::getSoLuong)
                        .sum();
            }

            // 4. Đặt cartSize vào session
            session.setAttribute("cartSize", cartSize);
        } else {
            // 5. User chưa đăng nhập, đặt cartSize là 0
            session.setAttribute("cartSize", 0);
        }

        return true; // Cho phép request tiếp tục chạy
    }
}