package com.shop.fperfume.model.request;

import jakarta.validation.constraints.NotBlank;
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
public class MuaThichHopRequest {

    private Long id;

    @NotBlank(message = "Mã mùa không được để trống")
    private String maMua;

    @NotBlank(message = "Tên mùa không được để trống")
    private String tenMua;

    private String moTa;

}
