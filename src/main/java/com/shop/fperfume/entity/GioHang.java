package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "GioHang")
@Getter
@Setter
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id; // Kiểu INT trong SQL

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;

    // --- Mối quan hệ ---

    /**
     * Một giỏ hàng chỉ thuộc về MỘT khách hàng.
     * unique = true đảm bảo một người dùng chỉ có một giỏ hàng.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdKH", nullable = false, unique = true)
    private NguoiDung khachHang;

    /**
     * Một giỏ hàng có thể áp dụng MỘT mã giảm giá (hoặc không).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdGiamGia") // Cho phép null
    private GiamGia giamGia;

    /**
     * Một giỏ hàng có NHIỀU chi tiết sản phẩm.
     * cascade = CascadeType.ALL: Khi lưu/xóa GioHang, các GioHangChiTiet con cũng được lưu/xóa.
     * orphanRemoval = true: Khi xóa một item khỏi List này, nó cũng bị xóa khỏi CSDL.
     */
    @OneToMany(
            mappedBy = "gioHang", // Tên trường "gioHang" trong lớp GioHangChiTiet
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<GioHangChiTiet> gioHangChiTiets;
}