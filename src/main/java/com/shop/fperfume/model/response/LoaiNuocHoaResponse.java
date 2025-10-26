package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.LoaiNuocHoa;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class LoaiNuocHoaResponse {

    private Long id;

    private String tenLoai;

    private String moTaLoai;

    private LocalDate ngayTao;

    private LocalDate ngaySua;

    public LoaiNuocHoaResponse(LoaiNuocHoa lnh) {
        this.id = lnh.getId();
        this.tenLoai = lnh.getTenLoai();
        this.moTaLoai = lnh.getMoTaLoai();
        this.ngayTao = lnh.getNgayTao();
        this.ngaySua = lnh.getNgaySua();
    }
}
