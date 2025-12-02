package com.shop.fperfume.repository;

import com.shop.fperfume.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// Kiểu khóa chính của ThanhToan là BIGINT -> Long
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {
}