package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.*; // Giả sử bạn có đủ entity
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // Nên thêm NoArgsConstructor

@Getter
@Setter
@NoArgsConstructor
public class SanPhamResponse {

    private Long id;
    private String tenNuocHoa;
    private String moTa;

    private Long idThuongHieu;
    private String tenThuongHieu;

    private Long idXuatXu;
    private String tenXuatXu;

    private Long idLoaiNuocHoa;
    private String tenLoai;

    private Long idMuaThichHop;
    private String tenMua;

    private Long idNhomHuong;
    private String tenNhomHuong;

    public SanPhamResponse(SanPham sp) {
        this.id = sp.getId();
        this.tenNuocHoa = sp.getTenNuocHoa();
        this.moTa = sp.getMoTa();

        if (sp.getThuongHieu() != null) {
            this.idThuongHieu = sp.getThuongHieu().getId();
            this.tenThuongHieu = sp.getThuongHieu().getTenThuongHieu();
        }
        if (sp.getXuatXu() != null) {
            this.idXuatXu = sp.getXuatXu().getId();
            this.tenXuatXu = sp.getXuatXu().getTenXuatXu();
        }
        if (sp.getLoaiNuocHoa() != null) {
            this.idLoaiNuocHoa = sp.getLoaiNuocHoa().getId();
            this.tenLoai = sp.getLoaiNuocHoa().getTenLoai();
        }
        if (sp.getMuaThichHop() != null) {
            this.idMuaThichHop = sp.getMuaThichHop().getId();
            this.tenMua = sp.getMuaThichHop().getTenMua();
        }
        if (sp.getNhomHuong() != null) {
            this.idNhomHuong = sp.getNhomHuong().getId();
            this.tenNhomHuong = sp.getNhomHuong().getTenNhomHuong();
        }
    }
}