package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
// Kiểu khóa chính của HoaDon là INT -> Integer
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMa(String ma);
    @Query("SELECT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "WHERE h.kenhBan = 'TAI_QUAY' " +
            "AND h.trangThai = 'DANG_CHO_THANH_TOAN' " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHoaDonChoTaiQuay();

    List<HoaDon> findByKhachHangOrderByNgayTaoDesc(NguoiDung khachHang);
    @Query("SELECT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "WHERE h.id = :id")
    Optional<HoaDon> findByIdWithKhachHang(@Param("id") Integer id);

    @Query("SELECT h FROM HoaDon h " +
            "WHERE h.kenhBan = 'TAI_QUAY' " +
            "AND h.trangThai = :trangThai " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHoaDonTaiQuayTheoTrangThai(@Param("trangThai") String trangThai);

    // Nếu cần tìm 1 hóa đơn nháp mới nhất cho NV đang thao tác:
    @Query("SELECT h FROM HoaDon h " +
            "WHERE h.kenhBan = 'TAI_QUAY' " +
            "AND h.trangThai = :trangThai " +
            "AND h.nhanVien.id = :idNv " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHoaDonTaiQuayCuaNv(@Param("trangThai") String trangThai,
                                        @Param("idNv") Integer idNv);

    // Thống kê doanh thu theo ngày trong khoảng thời gian
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

    // Tổng doanh thu trong khoảng
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

    // Tổng số đơn đã thanh toán trong khoảng
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
    // Tổng (không phân kênh)
    long countByTrangThai(String trangThai);

    // Theo kênh
    long countByKenhBanAndTrangThai(String kenhBan, String trangThai);
    // Đếm số đơn theo từng trạng thái trong khoảng thời gian (tất cả kênh)
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

    // Đếm số đơn theo từng trạng thái trong khoảng thời gian + theo kênh (WEB / TAI_QUAY)
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

}