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
     * Truy vấn giỏ hàng của khách hàng, đồng thời tải luôn các chi tiết liên quan
     * (giúp tránh lỗi LazyInitializationException khi render trang Thymeleaf).
     */
    @Query("SELECT DISTINCT g FROM GioHang g " +
            "LEFT JOIN FETCH g.gioHangChiTiets gct " +
            "LEFT JOIN FETCH gct.sanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich " +
            "LEFT JOIN FETCH spct.nongDo " +
            "WHERE g.khachHang = :khachHang")
    Optional<GioHang> findByKhachHang(@Param("khachHang") NguoiDung khachHang);
}
