package com.shop.fperfume.repository;

import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.GioHangChiTietId; // Import lớp ID
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
// Kiểu khóa chính là lớp GioHangChiTietId
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, GioHangChiTietId> {

    List<GioHangChiTiet> findByGioHang(GioHang gioHang);

    /**
     * Xóa tất cả chi tiết giỏ hàng thuộc về một giỏ hàng cha
     * Dùng @Transactional để đảm bảo thao tác xóa được thực hiện
     */
    @Transactional
    void deleteAllByGioHang(GioHang gioHang);

    Optional<GioHangChiTiet> findByGioHang_IdAndSanPhamChiTiet_Id(Integer idGioHang, Integer idSanPhamChiTiet);

}