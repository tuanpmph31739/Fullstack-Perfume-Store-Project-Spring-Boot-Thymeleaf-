package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.ThuongHieu;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ThuongHieuResponse {

    private Long id;

    private String maThuongHieu;

    private String tenThuongHieu;

    public ThuongHieuResponse(ThuongHieu th) {
        this.id = th.getId();
        this.maThuongHieu = th.getMaThuongHieu();
        this.tenThuongHieu = th.getTenThuongHieu();
    }
}
