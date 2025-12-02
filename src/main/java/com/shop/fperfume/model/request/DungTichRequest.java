package com.shop.fperfume.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DungTichRequest {

    private Long id;

    @NotBlank(message = "Mã dung tích không được để trống")
    private String maDungTich;

    @NotNull(message = "Số Ml không được để trống")
    @Min(value = 1, message = "Số Ml phải là số dương")
    private Integer soMl;

}
