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

    @Query("""
    SELECT h FROM HoaDon h
    LEFT JOIN h.khachHang kh
    WHERE
      (:keyword IS NULL OR :keyword = '' OR
         LOWER(h.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(h.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(h.sdt) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (:kenhBan IS NULL OR :kenhBan = '' OR h.kenhBan = :kenhBan)
      AND (:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)
    """)
    Page<HoaDon> searchHoaDon(@Param("keyword") String keyword,
                              @Param("kenhBan") String kenhBan,
                              @Param("trangThai") String trangThai,
                              Pageable pageable);


}
