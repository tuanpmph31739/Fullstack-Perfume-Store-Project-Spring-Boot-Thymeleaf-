package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.SanPham;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SanPhamResponse {
    private Long id;

    private String tenNuocHoa;

    private Integer soLuongTon;

    private BigDecimal giaNhap;

    private BigDecimal giaBan;

    private String moTa;

    private String hinhAnh;

    private Boolean trangThai;

    private String maThuongHieu;

    private String tenThuongHieu;

    private String maXuatXu;

    private String tenXuatXu;

    private String maDungTich;

    private Integer soMl;

    private String tenLoai;

    private String moTaLoai;

    public SanPhamResponse(SanPham sp) {
        this.id = sp.getId();
        this.tenNuocHoa = sp.getTenNuocHoa();
        this.soLuongTon = sp.getSoLuongTon();
        this.giaNhap = sp.getGiaNhap();
        this.giaBan = sp.getGiaBan();
        this.moTa = sp.getMoTa();
        this.hinhAnh = sp.getHinhAnh();
        this.trangThai = sp.getTrangThai();
        this.maThuongHieu = sp.getThuongHieu().getMaThuongHieu();
        this.tenThuongHieu = sp.getThuongHieu().getTenThuongHieu();
        this.maXuatXu = sp.getXuatXu().getMaXuatXu();
        this.tenXuatXu = sp.getXuatXu().getTenXuatXu();
        this.maDungTich = sp.getDungTich().getMaDungTich();
        this.soMl = sp.getDungTich().getSoMl();
        this.tenLoai = sp.getLoaiNuocHoa().getTenLoai();
        this.moTaLoai = sp.getLoaiNuocHoa().getMoTaLoai();
    }
}
