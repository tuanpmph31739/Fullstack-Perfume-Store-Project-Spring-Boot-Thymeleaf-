package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.XuatXu;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class XuatXuResponse {

    private Long id;

    private String maXuatXu;

    private String tenXuatXu;

    private LocalDate ngayTao;

    private LocalDate ngaySua;

    public XuatXuResponse(XuatXu xx) {
        this.id = xx.getId();
        this.maXuatXu = xx.getMaXuatXu();
        this.tenXuatXu = xx.getTenXuatXu();
        this.ngayTao = xx.getNgayTao();
        this.ngaySua = xx.getNgaySua();
    }
}
