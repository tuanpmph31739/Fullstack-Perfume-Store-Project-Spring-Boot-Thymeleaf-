package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "SanPhamChiTiet")
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MaSKU")
    private String maSKU;

    @Column(name = "SoLuongTon")
    private Integer soLuongTon;

    @Column(name = "GiaNhap")
    private BigDecimal giaNhap;

    @Column(name = "GiaBan")
    private BigDecimal giaBan;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    // ✅ trạng thái kinh doanh: true=đang kinh doanh, false=ngừng kinh doanh
    @Column(name = "TrangThai")
    private Boolean trangThai;

    // ✅ trạng thái hiển thị client: true=hiện, false=ẩn
    @Column(name = "HienThi")
    private Boolean hienThi;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;

    @ManyToOne
    @JoinColumn(name = "IdSanPham", referencedColumnName = "Id")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "IdDungTich", referencedColumnName = "Id")
    private DungTich dungTich;

    @ManyToOne
    @JoinColumn(name = "IdNongDo", referencedColumnName = "Id")
    private NongDo nongDo;
}
