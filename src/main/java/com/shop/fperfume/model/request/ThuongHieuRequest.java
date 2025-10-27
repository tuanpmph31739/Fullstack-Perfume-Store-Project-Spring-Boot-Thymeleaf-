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
public class ThuongHieuRequest {

    private Long id;

    @NotBlank(message = "Mã thương hiệu không được để trống")
    private String maThuongHieu;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String tenThuongHieu;

}
