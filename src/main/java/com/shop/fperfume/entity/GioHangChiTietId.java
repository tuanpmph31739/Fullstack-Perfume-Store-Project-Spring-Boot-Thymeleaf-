package com.shop.fperfume.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Khóa chính tổng hợp cho bảng GioHangChiTiet
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GioHangChiTietId implements Serializable {

    @EqualsAndHashCode.Include
    @Column(name = "IdGioHang")
    private Integer idGioHang;

    @EqualsAndHashCode.Include
    @Column(name = "IdSanPhamChiTiet")
    private Integer idSanPhamChiTiet;

    public GioHangChiTietId(Integer idGioHang, Integer idSanPhamChiTiet) {
        this.idGioHang = idGioHang;
        this.idSanPhamChiTiet = idSanPhamChiTiet;
    }
}
