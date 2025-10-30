package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class SanPhamChiTietResponse {

    private Long id;
    private String maSKU;
    private Integer soLuongTon;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private String hinhAnh;
    private Boolean trangThai;

    private Long idSanPham;
    private String tenSanPham;

    private Long idDungTich;
    private Integer soMl;

    private Long idNongDo;
    private String tenNongDo;

    private LocalDateTime ngayTao;
    private LocalDateTime ngaySua;

    private String tenThuongHieu;
    private Long idThuongHieu;

    public SanPhamChiTietResponse(SanPhamChiTiet ct) {
        this.id = ct.getId();
        this.maSKU = ct.getMaSKU();
        this.soLuongTon = ct.getSoLuongTon();
        this.giaNhap = ct.getGiaNhap();
        this.giaBan = ct.getGiaBan();
        this.hinhAnh = ct.getHinhAnh();
        this.trangThai = ct.getTrangThai();
        this.ngayTao = ct.getNgayTao();
        this.ngaySua = ct.getNgaySua();

        if (ct.getSanPham() != null) {
            this.idSanPham = ct.getSanPham().getId();
            this.tenSanPham = ct.getSanPham().getTenNuocHoa();
        }
        if (ct.getDungTich() != null) {
            this.idDungTich = ct.getDungTich().getId();
            this.soMl = ct.getDungTich().getSoMl();
        }
        if (ct.getNongDo() != null) {
            this.idNongDo = ct.getNongDo().getId();
            this.tenNongDo = ct.getNongDo().getTenNongDo();
        }
        if (ct.getSanPham() != null) {
            this.idSanPham = ct.getSanPham().getId();
            this.tenSanPham = ct.getSanPham().getTenNuocHoa();

            if (ct.getSanPham().getThuongHieu() != null) {
                this.idThuongHieu = ct.getSanPham().getThuongHieu().getId();
                this.tenThuongHieu = ct.getSanPham().getThuongHieu().getTenThuongHieu();
            }
        }


    }
}