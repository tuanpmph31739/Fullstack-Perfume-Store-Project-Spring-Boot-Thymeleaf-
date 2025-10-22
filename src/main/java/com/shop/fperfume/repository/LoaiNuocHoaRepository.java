package com.shop.fperfume.repository;

import com.shop.fperfume.entity.LoaiNuocHoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiNuocHoaRepository extends JpaRepository<LoaiNuocHoa,Long> {
}
