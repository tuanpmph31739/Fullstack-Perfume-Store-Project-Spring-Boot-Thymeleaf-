package com.shop.fperfume.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DungTichOptionResponse {
    private Integer idSpct;        // id biến thể
    private Integer soMl;       // dung tích
    private BigDecimal giaBan;  // giá của biến thể
}
