package com.shop.fperfume.repository;

import com.shop.fperfume.entity.CuaHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuaHangRepository extends JpaRepository<CuaHang,Long> {
}
