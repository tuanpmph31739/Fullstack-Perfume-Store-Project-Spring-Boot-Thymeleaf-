package com.shop.fperfume.entity;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Enabled
@Entity
@Table(name = "NhanVien")
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ma")
    private String maNv;

    @Column(name = "Ten")
    private String tenNv;

    @Column(name = "GioiTinh")
    private Boolean gioiTinhNv;

    @Column(name = "DiaChi")
    private String diaChiNv;

    @Column(name = "Sdt")
    private String sdtNv;

    @Column(name = "MatKhau")
    private String matKhau;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "Email")
    private String email;

    @ManyToOne
    @JoinColumn(name = "IdCH", referencedColumnName = "Id")
    private CuaHang cuaHang;

    @ManyToOne
    @JoinColumn(name = "IdCV", referencedColumnName = "Id")
    private ChucVu chucVu;
}
