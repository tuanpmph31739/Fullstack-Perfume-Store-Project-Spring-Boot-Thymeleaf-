package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// Kiểu khóa chính của HoaDonChiTiet là INT -> Integer
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
}