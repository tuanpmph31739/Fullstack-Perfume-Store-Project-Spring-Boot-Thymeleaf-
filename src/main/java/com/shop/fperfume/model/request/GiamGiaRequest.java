package com.shop.fperfume.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiamGiaRequest {

    private Integer id;           // Khớp với kiểu trong Entity (Integer)

    private String ma;            // Mã giảm giá
    private String ten;           // Tên giảm giá
    private String loaiGiam;      // 'PERCENT' hoặc 'AMOUNT'
    private BigDecimal giaTri;    // Giá trị giảm
    private Integer soLuong;      // Số lượng mã phát hành
    private LocalDate ngayBatDau; // Dùng LocalDate thay vì LocalDateTime để khớp Entity
    private LocalDate ngayKetThuc;
    private Integer trangThai;    // Trạng thái: 1=active, 0=inactive
    private Integer idSanPham;       // ID sản phẩm (Long để khớp với Entity SanPham)
}
