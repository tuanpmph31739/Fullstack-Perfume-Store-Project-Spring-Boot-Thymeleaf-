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
public class NongDoRequest {

    private Long id;

    @NotBlank(message = "Mã nồng độ không được để trống")
    private String maNongDo;

    @NotBlank(message = "Tên nồng độ không được để trống")
    private String tenNongDo;

    private String moTaNongDo;

}
