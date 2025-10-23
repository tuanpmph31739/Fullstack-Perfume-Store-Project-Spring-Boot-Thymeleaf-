package com.shop.fperfume.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class LoaiNuocHoaRequest {

    private String tenLoai;

    private String moTaLoai;
}
