package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "NongDo")
public class NongDo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ma")
    private String maNongDo;

    @Column(name = "Ten")
    private String tenNongDo;

    @Column(name = "MoTa")
    private String moTaNongDo;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;
}
