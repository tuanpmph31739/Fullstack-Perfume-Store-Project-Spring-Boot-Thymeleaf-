package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.GiamGia;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GiamGiaResponse {

    private Integer id;           // Entity GiamGia thường dùng Integer cho id
    private String ma;
    private String ten;
    private String loaiGiam;
    private BigDecimal giaTri;
    private Integer soLuong;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer trangThai;
    private Integer idSanPham;       // Đổi sang Long để đồng bộ với SanPham entity
    private String tenSanPham;

    public GiamGiaResponse(GiamGia giamGia) {
        this.id = giamGia.getId();
        this.ma = giamGia.getMa();
        this.ten = giamGia.getTen();
        this.loaiGiam = giamGia.getLoaiGiam();
        this.giaTri = giamGia.getGiaTri();
        this.soLuong = giamGia.getSoLuong();
        this.ngayBatDau = giamGia.getNgayBatDau();
        this.ngayKetThuc = giamGia.getNgayKetThuc();
        this.trangThai = giamGia.getTrangThai();

        if (giamGia.getSanPham() != null) {
            this.idSanPham = giamGia.getSanPham().getId();
            this.tenSanPham = giamGia.getSanPham().getTenNuocHoa(); // đổi đúng theo entity
        }
    }
}
