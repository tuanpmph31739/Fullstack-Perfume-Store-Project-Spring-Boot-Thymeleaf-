package com.shop.fperfume.DTO;

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

    // (Chúng ta sẽ lấy IdGiamGia từ Giỏ hàng của user, không cần DTO)
}