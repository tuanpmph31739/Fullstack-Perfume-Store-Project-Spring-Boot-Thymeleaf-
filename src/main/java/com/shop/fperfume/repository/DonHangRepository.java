package com.shop.fperfume.repository;

import com.shop.fperfume.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonHangRepository extends JpaRepository<HoaDon, Integer> {

    @Query("""
           SELECT h
           FROM HoaDon h
           WHERE (:kenhBan IS NULL OR h.kenhBan = :kenhBan)
             AND (
                  :keyword IS NULL OR :keyword = '' OR
                  h.ma LIKE %:keyword% OR
                  h.tenNguoiNhan LIKE %:keyword% OR
                  h.sdt LIKE %:keyword%
             )
             AND (:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)
           """)
    Page<HoaDon> searchDonHang(@Param("kenhBan") String kenhBan,
                               @Param("keyword") String keyword,
                               @Param("trangThai") String trangThai,
                               Pageable pageable);
}
