package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.XuatXu;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class XuatXuResponse {

    private Long id;

    private String maXuatXu;

    private String tenXuatXu;

    private LocalDateTime ngayTao;

    private LocalDateTime ngaySua;

    public XuatXuResponse(XuatXu xx) {
        this.id = xx.getId();
        this.maXuatXu = xx.getMaXuatXu();
        this.tenXuatXu = xx.getTenXuatXu();
        this.ngayTao = xx.getNgayTao();
        this.ngaySua = xx.getNgaySua();
    }
}
