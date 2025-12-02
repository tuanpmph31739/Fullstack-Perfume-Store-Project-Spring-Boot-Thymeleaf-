package com.shop.fperfume.service.client;

import com.shop.fperfume.dto.CheckoutRequestDTO;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;

import java.util.List;

public interface HoaDonClientService {

    // Tạo đơn hàng mới (Xử lý chính)
    HoaDon createOrder(GioHang gioHang, NguoiDung khachHang, CheckoutRequestDTO checkoutInfo);

    // Tạo đơn từ giỏ hàng (Hàm cũ, giữ lại để tương thích)
    HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo);

    // Lấy danh sách đơn hàng (Có tìm kiếm & Lọc ngày)
    List<HoaDon> getOrdersByUser(NguoiDung khachHang, String keyword, String fromDateStr, String toDateStr);

    // Lấy chi tiết đơn hàng (Có kiểm tra quyền)
    HoaDon getOrderDetailForUser(Integer hoaDonId, NguoiDung khachHang);

    // Hủy đơn hàng
    void cancelOrder(Integer hoaDonId, NguoiDung khachHang, String lyDoHuy);
}