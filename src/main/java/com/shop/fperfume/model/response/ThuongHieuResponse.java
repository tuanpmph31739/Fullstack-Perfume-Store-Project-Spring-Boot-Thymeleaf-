package com.shop.fperfume.model.response;

import com.shop.fperfume.entity.ThuongHieu;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class ThuongHieuResponse {

    private Long id;

    private String maThuongHieu;

    private String tenThuongHieu;

    private LocalDateTime ngayTao;

    private LocalDateTime ngaySua;

    public ThuongHieuResponse(ThuongHieu th) {
        this.id = th.getId();
        this.maThuongHieu = th.getMaThuongHieu();
        this.tenThuongHieu = th.getTenThuongHieu();
        this.ngayTao = th.getNgayTao();
        this.ngaySua = th.getNgaySua();
    }
}
