package com.shop.fperfume.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangResponse {

    // ====== Thông tin chính (HoaDon) ======
    private Integer id;
    private Long idKH;
    private Long idNV;

    private String ma;
    private LocalDateTime ngayTao;
    private LocalDateTime ngaySua;
    private LocalDateTime ngayThanhToan;
    private LocalDateTime ngayGiaoHang;

    private String tenNguoiNhan;
    private String diaChi;
    private String sdt;
    private String trangThai;   // ví dụ: CHO_XAC_NHAN, DANG_GIAO, HOAN_THANH...
    private String kenhBan;     // 'WEB' hoặc 'TAI_QUAY'

    // ====== Tiền ======
    private BigDecimal tongTienHang;
    private BigDecimal tienGiamGia;
    private BigDecimal phiShip;
    private BigDecimal tongThanhToan;

    private Integer idGiamGia;
    private Long idThanhToan;

    // Tên phương thức thanh toán (join bảng ThanhToan)
    private String phuongThucThanhToan;

    // ====== Thông tin tổng hợp (tùy dùng) ======
    private Integer soDongChiTiet;
    private Integer tongSoLuongSanPham;

    // Thông tin người dùng (join NguoiDung)
    private String tenKhachHang;
    private String tenNhanVien;

    private String ghiChu;
}
