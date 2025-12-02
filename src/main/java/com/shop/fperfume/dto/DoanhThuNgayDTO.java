package com.shop.fperfume.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuNgayDTO {
    private LocalDate ngay;
    private BigDecimal doanhThu;
}
