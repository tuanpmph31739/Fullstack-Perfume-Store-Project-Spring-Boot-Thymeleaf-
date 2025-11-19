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

    @Query("SELECT n FROM NguoiDung n " +
            "WHERE (:vaiTro IS NULL OR n.vaiTro = :vaiTro) " +
            "AND (:trangThai IS NULL OR n.trangThai = :trangThai)")
    Page<NguoiDung> findByFilter(String vaiTro, Boolean trangThai, Pageable pageable);

    Optional<NguoiDung> findByEmail(String email);
    Optional<NguoiDung> findByVerificationCode(String code);
    List<NguoiDung> findBySdtContainingOrHoTenContaining(String sdt, String hoTen);
    List<NguoiDung> findByVaiTro(String vaiTro);
    @Query("SELECT n FROM NguoiDung n " +
            "WHERE n.vaiTro = 'KHACHHANG' " +
            "AND (n.sdt LIKE %:keyword% OR n.hoTen LIKE %:keyword%)")
    List<NguoiDung> searchKhachHangForPos(@Param("keyword") String keyword);
}
