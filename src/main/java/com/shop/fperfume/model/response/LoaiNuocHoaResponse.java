package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.LoaiNuocHoa;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoaiNuocHoaResponse {

    private Long id;

    private String tenLoai;

    private String moTaLoai;

    public LoaiNuocHoaResponse(LoaiNuocHoa lnh) {
        this.id = lnh.getId();
        this.tenLoai = lnh.getTenLoai();
        this.moTaLoai = lnh.getMoTaLoai();
    }
}
