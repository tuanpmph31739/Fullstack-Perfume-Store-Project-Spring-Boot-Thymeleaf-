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

    private Integer id;

    private String ma;              // Mã giảm giá
    private String ten;             // Tên giảm giá
    private String moTa;            // Mô tả (tùy chọn)

    private String loaiGiam;        // 'PERCENT' hoặc 'AMOUNT'
    private BigDecimal giaTri;      // Giá trị giảm

    private Integer soLuong;        // ⭐ THÊM — số lượng mã giảm giá

    private BigDecimal donHangToiThieu; // Đơn hàng tối thiểu
    private BigDecimal giamToiDa;       // Giảm tối đa (chỉ dùng khi %)

    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    private Boolean trangThai;          // true/false

    private String phamViApDung;        // SANPHAM / TOAN_CUA_HANG

    private Integer idSanPhamChiTiet;   // Liên kết SPCT (nullable)
}
