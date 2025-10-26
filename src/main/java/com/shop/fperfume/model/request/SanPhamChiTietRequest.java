package com.shop.fperfume.model.request;

import com.shop.fperfume.entity.DungTich;
import com.shop.fperfume.entity.NongDo;
import com.shop.fperfume.entity.SanPham;
import jakarta.persistence.*;
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
public class SanPhamChiTietRequest {

    private Long id;

    private String maSKU;

    private Integer soLuongTon;

    private BigDecimal giaNhap;

    private BigDecimal giaBan;

    private MultipartFile hinhAnh;

    private Boolean trangThai;

    private Long idSanPham;

    private Long idDungTich;

    private Long idNongDo;
}
