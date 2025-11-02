package com.shop.fperfume.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiamGiaRequest {

    private Long id;
    private String ma;           // Mã giảm giá
    private String ten;          // Tên giảm giá
    private String loaiGiam;     // 'PERCENT' hoặc 'AMOUNT'
    private BigDecimal giaTri;   // Giá trị giảm
    private Integer soLuong;     // Số lượng mã phát hành
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer trangThai;   // Trạng thái: 1=active, 0=inactive (ví dụ)
    private Long idSanPham;   // Id của sản phẩm nếu giảm giá áp dụng cho sản phẩm cụ thể
}
