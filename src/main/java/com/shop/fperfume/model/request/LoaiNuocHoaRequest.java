package com.shop.fperfume.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class LoaiNuocHoaRequest {

    private Long id;

    @NotBlank(message = "Tên loại nước hoa không được để trống")
    private String tenLoai;

    private String moTaLoai;

}
