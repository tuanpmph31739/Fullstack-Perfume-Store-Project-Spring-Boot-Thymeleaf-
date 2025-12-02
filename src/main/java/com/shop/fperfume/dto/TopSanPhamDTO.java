package com.shop.fperfume.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSanPhamDTO {
    private Integer idSanPham;
    private String tenSanPham;      // tên nước hoa
    private long tongSoLuong;
    private BigDecimal doanhThu;    // tiền bán được từ sản phẩm này
}
