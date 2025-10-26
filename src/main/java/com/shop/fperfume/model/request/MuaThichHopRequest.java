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
public class MuaThichHopRequest {

    private String maMua;

    private String tenMua;

    private String moTa;

    private LocalDate ngayTao;

    private LocalDate ngaySua;
}
