package com.shop.fperfume.repository;

import com.shop.fperfume.entity.GiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {

    // Tìm giảm giá theo mã
    Optional<GiamGia> findByMa(String ma);

    // Kiểm tra mã giảm giá đã tồn tại chưa
    boolean existsByMa(String ma);

    // Lọc + tìm kiếm (theo mã / tên / mô tả + loại giảm + trạng thái)
    @Query("""
        SELECT g FROM GiamGia g
        WHERE (:keyword IS NULL OR :keyword = '' OR
                 LOWER(g.ma)   LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(g.ten)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(g.moTa) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
          AND (:loaiGiam IS NULL OR :loaiGiam = '' OR g.loaiGiam = :loaiGiam)
          AND (:trangThai IS NULL OR g.trangThai = :trangThai)
        """)
    Page<GiamGia> searchGiamGia(@Param("keyword") String keyword,
                                @Param("loaiGiam") String loaiGiam,
                                @Param("trangThai") Boolean trangThai,
                                Pageable pageable);
}
