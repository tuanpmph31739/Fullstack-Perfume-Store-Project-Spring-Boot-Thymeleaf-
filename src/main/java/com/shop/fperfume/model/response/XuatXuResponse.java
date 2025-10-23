package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.XuatXu;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class XuatXuResponse {

    private Long id;

    private String maXuatXu;

    private String tenXuatXu;

    public XuatXuResponse(XuatXu xx) {
        this.id = xx.getId();
        this.maXuatXu = xx.getMaXuatXu();
        this.tenXuatXu = xx.getTenXuatXu();
    }
}
