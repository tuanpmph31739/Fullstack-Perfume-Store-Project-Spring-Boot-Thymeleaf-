package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HoaDonChiTiet")
@Getter
@Setter
public class HoaDonChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal donGia; // Giá đã "đóng băng" tại thời điểm mua

    // Cột 'ThanhTien' được tính toán trong DB (PERSISTED)
    // Chúng ta đánh dấu nó là insertable=false, updatable=false để JPA không cố ghi đè
    @Column(name = "ThanhTien", insertable = false, updatable = false)
    private BigDecimal thanhTien;

    @Column(name = "GhiChu")
    private String ghiChu;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;

    // --- Mối quan hệ (Many-to-One) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdHoaDon", nullable = false)
    private HoaDon hoaDon; // Liên kết BẮT BUỘC

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSanPhamChiTiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet; // Liên kết BẮT BUỘC
}