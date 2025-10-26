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
public class XuatXuRequest {

    private String maXuatXu;

    private String tenXuatXu;

    private LocalDate ngayTao;

    private LocalDate ngaySua;

}
