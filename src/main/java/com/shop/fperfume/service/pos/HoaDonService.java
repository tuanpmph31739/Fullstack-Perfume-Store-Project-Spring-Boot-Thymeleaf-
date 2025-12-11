package com.shop.fperfume.service.pos;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.repository.NguoiDungRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository,
                         NguoiDungRepository nguoiDungRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.nguoiDungRepository = nguoiDungRepository;
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

    // Lấy nhân viên hiện đang đăng nhập từ Spring Security
    private NguoiDung getCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Không tìm thấy người dùng đăng nhập!");
        }

        String username = auth.getName(); // tuỳ bạn cấu hình login bằng gì

        // Nếu bạn login bằng email:
        return nguoiDungRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + username));

        // Nếu bạn login bằng mã (Ma):
        // return nguoiDungRepository.findByMa(username)
        //         .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + username));
    }

    // Tạo mới hóa đơn bán hàng tại quầy
    public HoaDon createNewHoaDon() {

        HoaDon hd = new HoaDon();
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setKenhBan("TAI_QUAY");
        hd.setTrangThai("DANG_CHO_THANH_TOAN");

        hd.setTongTienHang(BigDecimal.ZERO);
        hd.setTienGiamGia(BigDecimal.ZERO);
        hd.setPhiShip(BigDecimal.ZERO);
        hd.setTongThanhToan(BigDecimal.ZERO);

        // 👉 GÁN NHÂN VIÊN ĐANG ĐĂNG NHẬP
        NguoiDung nv = getCurrentNhanVien();
        hd.setNhanVien(nv);       // nếu entity HoaDon có field NhanVien (ManyToOne)
        // hoặc: hd.setIdNV(nv.getId());  // nếu dùng field IdNV dạng int

        return hoaDonRepository.save(hd);
    }

}
