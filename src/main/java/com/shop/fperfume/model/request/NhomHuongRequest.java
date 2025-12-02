package com.shop.fperfume.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class NhomHuongRequest {

    private Long id;

    @NotBlank(message = "Mã nhóm hương không được để trống")
    private String maNhomHuong;

    @NotBlank(message = "Tên nhóm hương không được để trống")
    private String tenNhomHuong;

}
