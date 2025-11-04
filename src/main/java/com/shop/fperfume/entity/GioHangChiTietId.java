package com.shop.fperfume.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Lớp này định nghĩa khóa chính tổng hợp cho bảng GioHangChiTiet.
 * Nó phải implement Serializable và có equals()/hashCode().
 */
@Embeddable // Đánh dấu là một phần của Entity khác
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode // Rất quan trọng cho khóa tổng hợp
public class GioHangChiTietId implements Serializable {

    @Column(name = "IdGioHang")
    private Integer idGioHang; // Phải khớp kiểu dữ liệu với Id của GioHang (INT)

    @Column(name = "IdSanPhamChiTiet")
    private Integer idSanPhamChiTiet; // Phải khớp kiểu dữ liệu với Id của SanPhamChiTiet (INT)

    public GioHangChiTietId(Integer idGioHang, Integer idSanPhamChiTiet) {
        this.idGioHang = idGioHang;
        this.idSanPhamChiTiet = idSanPhamChiTiet;
    }
}