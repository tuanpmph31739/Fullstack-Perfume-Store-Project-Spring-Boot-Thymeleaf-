package com.shop.fperfume.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SanPhamRequest {

    private Long id;

    @NotBlank(message = "Tên nước hoa không được để trống")
    private String tenNuocHoa;

    private String moTa;

    @NotNull(message = "Vui lòng chọn thương hiệu")
    private Long idThuongHieu;

    @NotNull(message = "Vui lòng chọn xuất xứ")
    private Long idXuatXu;

    @NotNull(message = "Vui lòng chọn loại nước hoa")
    private Long idLoaiNuocHoa;

    @NotNull(message = "Vui lòng chọn mùa thích hợp")
    private Long idMuaThichHop;

    @NotNull(message = "Vui lòng chọn nhóm hương")
    private Long idNhomHuong;

}