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

    private Integer id;
    private String maSKU;
    private Integer soLuongTon;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private String hinhAnh;
    private Boolean trangThai;

    private Integer idSanPham;
    private String tenSanPham;
    private String moTa;  // Thêm MoTa vào DTO

    private Long idDungTich;
    private Integer soMl;

    private Long idNongDo;
    private String tenNongDo;

    private LocalDateTime ngayTao;
    private LocalDateTime ngaySua;

    private String tenThuongHieu;
    private Long idThuongHieu;

    private Long idMuaThichHop;
    private String tenMuaThichHop;

    private Long idLoaiNuocHoa;
    private String tenLoaiNuocHoa;

    private Long idNhomHuong;
    private String tenNhomHuong;

    // thêm field
    private String slugThuongHieu;


    // Constructor chuyển đổi từ SanPhamChiTiet sang SanPhamChiTietResponse
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
            // Chỉ kiểm tra ct.getSanPham() một lần
            this.idSanPham = ct.getSanPham().getId();
            this.tenSanPham = ct.getSanPham().getTenNuocHoa();
            this.moTa = ct.getSanPham().getMoTa();  // Lấy MoTa từ SanPham
            if (ct.getSanPham().getThuongHieu() != null) {
                this.idThuongHieu = ct.getSanPham().getThuongHieu().getId();
                this.tenThuongHieu = ct.getSanPham().getThuongHieu().getTenThuongHieu();
            }
            if (ct.getSanPham().getLoaiNuocHoa() != null) {
                this.idLoaiNuocHoa = ct.getSanPham().getLoaiNuocHoa().getId();
                this.tenLoaiNuocHoa = ct.getSanPham().getLoaiNuocHoa().getTenLoai();
            }
            if (ct.getSanPham().getMuaThichHop() != null) {
                this.idMuaThichHop = ct.getSanPham().getMuaThichHop().getId();
                this.tenMuaThichHop = ct.getSanPham().getMuaThichHop().getTenMua();
            }
            if (ct.getSanPham().getNhomHuong() != null) {
                this.idNhomHuong = ct.getSanPham().getNhomHuong().getId();
                this.tenNhomHuong = ct.getSanPham().getNhomHuong().getTenNhomHuong();
            }
            if (ct.getSanPham().getThuongHieu() != null) {
                this.idThuongHieu = ct.getSanPham().getThuongHieu().getId();
                this.tenThuongHieu = ct.getSanPham().getThuongHieu().getTenThuongHieu();
                this.slugThuongHieu = ct.getSanPham().getThuongHieu().getSlug(); // <-- tên getter theo entity của bạn
            }

        }

        if (ct.getDungTich() != null) {
            this.idDungTich = ct.getDungTich().getId();
            this.soMl = ct.getDungTich().getSoMl();
        }

        if (ct.getNongDo() != null) {
            this.idNongDo = ct.getNongDo().getId();
            this.tenNongDo = ct.getNongDo().getTenNongDo();
        }
    }
}
