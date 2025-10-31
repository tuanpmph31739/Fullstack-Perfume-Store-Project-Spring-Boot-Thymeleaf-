package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GioHangChiTiet")
@Getter
@Setter
public class GioHangChiTiet {

    // 1. Sử dụng khóa chính tổng hợp
    @EmbeddedId
    private GioHangChiTietId id;

    // 2. Định nghĩa cột dữ liệu (không phải khóa)
    @Column(name = "SoLuong")
    private Integer soLuong;

    // --- Mối quan hệ (Many-to-One) ---

    // 3. Liên kết phần 'idGioHang' của khóa chính với Entity GioHang
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idGioHang") // Tên trường trong lớp GioHangChiTietId
    @JoinColumn(name = "IdGioHang")
    private GioHang gioHang;

    // 4. Liên kết phần 'idSanPhamChiTiet' của khóa chính với Entity SanPhamChiTiet
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSanPhamChiTiet") // Tên trường trong lớp GioHangChiTietId
    @JoinColumn(name = "IdSanPhamChiTiet")
    private SanPhamChiTiet sanPhamChiTiet;
}