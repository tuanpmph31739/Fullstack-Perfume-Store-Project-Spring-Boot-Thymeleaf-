package com.shop.fperfume.repository;

import com.shop.fperfume.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    Optional<SanPham> findByTenNuocHoa(String tenNuocHoa);
}
