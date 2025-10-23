package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.MuaThichHop;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MuaThichHopResponse {

    private Long id;

    private String maMua;

    private String tenMua;

    private String moTa;

    public MuaThichHopResponse(MuaThichHop mth) {
        this.id = mth.getId();
        this.maMua = mth.getMaMua();
        this.tenMua = mth.getTenMua();
        this.moTa = mth.getMoTa();
    }
}
