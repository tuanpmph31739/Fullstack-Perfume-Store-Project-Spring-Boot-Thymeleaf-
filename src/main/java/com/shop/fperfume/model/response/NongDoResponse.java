package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.NongDo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class NongDoResponse {

    private Long id;

    private String maNongDo;

    private String tenNongDo;

    private String moTaNongDo;

    private LocalDateTime ngayTao;

    private LocalDateTime ngaySua;

    public NongDoResponse(NongDo nd) {
        this.id = nd.getId();
        this.maNongDo = nd.getMaNongDo();
        this.tenNongDo = nd.getTenNongDo();
        this.moTaNongDo = nd.getMoTaNongDo();
        this.ngayTao = nd.getNgayTao();
        this.ngaySua = nd.getNgaySua();
    }
}
