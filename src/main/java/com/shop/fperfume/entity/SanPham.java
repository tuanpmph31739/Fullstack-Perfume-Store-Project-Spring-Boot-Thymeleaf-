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

    @Column(name = "MoTa")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "IdThuongHieu", referencedColumnName = "Id")
    private ThuongHieu thuongHieu;

    @ManyToOne
    @JoinColumn(name = "IdXuatXu", referencedColumnName = "Id")
    private XuatXu xuatXu;

    @ManyToOne
    @JoinColumn(name = "IdLoai", referencedColumnName = "Id")
    private LoaiNuocHoa loaiNuocHoa;

    @ManyToOne
    @JoinColumn(name = "IdMuaThichHop", referencedColumnName = "Id")
    private MuaThichHop muaThichHop;

    @ManyToOne
    @JoinColumn(name = "IdNhomHuong", referencedColumnName = "Id")
    private NhomHuong nhomHuong;
}
