package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "HoaDon")
@Getter
@Setter
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Ma", unique = true)
    private String ma;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayThanhToan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "TenNguoiNhan")
    private String tenNguoiNhan;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "Sdt")
    private String sdt;

    @Column(name = "GhiChu")
    private String ghiChu;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "TongTienHang", nullable = false)
    private BigDecimal tongTienHang;

    @Column(name = "TienGiamGia", nullable = false)
    private BigDecimal tienGiamGia;

    @Column(name = "PhiShip")
    private BigDecimal phiShip;

    @Column(name = "TongThanhToan", nullable = false)
    private BigDecimal tongThanhToan;

    @Column(name = "NgayGiaoHang")
    private LocalDateTime ngayGiaoHang;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;

    @Column(name = "KenhBan")
    private String kenhBan;

    @Column(name = "SoTienKhachDua")
    private BigDecimal soTienKhachDua;

    @Column(name = "SoTienTraLai")
    private BigDecimal soTienTraLai;

    @Column(name = "MaGiaoDichThanhToan")
    private String maGiaoDichThanhToan;

    // --- Mối quan hệ (Many-to-One) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdKH", nullable = true)
    private NguoiDung khachHang; // Liên kết BẮT BUỘC

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdNV")
    private NguoiDung nhanVien; // Có thể null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdGiamGia")
    private GiamGia giamGia; // Có thể null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdThanhToan")
    private ThanhToan thanhToan; // Liên kết BẮT BUỘC

    // --- Mối quan hệ (One-to-Many) ---

    // Liên kết đến các chi tiết hóa đơn
    // cascade = CascadeType.ALL: Khi lưu/xóa HoaDon, tự động lưu/xóa các HoaDonChiTiet con
    // orphanRemoval = true: Khi xóa một chi tiết khỏi List, nó cũng bị xóa khỏi DB
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoaDonChiTiet> hoaDonChiTiets;
}