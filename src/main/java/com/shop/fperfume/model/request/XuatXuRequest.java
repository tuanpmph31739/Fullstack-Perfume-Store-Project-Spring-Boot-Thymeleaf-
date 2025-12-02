package com.shop.fperfume.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class XuatXuRequest {

    private Long id;

    @NotBlank(message = "Mã xuất xứ không được để trống")
    private String maXuatXu;

    @NotBlank(message = "Tên xuất xứ không được để trống")
    private String tenXuatXu;

}
