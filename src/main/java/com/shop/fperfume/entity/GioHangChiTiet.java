package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GioHangChiTiet")
@Getter
@Setter
public class GioHangChiTiet {

    @EmbeddedId
    private GioHangChiTietId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idGioHang")
    @JoinColumn(name = "IdGioHang")
    private GioHang gioHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSanPhamChiTiet")
    @JoinColumn(name = "IdSanPhamChiTiet")
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "SoLuong")
    private Integer soLuong;
}
