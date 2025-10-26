package com.shop.fperfume.model.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class NhomHuongRequest {

    private String maNhomHuong;

    private String tenNhomHuong;

    private LocalDate ngayTao;

    private LocalDate ngaySua;
}
