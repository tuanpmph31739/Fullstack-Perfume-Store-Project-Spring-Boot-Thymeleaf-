package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.GiamGia;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GiamGiaResponse {

    private Long id;             // Đồng bộ với entity
    private String ma;
    private String ten;
    private String loaiGiam;
    private BigDecimal giaTri;
    private Integer soLuong;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer trangThai;
    private Long idSanPham;
    private String tenSanPham;

    // Constructor nhận entity GiamGia
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
            this.tenSanPham = giamGia.getSanPham().getTenNuocHoa(); // hoặc getTenNuocHoa() nếu bạn dùng tên đó
        }
    }
}
