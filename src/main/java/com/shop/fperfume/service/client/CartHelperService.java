package com.shop.fperfume.service.client; // Hoặc package service của bạn

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartHelperService {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    /**
     * Chuyển từ Map<Integer, Integer> trong Session thành một đối tượng GioHang "ảo"
     * (Lấy từ GioHangController)
     */
    public GioHang buildVirtualCartFromSession(Map<Integer, Integer> guestCart) {
        GioHang gioHang = new GioHang();
        if (guestCart == null || guestCart.isEmpty()) {
            gioHang.setGioHangChiTiets(Collections.emptyList());
            return gioHang;
        }

        List<GioHangChiTiet> chiTietList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : guestCart.entrySet()) {
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(entry.getKey()).orElse(null);
            if (spct == null) continue;

            GioHangChiTiet item = new GioHangChiTiet();
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(spct);
            item.setSoLuong(entry.getValue());
            chiTietList.add(item);
        }
        gioHang.setGioHangChiTiets(chiTietList);
        gioHang.setGiamGia(null); // Guest không dùng voucher
        return gioHang;
    }

    /**
     * Tính toán tổng tiền, giảm giá
     * (Lấy từ GioHangController)
     */
    public Map<String, Object> calculateCartData(GioHang gioHang) {
        BigDecimal tongTienHang = BigDecimal.ZERO;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        BigDecimal tongThanhToan;
        int cartSize = 0;

        if (gioHang != null && gioHang.getGioHangChiTiets() != null && !gioHang.getGioHangChiTiets().isEmpty()) {
            for (GioHangChiTiet item : gioHang.getGioHangChiTiets()) {
                cartSize += item.getSoLuong();
                if (item.getSanPhamChiTiet() != null && item.getSanPhamChiTiet().getGiaBan() != null) {
                    BigDecimal giaBan = item.getSanPhamChiTiet().getGiaBan();
                    BigDecimal soLuong = new BigDecimal(item.getSoLuong());
                    tongTienHang = tongTienHang.add(giaBan.multiply(soLuong));
                }
            }

            GiamGia giamGia = gioHang.getGiamGia();
            if (giamGia != null) {
                // (Logic tính giảm giá của bạn đã đúng)
                if ("PERCENT".equals(giamGia.getLoaiGiam())) {
                    tienGiamGia = tongTienHang.multiply(giamGia.getGiaTri().divide(new BigDecimal(100)));
                } else {
                    tienGiamGia = giamGia.getGiaTri();
                }
                tienGiamGia = tienGiamGia.min(tongTienHang);
            }
        }

        tongThanhToan = tongTienHang.subtract(tienGiamGia);

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("tongTienHang", tongTienHang);
        cartData.put("tienGiamGia", tienGiamGia);
        cartData.put("tongThanhToan", tongThanhToan);
        cartData.put("cartSize", cartSize);
        return cartData;
    }
}