package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// Kiểu khóa chính của HoaDonChiTiet là INT -> Integer
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    List<HoaDonChiTiet> findByHoaDon_Id(Integer idHoaDon);

    Optional<Object> findByHoaDonAndSanPhamChiTiet(HoaDon hoaDon, SanPhamChiTiet sanPhamChiTiet);
    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "JOIN FETCH hdct.sanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "WHERE hdct.hoaDon.id = :idHoaDon")
    List<HoaDonChiTiet> findByHoaDon_Id_WithSanPham(@Param("idHoaDon") Integer idHoaDon);
}