package com.shop.fperfume.service.banHang;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.repository.NguoiDungRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonService {

    // Tự động inject nhờ @RequiredArgsConstructor
    private final HoaDonRepository  hoaDonRepository;
    private final NguoiDungRepository nguoiDungRepository;


    public HoaDonService(HoaDonRepository hoaDonRepository, NguoiDungRepository nguoiDungRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }


    public List<HoaDon> getAll() {
        return hoaDonRepository.findAll();
    }

    // 🟢 Lấy hóa đơn trạng thái 0 (chưa thanh toán)
    public List<HoaDon> getHoaDonChoTaiQuay() {
        return hoaDonRepository.findHoaDonChoTaiQuay();
    }

    public HoaDon getById(Integer id) {
        Optional<HoaDon> opt = hoaDonRepository.findByIdWithKhachHang(id);
        return opt.orElse(null);
    }

    public HoaDon save(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }

    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    // 🟢 SỬA LỖI: Tạo mới hóa đơn bán hàng tại quầy
    public HoaDon createNewHoaDon() {

        // 1. Tìm "Khách Lẻ" (Giả sử ID=1)
        // (Nếu bạn dùng ID khác, hãy sửa số 1)
        NguoiDung khachLe = nguoiDungRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("LỖI CẤU HÌNH: Không tìm thấy 'Khách Lẻ' (ID 1) trong DB."));

        // 2. Tạo hóa đơn
        HoaDon hd = new HoaDon();
        hd.setKhachHang(khachLe); // <-- SỬA LỖI (Gán khách hàng mặc định)
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setTrangThai("chờ thanh toán"); // 0 = chờ thanh toán
        hd.setTongTienHang(BigDecimal.ZERO);
        hd.setTienGiamGia(BigDecimal.ZERO);
        hd.setPhiShip(BigDecimal.ZERO);
        hd.setTongThanhToan(BigDecimal.ZERO);

        // ... (Bạn có thể cần gán thêm các giá trị bắt buộc khác, ví dụ ThanhToan) ...

        // 3. Lưu
        return hoaDonRepository.save(hd);
    }
}
