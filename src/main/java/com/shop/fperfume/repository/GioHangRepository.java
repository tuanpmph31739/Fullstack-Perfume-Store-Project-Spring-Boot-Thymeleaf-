package com.shop.fperfume.repository;

import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    /**
     * SỬA HÀM NÀY (PHIÊN BẢN HOÀN CHỈNH):
     * Dùng @Query với "LEFT JOIN FETCH" lồng nhau để buộc Hibernate
     * tải luôn mọi thứ chúng ta cần trong trang Giỏ hàng.
     * (Đây là cách sửa lỗi 'no session' hiệu quả nhất)
     */
    @Query("SELECT g FROM GioHang g " +
            "LEFT JOIN FETCH g.gioHangChiTiets gct " +
            // Tải biến thể sản phẩm
            "LEFT JOIN FETCH gct.sanPhamChiTiet spct " +
            // Tải sản phẩm cha (để lấy tên)
            "LEFT JOIN FETCH spct.sanPham sp " +
            // Tải dung tích (để lấy soMl)
            "LEFT JOIN FETCH spct.dungTich " +
            // Tải nồng độ (để lấy ten)
            "LEFT JOIN FETCH spct.nongDo " +
            "WHERE g.khachHang = :khachHang")
    Optional<GioHang> findByKhachHang(@Param("khachHang") NguoiDung khachHang);
}