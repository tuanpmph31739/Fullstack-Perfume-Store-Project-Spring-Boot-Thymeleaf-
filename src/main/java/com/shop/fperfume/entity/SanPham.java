package com.shop.fperfume.entity;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SanPham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TenNuocHoa")
    private String tenNuocHoa;

    @Column(name = "SoLuongTon")
    private Integer soLuongTon;

    @Column(name = "GiaNhap")
    private BigDecimal giaNhap;

    @Column(name = "GiaBan")
    private BigDecimal giaBan;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @ManyToOne
    @JoinColumn(name = "IdThuongHieu", referencedColumnName = "Id")
    private ThuongHieu thuongHieu;

    @ManyToOne
    @JoinColumn(name = "IdXuatXu", referencedColumnName = "Id")
    private XuatXu xuatXu;

    @ManyToOne
    @JoinColumn(name = "IdDungTich", referencedColumnName = "Id")
    private DungTich dungTich;

    @ManyToOne
    @JoinColumn(name = "IdLoai", referencedColumnName = "Id")
    private LoaiNuocHoa loaiNuocHoa;
}
