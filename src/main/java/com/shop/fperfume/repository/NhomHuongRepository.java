package com.shop.fperfume.repository;

import com.shop.fperfume.entity.NhomHuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhomHuongRepository extends JpaRepository<NhomHuong, Long> {

    boolean existsByMaNhomHuong(String maNhomHuong);
    boolean existsByTenNhomHuong(String tenNhomHuong);
}
