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

    // =========================
    // ADMIN: lấy chi tiết (không lọc hiển thị)
    // =========================
    @Query("""
        SELECT DISTINCT spct FROM SanPhamChiTiet spct
        LEFT JOIN FETCH spct.sanPham sp
        LEFT JOIN FETCH spct.dungTich dt
        LEFT JOIN FETCH spct.nongDo nd
        WHERE spct.id = :id
    """)
    Optional<SanPhamChiTiet> findByIdFetchingRelationships(@Param("id") Integer id);

    @Query("""
        SELECT DISTINCT spct FROM SanPhamChiTiet spct
        LEFT JOIN FETCH spct.sanPham sp
        LEFT JOIN FETCH spct.dungTich dt
        LEFT JOIN FETCH spct.nongDo nd
    """)
    List<SanPhamChiTiet> findAllFetchingRelationships();

    @Query("""
        SELECT DISTINCT spct FROM SanPhamChiTiet spct
        LEFT JOIN FETCH spct.sanPham sp
        LEFT JOIN FETCH spct.dungTich dt
        LEFT JOIN FETCH spct.nongDo nd
        WHERE sp.id = :sanPhamId
    """)
    List<SanPhamChiTiet> findBySanPhamIdFetchingRelationships(@Param("sanPhamId") Integer sanPhamId);

    Optional<SanPhamChiTiet> findByMaSKU(String maSKU);


    // =========================
    // CLIENT: lấy chi tiết (chỉ HIỂN THỊ)
    // =========================
    @Query("""
        SELECT DISTINCT spct FROM SanPhamChiTiet spct
        LEFT JOIN FETCH spct.sanPham sp
        LEFT JOIN FETCH spct.dungTich dt
        LEFT JOIN FETCH spct.nongDo nd
        WHERE spct.id = :id AND spct.hienThi = true
    """)
    Optional<SanPhamChiTiet> findByIdFetchingRelationshipsVisible(@Param("id") Integer id);

    Optional<SanPhamChiTiet> findByIdAndHienThiTrue(Integer id);

    // =========================
    // CLIENT: list đại diện (mỗi sản phẩm 1 biến thể) chỉ HIỂN THỊ
    // =========================
    @Query("""
        SELECT spct FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        WHERE spct.hienThi = true
          AND spct.id IN (
            SELECT MIN(ct2.id)
            FROM SanPhamChiTiet ct2
            WHERE ct2.hienThi = true
            GROUP BY ct2.sanPham.id
          )
    """)
    Page<SanPhamChiTiet> findAllSanPhamChiTiet(Pageable pageable);

    // =========================
    // CLIENT: lấy biến thể theo dung tích chỉ HIỂN THỊ
    // =========================
    @Query("""
        SELECT spct FROM SanPhamChiTiet spct
        WHERE spct.hienThi = true
          AND spct.sanPham.id = :idSanPham
          AND spct.dungTich.soMl = :soMl
    """)
    Optional<SanPhamChiTiet> findFirstBySanPhamIdAndDungTich_SoMl(
            @Param("idSanPham") Integer idSanPham,
            @Param("soMl") Integer soMl
    );

    // ADMIN: options (không filter)
    List<SanPhamChiTiet> findBySanPham_IdOrderByDungTich_SoMlAsc(Integer idSanPham);

    // CLIENT: options chỉ HIỂN THỊ
    List<SanPhamChiTiet> findBySanPham_IdAndHienThiTrueOrderByDungTich_SoMlAsc(Integer idSanPham);


    // =========================
    // CLIENT: theo brand slug chỉ HIỂN THỊ
    // =========================
    List<SanPhamChiTiet> findBySanPham_ThuongHieu_SlugAndHienThiTrue(String slug);

    // CLIENT: theo sản phẩm chỉ HIỂN THỊ
    List<SanPhamChiTiet> findBySanPhamIdAndHienThiTrue(Integer sanPhamId);


    // =========================
    // CLIENT: đại diện theo loại nước hoa (native) chỉ HIỂN THỊ
    // =========================
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
              AND s.HienThi = 1
        )
        SELECT * FROM ranked
        WHERE rn = 1
        ORDER BY IdSanPham
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """, nativeQuery = true)
    List<SanPhamChiTiet> findDaiDienByLoaiNuocHoa(
            @Param("tenLoai") String tenLoai,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // =========================
    // CLIENT: filter nâng cao (native) chỉ HIỂN THỊ
    // =========================
    @Query(
            value = """
            WITH base AS (
                SELECT s.*, p.Id AS PId, th.Id AS THId, l.TenLoai AS TenLoai
                FROM SanPhamChiTiet s
                JOIN SanPham p          ON p.Id = s.IdSanPham
                LEFT JOIN ThuongHieu th ON th.Id = p.IdThuongHieu
                LEFT JOIN LoaiNuocHoa l ON l.Id = p.IdLoai
                WHERE s.HienThi = 1
                  AND (:loai IS NULL OR l.TenLoai = :loai)
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
            ORDER BY
                CASE WHEN :sort = 'price_asc'  THEN GiaBan END ASC,
                CASE WHEN :sort = 'price_desc' THEN GiaBan END DESC,
                CASE WHEN :sort = 'newest'     THEN PId   END DESC,
                PId
            """,
            countQuery = """
            WITH base AS (
                SELECT p.Id AS PId
                FROM SanPhamChiTiet s
                JOIN SanPham p          ON p.Id = s.IdSanPham
                LEFT JOIN ThuongHieu th ON th.Id = p.IdThuongHieu
                LEFT JOIN LoaiNuocHoa l ON l.Id = p.IdLoai
                WHERE s.HienThi = 1
                  AND (:loai IS NULL OR l.TenLoai = :loai)
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
            @Param("sort") String sort,
            Pageable pageable
    );


    // =========================
    // ADMIN: search màn quản trị (KHÔNG lọc hiển thị)
    // =========================
    @Query("""
        SELECT spct
        FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        LEFT JOIN spct.dungTich dt
        LEFT JOIN spct.nongDo nd
        WHERE (:keyword IS NULL OR :keyword = ''
                  OR LOWER(sp.tenNuocHoa) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(spct.maSKU)     LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:dungTichId IS NULL OR dt.id = :dungTichId)
          AND (:nongDoId   IS NULL OR nd.id = :nongDoId)
          AND (
                :trangThai IS NULL OR :trangThai = ''
             OR (:trangThai = 'NGUNG_BAN' AND spct.trangThai = false)
             OR (:trangThai = 'CON_HANG'  AND spct.trangThai = true AND spct.soLuongTon > 0)
             OR (:trangThai = 'HET_HANG'  AND spct.trangThai = true AND spct.soLuongTon <= 0)
          )
    """)
    Page<SanPhamChiTiet> searchSanPhamChiTiet(@Param("keyword") String keyword,
                                              @Param("dungTichId") Integer dungTichId,
                                              @Param("nongDoId") Integer nongDoId,
                                              @Param("trangThai") String trangThai,
                                              Pageable pageable);


    // =========================
    // CLIENT: suggest/search chỉ HIỂN THỊ
    // =========================
    @Query("""
        SELECT spct
        FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        LEFT JOIN sp.thuongHieu th
        WHERE spct.hienThi = true
          AND (
                LOWER(sp.tenNuocHoa) LIKE LOWER(CONCAT('%', :kw, '%'))
             OR LOWER(th.tenThuongHieu) LIKE LOWER(CONCAT('%', :kw, '%'))
          )
          AND spct.giaBan = (
                SELECT MIN(spct2.giaBan)
                FROM SanPhamChiTiet spct2
                WHERE spct2.sanPham = sp
                  AND spct2.hienThi = true
          )
    """)
    List<SanPhamChiTiet> searchSuggest(@Param("kw") String keyword, Pageable pageable);

    @Query("""
        SELECT spct
        FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        LEFT JOIN sp.thuongHieu th
        WHERE spct.hienThi = true
          AND (
                LOWER(sp.tenNuocHoa) LIKE LOWER(CONCAT('%', :kw, '%'))
             OR LOWER(th.tenThuongHieu) LIKE LOWER(CONCAT('%', :kw, '%'))
          )
          AND spct.giaBan = (
                SELECT MIN(sp2.giaBan)
                FROM SanPhamChiTiet sp2
                WHERE sp2.sanPham = sp
                  AND sp2.hienThi = true
          )
    """)
    Page<SanPhamChiTiet> searchByKeyword(@Param("kw") String keyword, Pageable pageable);


    // =========================
    // CLIENT: related products chỉ HIỂN THỊ
    // =========================
    @Query("""
        SELECT spct
        FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        LEFT JOIN sp.thuongHieu th
        LEFT JOIN sp.nhomHuong nh
        LEFT JOIN sp.loaiNuocHoa ln
        WHERE spct.hienThi = true
          AND spct.id <> :idSpct
          AND (
                (nh.id = :idNhomHuong AND ln.id = :idLoaiNuocHoa)
             OR (th.id = :idThuongHieu)
          )
          AND spct.giaBan = (
                SELECT MIN(spct2.giaBan)
                FROM SanPhamChiTiet spct2
                WHERE spct2.sanPham = sp
                  AND spct2.hienThi = true
          )
        ORDER BY spct.giaBan ASC
    """)
    List<SanPhamChiTiet> findRelatedProducts(@Param("idSpct") Integer idSpct,
                                             @Param("idThuongHieu") Long idThuongHieu,
                                             @Param("idNhomHuong") Long idNhomHuong,
                                             @Param("idLoaiNuocHoa") Long idLoaiNuocHoa,
                                             Pageable pageable);

    // =========================
    // CLIENT: max price bound chỉ HIỂN THỊ
    // =========================
    @Query("""
        SELECT COALESCE(MAX(spct.giaBan), 0)
        FROM SanPhamChiTiet spct
        JOIN spct.sanPham sp
        LEFT JOIN sp.loaiNuocHoa ln
        WHERE spct.hienThi = true
          AND (:loai IS NULL OR ln.tenLoai = :loai)
    """)
    BigDecimal findMaxGiaByLoai(@Param("loai") String loai);


    // =========================
    // Các hàm giữ lại (nếu bạn còn dùng ở chỗ khác)
    // =========================
    Optional<SanPhamChiTiet> findByIdAndTrangThaiTrue(Integer id);
    List<SanPhamChiTiet> findBySanPhamIdAndTrangThaiTrue(Integer sanPhamId);

    @Query("""
        SELECT spct FROM SanPhamChiTiet spct
        JOIN FETCH spct.sanPham sp
        LEFT JOIN FETCH spct.dungTich dt
    """)
    List<SanPhamChiTiet> findAllWithSanPham();

    @Query("""
    SELECT COALESCE(MAX(spct.giaBan), 0)
    FROM SanPhamChiTiet spct
    JOIN spct.sanPham sp
    JOIN sp.thuongHieu th
    LEFT JOIN sp.loaiNuocHoa ln
    WHERE spct.hienThi = true
      AND th.slug = :slug
      AND (:loai IS NULL OR ln.tenLoai = :loai)
""")
    BigDecimal findMaxGiaByThuongHieuSlug(
            @Param("slug") String slug,
            @Param("loai") String loai
    );

}
