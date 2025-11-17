package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.GiamGia;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class GiamGiaResponse {

    private Integer id;
    private String ma;
    private String ten;
    private String moTa;

    private String loaiGiam;
    private BigDecimal giaTri;

    private Integer soLuong;           // ⭐ THÊM – số lượng mã giảm giá

    private BigDecimal donHangToiThieu;
    private BigDecimal giamToiDa;

    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    private Boolean trangThai;
    private String phamViApDung;

    private Integer idSanPhamChiTiet;
    private String tenSanPhamChiTiet;

    public GiamGiaResponse(GiamGia gg) {
        this.id = gg.getId();
        this.ma = gg.getMa();
        this.ten = gg.getTen();
        this.moTa = gg.getMoTa();

        this.loaiGiam = gg.getLoaiGiam();
        this.giaTri = gg.getGiaTri();

        this.soLuong = gg.getSoLuong();  // ⭐ LẤY TỪ ENTITY

        this.donHangToiThieu = gg.getDonHangToiThieu();
        this.giamToiDa = gg.getGiamToiDa();

        this.ngayBatDau = gg.getNgayBatDau();
        this.ngayKetThuc = gg.getNgayKetThuc();

        this.trangThai = gg.getTrangThai();
        this.phamViApDung = gg.getPhamViApDung();

        if (gg.getSanPhamChiTiet() != null) {
            this.idSanPhamChiTiet = gg.getSanPhamChiTiet().getId();
            this.tenSanPhamChiTiet = gg.getSanPhamChiTiet().getMaSKU();
        }
    }
}
