package com.shop.fperfume.model.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class NhanVienRequest {

    private Long id;

    private String maNv;

    private String tenNv;

    private Boolean gioiTinhNv;

    private String diaChiNv;

    private String sdtNv;

    private String matKhau;

    private String trangThai;

    private String email;

    private Long idCh;

    private Long idCv;



}
