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
    @Query("SELECT h FROM HoaDon h WHERE h.trangThai = 'chờ thanh toán'")
    List<HoaDon> findHoaDonChoTaiQuay();
    List<HoaDon> findByKhachHangOrderByNgayTaoDesc(NguoiDung khachHang);
    @Query("SELECT hd FROM HoaDon hd LEFT JOIN FETCH hd.khachHang WHERE hd.id = :idHD")
    Optional<HoaDon> findByIdWithKhachHang(@Param("idHD") Integer idHD);

    // Hàm tìm kiếm nâng cao
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang = :khachHang " +
            "AND (:keyword IS NULL OR :keyword = '' OR h.ma LIKE %:keyword% OR h.tenNguoiNhan LIKE %:keyword%) " +
            "AND (:fromDate IS NULL OR h.ngayTao >= :fromDate) " +
            "AND (:toDate IS NULL OR h.ngayTao <= :toDate) " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> findHistory(
            @Param("khachHang") NguoiDung khachHang,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );


}