package com.shop.fperfume.repository;

import com.shop.fperfume.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    @Query("""
           SELECT n FROM NguoiDung n
           WHERE (:vaiTro IS NULL OR :vaiTro = '' OR n.vaiTro = :vaiTro)
             AND (:trangThai IS NULL OR n.trangThai = :trangThai)
             AND (
                    :keyword IS NULL OR :keyword = '' OR
                    LOWER(n.ma)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                    LOWER(n.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                    LOWER(n.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 )
           """)
    Page<NguoiDung> findByFilter(@Param("vaiTro") String vaiTro,
                                 @Param("trangThai") Boolean trangThai,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    Optional<NguoiDung> findByEmail(String email);
    Optional<NguoiDung> findByVerificationCode(String code);

    List<NguoiDung> findBySdtContainingOrHoTenContaining(String sdt, String hoTen);

    List<NguoiDung> findByVaiTro(String vaiTro);

    @Query("SELECT n FROM NguoiDung n " +
            "WHERE n.vaiTro = 'KHACHHANG' " +
            "AND (n.sdt LIKE %:keyword% OR n.hoTen LIKE %:keyword%)")
    List<NguoiDung> searchKhachHangForPos(@Param("keyword") String keyword);
}
