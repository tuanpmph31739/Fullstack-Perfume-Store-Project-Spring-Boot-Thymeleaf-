package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "GiamGia")
@Getter
@Setter
public class GiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Ma", nullable = false, unique = true)
    private String ma;

    @Column(name = "Ten", nullable = false)
    private String ten;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "LoaiGiam", nullable = false)
    private String loaiGiam; // PERCENT hoặc AMOUNT

    @Column(name = "GiaTri", nullable = false)
    private BigDecimal giaTri;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;  // ⭐ Thêm trường số lượng

    @Column(name = "DonHangToiThieu")
    private BigDecimal donHangToiThieu;

    @Column(name = "GiamToiDa")
    private BigDecimal giamToiDa;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;

    @Column(name = "PhamViApDung", nullable = false)
    private String phamViApDung; // SANPHAM hoặc TOAN_CUA_HANG

    // Liên kết sản phẩm chi tiết (nullable nếu toàn cửa hàng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSanPhamChiTiet")
    private SanPhamChiTiet sanPhamChiTiet;
}
