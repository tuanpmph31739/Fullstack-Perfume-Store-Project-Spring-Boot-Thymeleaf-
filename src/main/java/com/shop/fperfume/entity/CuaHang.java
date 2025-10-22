package com.shop.fperfume.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CuaHang")
public class CuaHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ma")
    private String maCh;

    @Column(name = "Ten")
    private String tenCh;

    @Column(name = "DiaChi")
    private String diaChi;

}

