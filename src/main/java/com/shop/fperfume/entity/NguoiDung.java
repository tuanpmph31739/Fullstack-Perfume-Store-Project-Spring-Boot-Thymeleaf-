package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "NguoiDung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ma", unique = true, length = 20)
    private String ma;

    @Column(name = "HoTen", length = 100)
    private String hoTen;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "MatKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "GioiTinh")
    private Integer gioiTinh; // 0: Nữ, 1: Nam

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @Column(name = "Sdt", length = 20)
    private String sdt;

    @Column(name = "VaiTro", nullable = false, length = 20)
    private String vaiTro = "KHACHHANG"; // ADMIN, NHANVIEN, KHACHHANG

    @Column(name = "VerificationCode")
    private String verificationCode;

    @Column(name = "Enabled")
    private Boolean enabled = false;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;
}