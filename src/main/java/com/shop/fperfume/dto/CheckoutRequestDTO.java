package com.shop.fperfume.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequestDTO {

    // Thông tin người nhận
    private String tenNguoiNhan;
    private String sdt;
    private String diaChi;
    private String email;
    private Integer gioiTinh;

    // ID của hình thức thanh toán (COD, VNPay...)
    private Long idThanhToan;

    private String ghiChu;

    private Boolean luuDiaChiMacDinh;

    public Boolean getLuuDiaChiMacDinh() {
        return luuDiaChiMacDinh;
    }

    public void setLuuDiaChiMacDinh(Boolean luuDiaChiMacDinh) {
        this.luuDiaChiMacDinh = luuDiaChiMacDinh;
    }

}