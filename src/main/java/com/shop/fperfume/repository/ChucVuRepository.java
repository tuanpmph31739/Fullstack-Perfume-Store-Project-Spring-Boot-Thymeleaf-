package com.shop.fperfume.repository;

import com.shop.fperfume.entity.ChucVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChucVuRepository extends JpaRepository<ChucVu,Long> {
}
