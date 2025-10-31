package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "GiamGia")
@Getter
@Setter
public class GiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Ma", unique = true, nullable = false)
    private String ma;

    @Column(name = "Ten")
    private String ten;

    @Column(name = "LoaiGiam")
    private String loaiGiam; // 'PERCENT' hoặc 'AMOUNT'

    @Column(name = "GiaTri")
    private BigDecimal giaTri;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "TrangThai")
    private Integer trangThai;

    // --- Mối quan hệ ---

    @OneToMany(mappedBy = "giamGia", fetch = FetchType.LAZY)
    private List<HoaDon> hoaDons;

    // Liên kết với SanPham (có thể null nếu giảm giá toàn cửa hàng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSanPham")
    private SanPham sanPham;
}