package com.shop.fperfume.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ThuongHieuRequest {

    private String maThuongHieu;

    private String tenThuongHieu;

    private LocalDate ngayTao;

    private LocalDate ngaySua;
}
