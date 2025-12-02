package com.shop.fperfume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SearchSuggestionDTO {
    private Integer id;
    private String tenSanPham;
    private String tenThuongHieu;
    private String hinhAnh;
    private BigDecimal giaBan;
}
