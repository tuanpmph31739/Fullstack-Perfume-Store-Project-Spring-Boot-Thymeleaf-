package com.shop.fperfume.service.client;

import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.NguoiDung;

/**
 * Interface cho các nghiệp vụ Giỏ Hàng phía Client
 */
public interface GioHangClientService {

    /**
     * Lấy giỏ hàng của user.
     * Nếu user chưa có, tự động tạo mới một giỏ hàng.
     * (Vay mượn ý tưởng từ CartServiceImpl.java)
     */
    GioHang getCartByUser(NguoiDung khachHang);

    /**
     * Thêm một sản phẩm (biến thể) vào giỏ hàng.
     * Nếu sản phẩm đã có, tăng số lượng.
     */
    GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuong);

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ.
     */
    GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong);

    /**
     * Xóa một sản phẩm (biến thể) khỏi giỏ hàng.
     */
    GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet);

    /**
     * Áp dụng mã giảm giá vào giỏ hàng.
     */
    GioHang applyVoucher(NguoiDung khachHang, String maGiamGia);

    /**
     * Gỡ mã giảm giá khỏi giỏ hàng.
     */
    GioHang removeVoucher(NguoiDung khachHang);
}