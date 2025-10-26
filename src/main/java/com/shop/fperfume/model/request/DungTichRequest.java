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
public class DungTichRequest {

    private String maDungTich;

    private Integer soMl;

    private LocalDate ngayTao;

    private LocalDate ngaySua;
}
