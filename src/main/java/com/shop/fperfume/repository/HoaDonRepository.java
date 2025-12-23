package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    // 1. Tìm kiếm cơ bản
    Optional<HoaDon> findByMa(String ma);

    // 2. Lấy danh sách đơn hàng của khách (Sắp xếp mới nhất)
    List<HoaDon> findByKhachHangOrderByNgayTaoDesc(NguoiDung khachHang);

    // 3. Lấy chi tiết đơn hàng (Fetch Join để tránh lỗi Lazy Loading User)
    @Query("""
    SELECT hd FROM HoaDon hd
    LEFT JOIN FETCH hd.khachHang kh
    LEFT JOIN FETCH hd.giamGia gg
    LEFT JOIN FETCH gg.sanPhamChiTiet spct
    WHERE hd.id = :id
""")
    Optional<HoaDon> findByIdWithKhachHang(@Param("id") Integer id);


    // 4. TÌM KIẾM & LỌC LỊCH SỬ (CLIENT)
    @Query("SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN h.hoaDonChiTiets hdct " +
            "LEFT JOIN hdct.sanPhamChiTiet spct " +
            "LEFT JOIN spct.sanPham sp " +
            "WHERE h.khachHang = :khachHang " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "     OR h.ma LIKE %:keyword% " +
            "     OR h.tenNguoiNhan LIKE %:keyword% " +
            "     OR sp.tenNuocHoa LIKE %:keyword%) " +
            "AND (:fromDate IS NULL OR h.ngayTao >= :fromDate) " +
            "AND (:toDate IS NULL OR h.ngayTao <= :toDate) " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHistory(
            @Param("khachHang") NguoiDung khachHang,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // =================================================================
    // 5. CÁC QUERY DÀNH CHO BÁN HÀNG TẠI QUẦY (POS / ADMIN)
    // =================================================================

    @Query("SELECT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "WHERE h.kenhBan = 'TAI_QUAY' " +
            "AND h.trangThai = 'DANG_CHO_THANH_TOAN' " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHoaDonChoTaiQuay();

    @Query("SELECT h FROM HoaDon h " +
            "WHERE h.kenhBan = 'TAI_QUAY' " +
            "AND h.trangThai = :trangThai " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHoaDonTaiQuayTheoTrangThai(@Param("trangThai") String trangThai);

    @Query("SELECT h FROM HoaDon h " +
            "WHERE h.kenhBan = 'TAI_QUAY' " +
            "AND h.trangThai = :trangThai " +
            "AND h.nhanVien.id = :idNv " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHoaDonTaiQuayCuaNv(@Param("trangThai") String trangThai,
                                        @Param("idNv") Integer idNv);

    // =================================================================
    // 6. CÁC QUERY THỐNG KÊ (DASHBOARD)
    // =================================================================

    // Thống kê doanh thu theo ngày
    @Query("""
    SELECT cast(hd.ngayThanhToan as date) AS ngay,
           SUM(hd.tongThanhToan) AS doanhThu
    FROM HoaDon hd
    WHERE hd.trangThai = 'HOAN_THANH'
      AND hd.ngayThanhToan BETWEEN :start AND :end
    GROUP BY cast(hd.ngayThanhToan as date)
    ORDER BY cast(hd.ngayThanhToan as date) ASC
    """)
    List<Object[]> thongKeDoanhThuTheoNgay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Tổng doanh thu
    @Query("""
        SELECT COALESCE(SUM(hd.tongThanhToan), 0)
        FROM HoaDon hd
        WHERE hd.trangThai = 'HOAN_THANH'
          AND hd.ngayThanhToan BETWEEN :start AND :end
        """)
    java.math.BigDecimal tongDoanhThuTrongKhoang(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Tổng số đơn
    @Query("""
        SELECT COUNT(hd)
        FROM HoaDon hd
        WHERE hd.trangThai = 'HOAN_THANH'
          AND hd.ngayThanhToan BETWEEN :start AND :end
        """)
    Long tongDonDaThanhToanTrongKhoang(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Thống kê theo kênh bán
    @Query("""
    SELECT cast(hd.ngayThanhToan as date) AS ngay,
           SUM(hd.tongThanhToan) AS doanhThu
    FROM HoaDon hd
    WHERE hd.trangThai = 'HOAN_THANH'
      AND hd.kenhBan = :kenhBan
      AND hd.ngayThanhToan BETWEEN :start AND :end
    GROUP BY cast(hd.ngayThanhToan as date)
    ORDER BY cast(hd.ngayThanhToan as date) ASC
    """)
    List<Object[]> thongKeDoanhThuTheoNgayTheoKenh(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("kenhBan") String kenhBan
    );

    @Query("""
    SELECT COALESCE(SUM(hd.tongThanhToan), 0)
    FROM HoaDon hd
    WHERE hd.trangThai = 'HOAN_THANH'
      AND hd.kenhBan = :kenhBan
      AND hd.ngayThanhToan BETWEEN :start AND :end
    """)
    java.math.BigDecimal tongDoanhThuTrongKhoangTheoKenh(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("kenhBan") String kenhBan
    );

    @Query("""
    SELECT COUNT(hd)
    FROM HoaDon hd
    WHERE hd.trangThai = 'HOAN_THANH'
      AND hd.kenhBan = :kenhBan
      AND hd.ngayThanhToan BETWEEN :start AND :end
    """)
    Long tongDonDaThanhToanTrongKhoangTheoKenh(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("kenhBan") String kenhBan
    );

    // Đếm số lượng đơn theo trạng thái
    long countByTrangThai(String trangThai);

    long countByKenhBanAndTrangThai(String kenhBan, String trangThai);

    @Query("""
        SELECT hd.trangThai, COUNT(hd)
        FROM HoaDon hd
        WHERE hd.ngayThanhToan BETWEEN :start AND :end
        GROUP BY hd.trangThai
        """)
    List<Object[]> demSoDonTheoTrangThaiTrongKhoang(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT hd.trangThai, COUNT(hd)
        FROM HoaDon hd
        WHERE hd.kenhBan = :kenhBan
          AND hd.ngayThanhToan BETWEEN :start AND :end
        GROUP BY hd.trangThai
        """)
    List<Object[]> demSoDonTheoTrangThaiTheoKenh(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("kenhBan") String kenhBan
    );

    @Query("""
        SELECT h FROM HoaDon h
        WHERE
          (:keyword IS NULL OR :keyword = '' OR
             LOWER(h.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(h.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(h.sdt) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:kenhBan IS NULL OR :kenhBan = '' OR h.kenhBan = :kenhBan)
          AND (:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)
        """)
    Page<HoaDon> searchHoaDon(@Param("keyword") String keyword,
                              @Param("kenhBan") String kenhBan,
                              @Param("trangThai") String trangThai,
                              Pageable pageable);

    @Query("""
       SELECT h FROM HoaDon h
       WHERE (:kenhBan IS NULL OR :kenhBan = '' OR h.kenhBan = :kenhBan)
         AND (:keyword IS NULL OR :keyword = '' 
              OR h.ma LIKE %:keyword%
              OR h.tenNguoiNhan LIKE %:keyword%
              OR h.sdt LIKE %:keyword%)
         AND (:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)
       """)
    Page<HoaDon> searchDonHang(@Param("kenhBan") String kenhBan,
                               @Param("keyword") String keyword,
                               @Param("trangThai") String trangThai,
                               Pageable pageable);


}