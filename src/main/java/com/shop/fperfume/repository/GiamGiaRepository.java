package com.shop.fperfume.repository;

import com.shop.fperfume.entity.GiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiamGiaRepository extends JpaRepository<GiamGia, Long> {

    // Tìm giảm giá theo mã
    Optional<GiamGia> findByMa(String ma);

    // Kiểm tra mã giảm giá đã tồn tại chưa
    boolean existsByMa(String ma);
}
