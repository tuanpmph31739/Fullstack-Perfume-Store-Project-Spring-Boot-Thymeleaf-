package com.shop.fperfume.repository;

import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
// Kiểu khóa chính của GioHang là INT -> Integer
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    @Query("SELECT g FROM GioHang g LEFT JOIN FETCH g.gioHangChiTiets WHERE g.khachHang = :khachHang")
    Optional<GioHang> findByKhachHang(@Param("khachHang") NguoiDung khachHang);
}