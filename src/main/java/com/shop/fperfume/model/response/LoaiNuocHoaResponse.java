package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.LoaiNuocHoa;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class LoaiNuocHoaResponse {

    private Long id;

    private String tenLoai;

    private String moTaLoai;

    private LocalDateTime ngayTao;

    private LocalDateTime ngaySua;

    public LoaiNuocHoaResponse(LoaiNuocHoa lnh) {
        this.id = lnh.getId();
        this.tenLoai = lnh.getTenLoai();
        this.moTaLoai = lnh.getMoTaLoai();
        this.ngayTao = lnh.getNgayTao();
        this.ngaySua = lnh.getNgaySua();
    }
}
