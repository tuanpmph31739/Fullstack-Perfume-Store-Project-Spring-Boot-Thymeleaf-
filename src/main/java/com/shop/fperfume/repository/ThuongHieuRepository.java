package com.shop.fperfume.repository;

import com.shop.fperfume.entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu,Long> {
    boolean existsByMaThuongHieu(String maThuongHieu);
    boolean existsByTenThuongHieu(String tenThuongHieu);

    Optional<ThuongHieu> findBySlug(String slug);

    List<ThuongHieu> findAllByOrderByTenThuongHieuAsc();

    @Query("""
        SELECT th
        FROM ThuongHieu th
        WHERE (:kw IS NULL OR :kw = ''
            OR LOWER(th.maThuongHieu)  LIKE LOWER(CONCAT('%', :kw, '%'))
            OR LOWER(th.tenThuongHieu) LIKE LOWER(CONCAT('%', :kw, '%'))
        )
        """)
    Page<ThuongHieu> searchByKeyword(@Param("kw") String keyword, Pageable pageable);
}
