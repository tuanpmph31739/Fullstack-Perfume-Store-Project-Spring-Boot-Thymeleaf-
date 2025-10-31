package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// Kiểu khóa chính của HoaDon là INT -> Integer
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMa(String ma);

    List<HoaDon> findByKhachHangOrderByNgayTaoDesc(NguoiDung khachHang);
}