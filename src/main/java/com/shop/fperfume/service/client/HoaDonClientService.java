package com.shop.fperfume.service.client;

import com.shop.fperfume.DTO.CheckoutRequestDTO;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;

import java.util.List;

public interface HoaDonClientService {

    /**
     * Xử lý logic checkout chính:
     * 1. Chuyển item từ giỏ hàng sang chi tiết hóa đơn.
     * 2. Tính toán tổng tiền, giảm giá.
     * 3. Quyết định trạng thái ban đầu (Chờ xác nhận / Chờ thanh toán).
     * 4. Xóa giỏ hàng.
     */
    HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo);

    /**
     * Lấy lịch sử đơn hàng cho khách hàng đã đăng nhập.
     * (Lấy ý tưởng từ file OrderServiceImpl.java)
     */
    List<HoaDon> getOrdersByUser(NguoiDung khachHang);

    /**
     * Lấy chi tiết 1 đơn hàng và xác thực
     * (Bảo mật: Đảm bảo khách hàng chỉ xem được đơn của chính mình)
     */
    HoaDon getOrderDetailForUser(Integer hoaDonId, NguoiDung khachHang);
}