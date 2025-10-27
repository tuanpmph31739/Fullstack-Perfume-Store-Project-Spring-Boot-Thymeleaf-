package com.shop.fperfume.repository;

import com.shop.fperfume.entity.NongDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NongDoRepository extends JpaRepository<NongDo, Long> {

    boolean existsByMaNongDo(String maNongDo);
    boolean existsByTenNongDo(String tenNongDo);
}
