package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    // Lấy toàn bộ chi tiết theo hóa đơn
    List<HoaDonChiTiet> findByHoaDon_Id(Integer idHoaDon);

    // ❗ FIX LỖI: Trả về đúng kiểu HoaDonChiTiet
    Optional<HoaDonChiTiet> findByHoaDonAndSanPhamChiTiet(HoaDon hoaDon, SanPhamChiTiet sanPhamChiTiet);

    // Lấy chi tiết kèm sản phẩm (JOIN FETCH tránh N+1)
    @Query("""
            SELECT hdct FROM HoaDonChiTiet hdct
            JOIN FETCH hdct.sanPhamChiTiet spct
            JOIN FETCH spct.sanPham sp
            WHERE hdct.hoaDon.id = :idHoaDon
            """)
    List<HoaDonChiTiet> findByHoaDon_Id_WithSanPham(@Param("idHoaDon") Integer idHoaDon);

    // Tính tổng thành tiền toàn hóa đơn
    @Query("""
            SELECT COALESCE(SUM(hdct.thanhTien), 0)
            FROM HoaDonChiTiet hdct
            WHERE hdct.hoaDon.id = :idHoaDon
            """)
    BigDecimal tinhTongTienHang(@Param("idHoaDon") Integer idHoaDon);

    // Top sản phẩm bán chạy (tất cả kênh)
    @Query("""
        SELECT sp.tenNuocHoa AS tenSanPham,
               MIN(spct.hinhAnh) AS hinhAnh,
               SUM(hdct.soLuong) AS tongSoLuong,
               SUM(hdct.thanhTien) AS tongDoanhThu
        FROM HoaDonChiTiet hdct
        JOIN hdct.hoaDon hd
        JOIN hdct.sanPhamChiTiet spct
        JOIN spct.sanPham sp
        WHERE hd.trangThai = 'HOAN_THANH'
          AND hd.ngayThanhToan BETWEEN :start AND :end
        GROUP BY sp.tenNuocHoa
        ORDER BY SUM(hdct.soLuong) DESC
        """)
    List<Object[]> topSanPhamBanChay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



    @Query("""
        SELECT sp.tenNuocHoa AS tenSanPham,
               MIN(spct.hinhAnh) AS hinhAnh,
               SUM(hdct.soLuong) AS tongSoLuong,
               SUM(hdct.thanhTien) AS tongDoanhThu
        FROM HoaDonChiTiet hdct
        JOIN hdct.hoaDon hd
        JOIN hdct.sanPhamChiTiet spct
        JOIN spct.sanPham sp
        WHERE hd.trangThai = 'HOAN_THANH'
          AND hd.kenhBan = :kenhBan
          AND hd.ngayThanhToan BETWEEN :start AND :end
        GROUP BY sp.tenNuocHoa
        ORDER BY SUM(hdct.soLuong) DESC
        """)
    List<Object[]> topSanPhamBanChayTheoKenh(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("kenhBan") String kenhBan
    );



}
