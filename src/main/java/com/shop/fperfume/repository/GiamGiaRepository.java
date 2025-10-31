package com.shop.fperfume.repository;

import com.shop.fperfume.entity.GiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// Kiểu khóa chính của GiamGia là INT -> Integer
public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {

    Optional<GiamGia> findByMa(String ma);
}