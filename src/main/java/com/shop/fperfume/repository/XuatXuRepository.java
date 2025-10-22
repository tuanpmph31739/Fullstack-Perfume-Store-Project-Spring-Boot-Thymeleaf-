package com.shop.fperfume.repository;

import com.shop.fperfume.entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Long> {
}
