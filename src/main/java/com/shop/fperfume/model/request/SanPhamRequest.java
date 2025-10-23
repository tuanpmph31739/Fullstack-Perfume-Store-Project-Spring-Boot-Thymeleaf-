package com.shop.fperfume.model.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SanPhamRequest {

    private Long id;

    private String tenNuocHoa;

    private Integer soLuongTon;

    private BigDecimal giaNhap;

    private BigDecimal giaBan;

    private String moTa;

    private MultipartFile hinhAnh;

    private Boolean trangThai;

    private Long idThuongHieu;

    private Long idXuatXu;

    private Long idDungTich;

    private Long idLoaiNuocHoa;

    private Long idMuaThichHop;

}
