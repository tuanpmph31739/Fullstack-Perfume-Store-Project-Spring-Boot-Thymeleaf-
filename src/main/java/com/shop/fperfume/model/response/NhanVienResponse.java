package com.shop.fperfume.model.response;


import com.shop.fperfume.entity.NhanVien;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhanVienResponse {

    private Long id;

    private String maNv;

    private String tenNv;

    private Boolean gioiTinhNv;

    private String diaChiNv;

    private String sdtNv;

    private String matKhau;

    private String trangThai;

    private String email;

    private String maCh;

    private String tenCh;

    private String diaChiCh;

    private String maCv;

    private String tenCv;

    public NhanVienResponse(NhanVien nhanVien) {
        this.id = nhanVien.getId();
        this.maNv = nhanVien.getMaNv();
        this.tenNv = nhanVien.getTenNv();
        this.gioiTinhNv = nhanVien.getGioiTinhNv();
        this.diaChiNv = nhanVien.getDiaChiNv();
        this.sdtNv = nhanVien.getSdtNv();
        this.matKhau = nhanVien.getMatKhau();
        this.trangThai = nhanVien.getTrangThai();
        this.email = nhanVien.getEmail();
        this.maCh = nhanVien.getCuaHang().getMaCh();
        this.tenCh = nhanVien.getCuaHang().getTenCh();
        this.diaChiCh = nhanVien.getCuaHang().getDiaChi();
        this.maCv = nhanVien.getChucVu().getMaCv();
        this.tenCv = nhanVien.getChucVu().getTenCv();
    }
}
