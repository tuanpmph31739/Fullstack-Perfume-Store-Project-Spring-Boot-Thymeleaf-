package com.shop.fperfume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "ThuongHieu")
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ma")
    private String maThuongHieu;

    @Column(name = "Ten")
    private String tenThuongHieu;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgaySua")
    private LocalDateTime ngaySua;
}
