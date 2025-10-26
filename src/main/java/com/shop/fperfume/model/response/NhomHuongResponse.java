package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.NhomHuong;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class NhomHuongResponse {

    private Long id;

    private String maNhomHuong;

    private String tenNhomHuong;

    private LocalDate ngayTao;

    private LocalDate ngaySua;

    public NhomHuongResponse(NhomHuong nh) {
        this.id = nh.getId();
        this.maNhomHuong = nh.getMaNhomHuong();
        this.tenNhomHuong = nh.getTenNhomHuong();
        this.ngayTao = nh.getNgayTao();
        this.ngaySua = nh.getNgaySua();
    }
}
