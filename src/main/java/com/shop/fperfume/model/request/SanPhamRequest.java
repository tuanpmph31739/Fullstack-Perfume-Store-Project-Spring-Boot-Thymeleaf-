package com.shop.fperfume.model.request;

import com.shop.fperfume.entity.*;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    private String moTa;

    private Long idThuongHieu;

    private Long idXuatXu;

    private Long idLoaiNuocHoa;

    private Long idMuaThichHop;

    private Long idNhomHuong;

}
