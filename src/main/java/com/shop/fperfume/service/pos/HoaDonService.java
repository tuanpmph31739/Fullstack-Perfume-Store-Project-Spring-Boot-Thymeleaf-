package com.shop.fperfume.service.pos;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.repository.NguoiDungRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoaDonService {

    private final HoaDonRepository  hoaDonRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository, NguoiDungRepository nguoiDungRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    public List<HoaDon> getAll() {
        return hoaDonRepository.findAll();
    }

    public List<HoaDon> getHoaDonChoTaiQuay() {
        return hoaDonRepository.findHoaDonChoTaiQuay();
    }

    public HoaDon getById(Integer id) {
        return hoaDonRepository.findByIdWithKhachHang(id).orElse(null);
    }

    public HoaDon save(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }

    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    // Tạo mới hóa đơn bán hàng tại quầy
    public HoaDon createNewHoaDon() {

        HoaDon hd = new HoaDon();
        // Không set khachHang => IdKH sẽ = NULL
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setKenhBan("TAI_QUAY");
        hd.setTrangThai("DANG_CHO_THANH_TOAN");

        hd.setTongTienHang(BigDecimal.ZERO);
        hd.setTienGiamGia(BigDecimal.ZERO);
        hd.setPhiShip(BigDecimal.ZERO);
        hd.setTongThanhToan(BigDecimal.ZERO);

        return hoaDonRepository.save(hd);
    }

}
