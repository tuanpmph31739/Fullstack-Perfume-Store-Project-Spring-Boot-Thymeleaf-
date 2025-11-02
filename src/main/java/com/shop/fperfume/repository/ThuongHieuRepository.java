package com.shop.fperfume.repository;

import com.shop.fperfume.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu,Long> {
    boolean existsByMaThuongHieu(String maThuongHieu);
    boolean existsByTenThuongHieu(String tenThuongHieu);

    Optional<ThuongHieu> findBySlug(String slug);

}
