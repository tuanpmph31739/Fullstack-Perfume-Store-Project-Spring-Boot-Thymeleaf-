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

    @Query("SELECT n FROM NguoiDung n WHERE " +
            // 1. Logic tìm kiếm (Tên hoặc SĐT hoặc Email)
            "( :keyword IS NULL OR :keyword = '' OR " +
            "  LOWER(n.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "  n.sdt LIKE CONCAT('%', :keyword, '%') OR " +
            "  LOWER(n.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
            // 2. Logic phân quyền (Chỉ hiện những vai trò nằm trong danh sách cho phép)
            "AND n.vaiTro IN :roles " +
            // 3. Logic lọc trạng thái (Nếu null thì lấy tất cả)
            "AND ( :trangThai IS NULL OR n.trangThai = :trangThai )")
    Page<NguoiDung> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("roles") List<String> roles,
            @Param("trangThai") Boolean trangThai,
            Pageable pageable);

    Optional<NguoiDung> findByEmail(String email);
    Optional<NguoiDung> findByVerificationCode(String code);
    List<NguoiDung> findBySdtContainingOrHoTenContaining(String sdt, String hoTen);
    List<NguoiDung> findByVaiTro(String vaiTro);

    Optional<Object> findById(Integer idKhachHang);
    @Query("SELECT n FROM NguoiDung n " +
            "WHERE n.vaiTro = 'KHACHHANG' " +
            "AND (n.sdt LIKE %:keyword% OR n.hoTen LIKE %:keyword%)")
    List<NguoiDung> searchKhachHangForPos(@Param("keyword") String keyword);
}
