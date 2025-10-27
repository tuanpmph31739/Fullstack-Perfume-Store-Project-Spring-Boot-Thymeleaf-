package com.shop.fperfume.repository;

import com.shop.fperfume.entity.DungTich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DungTichRepository extends JpaRepository<DungTich,Long> {

    boolean existsByMaDungTich(String maDungTich);
    boolean existsBySoMl(Integer soMl);
}
