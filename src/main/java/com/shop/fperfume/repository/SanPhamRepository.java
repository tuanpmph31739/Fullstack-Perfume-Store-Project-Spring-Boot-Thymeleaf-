package com.shop.fperfume.repository;

import com.shop.fperfume.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    Optional<SanPham> findByTenNuocHoa(String tenNuocHoa);

    @Query("""
   SELECT DISTINCT s FROM SanPham s
   JOIN FETCH s.thuongHieu th
   LEFT JOIN FETCH s.xuatXu xx
   LEFT JOIN FETCH s.loaiNuocHoa lnh
   LEFT JOIN FETCH s.muaThichHop mth
   LEFT JOIN FETCH s.nhomHuong nh
   WHERE th.id = :thuongHieuId
""")
    List<SanPham> findByThuongHieuIdFetchAll(@Param("thuongHieuId") Long thuongHieuId);

}
