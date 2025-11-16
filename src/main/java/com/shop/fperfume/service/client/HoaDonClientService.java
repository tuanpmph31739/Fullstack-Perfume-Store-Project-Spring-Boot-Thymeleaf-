package com.shop.fperfume.service.client;

import com.shop.fperfume.DTO.CheckoutRequestDTO;
import com.shop.fperfume.entity.GioHang; // <<< THÊM IMPORT NÀY
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;

import java.util.List;

public interface HoaDonClientService {

    /**
     * SỬA: Thêm phương thức mới, linh hoạt hơn
     * @param gioHang Giỏ hàng (có thể là "ảo" từ session hoặc "thật" từ DB)
     * @param khachHang Người dùng (CÓ THỂ LÀ NULL nếu là khách)
     * @param checkoutInfo Thông tin từ form
     */
    HoaDon createOrder(GioHang gioHang, NguoiDung khachHang, CheckoutRequestDTO checkoutInfo);

    /**
     * Giữ lại hàm cũ (hoặc cập nhật nó)
     */
    HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo);

    List<HoaDon> getOrdersByUser(NguoiDung khachHang);

    HoaDon getOrderDetailForUser(Integer hoaDonId, NguoiDung khachHang);
}