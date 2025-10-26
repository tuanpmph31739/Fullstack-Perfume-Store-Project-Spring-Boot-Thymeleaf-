package com.shop.fperfume.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class LoaiNuocHoaRequest {

    private String tenLoai;

    private String moTaLoai;

    private LocalDate ngayTao;

    private LocalDate ngaySua;
}
