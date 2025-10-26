package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "DungTich")
public class DungTich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ma")
    private String maDungTich;

    @Column(name = "SoMl")
    private Integer soMl;

    @Column(name = "NgayTao")
    private LocalDate ngayTao;

    @Column(name = "NgaySua")
    private LocalDate ngaySua;
}
