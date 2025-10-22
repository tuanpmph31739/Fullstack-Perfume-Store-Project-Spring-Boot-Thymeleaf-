package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
