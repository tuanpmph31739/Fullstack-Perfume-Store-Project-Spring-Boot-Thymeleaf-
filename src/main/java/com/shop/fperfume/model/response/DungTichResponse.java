package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.DungTich;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DungTichResponse {

    private Long id;

    private String maDungTich;

    private Integer soMl;

    public DungTichResponse(DungTich dt) {
        this.id = dt.getId();
        this.maDungTich = dt.getMaDungTich();
        this.soMl = dt.getSoMl();
    }
}
