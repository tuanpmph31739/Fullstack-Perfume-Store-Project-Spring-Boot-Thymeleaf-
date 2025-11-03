package com.shop.fperfume.repository; // Đảm bảo đúng đường dẫn package

import com.shop.fperfume.entity.SanPhamChiTiet; // Đảm bảo đúng đường dẫn entity
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Có thể thêm @Repository

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd " +
            "WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdFetchingRelationships(@Param("id") Integer id);

    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd ")
    List<SanPhamChiTiet> findAllFetchingRelationships();

    @Query(value = "SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd ",
            countQuery = "SELECT COUNT(spct) FROM SanPhamChiTiet spct")
    Page<SanPhamChiTiet> findAllFetchingRelationships(Pageable pageable);

    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd " +
            "WHERE sp.id = :sanPhamId")
    List<SanPhamChiTiet> findBySanPhamIdFetchingRelationships(@Param("sanPhamId") Integer sanPhamId);

    Optional<SanPhamChiTiet> findByMaSKU(String maSKU);

    @Query("""
    SELECT spct FROM SanPhamChiTiet spct
    JOIN FETCH spct.sanPham sp
    LEFT JOIN FETCH sp.thuongHieu th
    WHERE spct.id IN (
        SELECT MIN(ct2.id)
        FROM SanPhamChiTiet ct2
        GROUP BY ct2.sanPham.id
    )
    """)
    List<SanPhamChiTiet> findDistinctBySanPham(Pageable pageable);

    @Query("""
    SELECT DISTINCT spct FROM SanPhamChiTiet spct
    JOIN FETCH spct.sanPham sp
    JOIN FETCH sp.thuongHieu th
    WHERE th.slug = :slug
    """)
    List<SanPhamChiTiet> findByThuongHieuSlug(@Param("slug") String slug);

    @Query("""
SELECT spct FROM SanPhamChiTiet spct
WHERE spct.sanPham.id = :idSanPham
  AND spct.dungTich.soMl = :soMl
""")
    Optional<SanPhamChiTiet> findFirstBySanPhamIdAndDungTich_SoMl(Integer idSanPham, Integer soMl);

    List<SanPhamChiTiet> findBySanPham_IdOrderByDungTich_SoMlAsc(Integer idSanPham);


}
