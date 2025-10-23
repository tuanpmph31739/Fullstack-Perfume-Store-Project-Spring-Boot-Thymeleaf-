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

    private Long idThuongHieu;

    private String maThuongHieu;

    private String tenThuongHieu;

    private Long idXuatXu;

    private String maXuatXu;

    private String tenXuatXu;

    private Long idDungTich;

    private String maDungTich;

    private Integer soMl;

    private Long idLoaiNuocHoa;

    private String tenLoai;

    private String moTaLoai;

    private Long idMuaThichHop;

    private String tenMua;

    public SanPhamResponse(SanPham sp) {
        this.id = sp.getId();
        this.tenNuocHoa = sp.getTenNuocHoa();
        this.soLuongTon = sp.getSoLuongTon();
        this.giaNhap = sp.getGiaNhap();
        this.giaBan = sp.getGiaBan();
        this.moTa = sp.getMoTa();
        this.hinhAnh = sp.getHinhAnh();
        this.trangThai = sp.getTrangThai();
        this.idThuongHieu = sp.getThuongHieu().getId();
        this.maThuongHieu = sp.getThuongHieu().getMaThuongHieu();
        this.tenThuongHieu = sp.getThuongHieu().getTenThuongHieu();
        this.idXuatXu = sp.getXuatXu().getId();
        this.maXuatXu = sp.getXuatXu().getMaXuatXu();
        this.tenXuatXu = sp.getXuatXu().getTenXuatXu();
        this.idDungTich = sp.getDungTich().getId();
        this.maDungTich = sp.getDungTich().getMaDungTich();
        this.soMl = sp.getDungTich().getSoMl();
        this.idLoaiNuocHoa = sp.getLoaiNuocHoa().getId();
        this.tenLoai = sp.getLoaiNuocHoa().getTenLoai();
        this.moTaLoai = sp.getLoaiNuocHoa().getMoTaLoai();
        this.idMuaThichHop = sp.getMuaThichHop().getId();
        this.tenMua = sp.getMuaThichHop().getTenMua();
    }
}
