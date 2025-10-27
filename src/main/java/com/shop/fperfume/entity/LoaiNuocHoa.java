package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "LoaiNuocHoa")

public class LoaiNuocHoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TenLoai")
    private String tenLoai;

    @Column(name = "MoTa")
    private String moTaLoai;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;
}
