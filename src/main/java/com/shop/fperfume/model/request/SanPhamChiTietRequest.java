package com.shop.fperfume.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    @NotBlank(message = "Mã SKU không được để trống")
    private String maSKU;

    @NotNull(message = "Số lượng tồn không được để trống")
    @Min(value = 0, message = "Số lượng tồn phải lớn hơn hoặc bằng 0")
    private Integer soLuongTon;

    @NotNull(message = "Giá nhập không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá nhập phải lớn hơn hoặc bằng 0")
    private BigDecimal giaNhap;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal giaBan;

    private MultipartFile hinhAnh;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean trangThai;

    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Long idSanPham;

    @NotNull(message = "Vui lòng chọn dung tích")
    private Long idDungTich;

    @NotNull(message = "Vui lòng chọn nồng độ")
    private Long idNongDo;
}