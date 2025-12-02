package com.shop.fperfume.repository;

import com.shop.fperfume.entity.SanPhamChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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


    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd " +
            "WHERE sp.id = :sanPhamId")
    List<SanPhamChiTiet> findBySanPhamIdFetchingRelationships(@Param("sanPhamId") Integer sanPhamId);

    Optional<SanPhamChiTiet> findByMaSKU(String maSKU);

    @Query("""
        SELECT spct FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        WHERE spct.id IN (
            SELECT MIN(ct2.id)
            FROM SanPhamChiTiet ct2
            GROUP BY ct2.sanPham.id
        )
    """)
    Page<SanPhamChiTiet> findAllSanPhamChiTiet(Pageable pageable);


    @Query("""
        SELECT spct FROM SanPhamChiTiet spct
        WHERE spct.sanPham.id = :idSanPham
          AND spct.dungTich.soMl = :soMl
    """)
    Optional<SanPhamChiTiet> findFirstBySanPhamIdAndDungTich_SoMl(Integer idSanPham, Integer soMl);

    List<SanPhamChiTiet> findBySanPham_IdOrderByDungTich_SoMlAsc(Integer idSanPham);


    @Query(value = """
        WITH ranked AS (
            SELECT
                s.*,
                ROW_NUMBER() OVER (
                    PARTITION BY s.IdSanPham
                    ORDER BY s.GiaBan ASC, s.Id ASC
                ) AS rn
            FROM SanPhamChiTiet s
            JOIN SanPham p      ON p.Id = s.IdSanPham
            JOIN LoaiNuocHoa l  ON l.Id = p.IdLoai
            WHERE l.TenLoai = :tenLoai
        )
        SELECT * FROM ranked
        WHERE rn = 1
        ORDER BY IdSanPham
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """,
            nativeQuery = true)
    List<SanPhamChiTiet> findDaiDienByLoaiNuocHoa(
            @Param("tenLoai") String tenLoai,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Query(
            value = """
            WITH base AS (
                SELECT s.*, p.Id AS PId, th.Id AS THId, l.TenLoai AS TenLoai
                FROM SanPhamChiTiet s
                JOIN SanPham p          ON p.Id = s.IdSanPham
                LEFT JOIN ThuongHieu th ON th.Id = p.IdThuongHieu
                LEFT JOIN LoaiNuocHoa l ON l.Id = p.IdLoai
                WHERE (:loai IS NULL OR l.TenLoai = :loai)
                  AND (:brandsSize = 0 OR th.Id IN (:brands))
                  AND (:minP IS NULL OR s.GiaBan >= :minP)
                  AND (:maxP IS NULL OR s.GiaBan <= :maxP)
            ),
            ranked AS (
                SELECT base.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY base.IdSanPham
                        ORDER BY base.GiaBan ASC, base.Id ASC
                    ) AS rn
                FROM base
            )
            SELECT * FROM ranked
            WHERE rn = 1
            ORDER BY PId
            """,
            countQuery = """
            WITH base AS (
                SELECT p.Id AS PId
                FROM SanPhamChiTiet s
                JOIN SanPham p          ON p.Id = s.IdSanPham
                LEFT JOIN ThuongHieu th ON th.Id = p.IdThuongHieu
                LEFT JOIN LoaiNuocHoa l ON l.Id = p.IdLoai
                WHERE (:loai IS NULL OR l.TenLoai = :loai)
                  AND (:brandsSize = 0 OR th.Id IN (:brands))
                  AND (:minP IS NULL OR s.GiaBan >= :minP)
                  AND (:maxP IS NULL OR s.GiaBan <= :maxP)
            )
            SELECT COUNT(*) FROM (SELECT DISTINCT PId FROM base) t
            """,
            nativeQuery = true
    )
    Page<SanPhamChiTiet> searchAdvancedOneVariant(
            @Param("brands") List<Integer> brands,
            @Param("brandsSize") int brandsSize,
            @Param("loai") String loai,
            @Param("minP") BigDecimal minP,
            @Param("maxP") BigDecimal maxP,
            Pageable pageable
    );
//    @Query("SELECT spct FROM SanPhamChiTiet spct JOIN FETCH spct.sanPham sp")
//    List<SanPhamChiTiet> findAllWithSanPham();
    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt") // Thêm dòng này
    List<SanPhamChiTiet> findAllWithSanPham();
    List<SanPhamChiTiet> findBySanPham_ThuongHieu_Slug(String slug);

    @Query("""
    SELECT spct
    FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        LEFT JOIN spct.dungTich dt
        LEFT JOIN spct.nongDo nd
    WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(sp.tenNuocHoa) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:sku IS NULL OR :sku = '' OR LOWER(spct.maSKU) LIKE LOWER(CONCAT('%', :sku, '%')))
      AND (:giaMin IS NULL OR spct.giaBan >= :giaMin)
      AND (:giaMax IS NULL OR spct.giaBan <= :giaMax)
      AND (:dungTichId IS NULL OR dt.id = :dungTichId)
      AND (:nongDoId IS NULL OR nd.id = :nongDoId)
""")
    Page<SanPhamChiTiet> searchSanPhamChiTiet(@Param("keyword") String keyword,
                                              @Param("sku") String sku,
                                              @Param("giaMin") BigDecimal giaMin,
                                              @Param("giaMax") BigDecimal giaMax,
                                              @Param("dungTichId") Integer dungTichId,
                                              @Param("nongDoId") Integer nongDoId,
                                              Pageable pageable);

}
