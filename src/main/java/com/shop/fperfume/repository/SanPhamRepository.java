package com.shop.fperfume.repository;

import com.shop.fperfume.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
    SELECT s FROM SanPham s
    LEFT JOIN s.thuongHieu th
    LEFT JOIN s.loaiNuocHoa lnh
    LEFT JOIN s.nhomHuong nh
    LEFT JOIN s.muaThichHop mth
    WHERE (:keyword IS NULL OR :keyword = '' 
           OR LOWER(s.tenNuocHoa) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:loaiId IS NULL OR lnh.id = :loaiId)
      AND (:thuongHieuId IS NULL OR th.id = :thuongHieuId)
      AND (:nhomHuongId IS NULL OR nh.id = :nhomHuongId)
      AND (:muaId IS NULL OR mth.id = :muaId)
""")
    Page<SanPham> searchSanPham(@Param("keyword") String keyword,
                                @Param("loaiId") Long loaiId,
                                @Param("thuongHieuId") Long thuongHieuId,
                                @Param("nhomHuongId") Long nhomHuongId,
                                @Param("muaId") Long muaId,
                                Pageable pageable);

}
