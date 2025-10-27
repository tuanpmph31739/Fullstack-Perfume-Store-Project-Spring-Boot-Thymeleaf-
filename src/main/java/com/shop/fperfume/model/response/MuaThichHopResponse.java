package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.MuaThichHop;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class MuaThichHopResponse {

    private Long id;

    private String maMua;

    private String tenMua;

    private String moTa;

    private LocalDateTime ngayTao;

    private LocalDateTime ngaySua;


    public MuaThichHopResponse(MuaThichHop mth) {
        this.id = mth.getId();
        this.maMua = mth.getMaMua();
        this.tenMua = mth.getTenMua();
        this.moTa = mth.getMoTa();
        this.ngayTao = mth.getNgayTao();
        this.ngaySua = mth.getNgaySua();
    }
}
