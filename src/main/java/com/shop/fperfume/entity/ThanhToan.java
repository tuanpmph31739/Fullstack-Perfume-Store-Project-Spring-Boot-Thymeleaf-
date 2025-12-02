package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ThanhToan")
@Getter
@Setter
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "HinhThucThanhToan", nullable = false)
    private String hinhThucThanhToan;

    @Column(name = "Mota")
    private String moTa;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;

    // --- Mối quan hệ ---
    // Một hình thức thanh toán có thể được dùng cho nhiều hóa đơn
    @OneToMany(mappedBy = "thanhToan", fetch = FetchType.LAZY)
    private List<HoaDon> hoaDons;
}