package com.shop.fperfume.service.pos;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service chứa toàn bộ logic nghiệp vụ cho trang Bán Hàng Tại Quầy (POS).
 */
@Service
@RequiredArgsConstructor // Tự động inject các repository
public class BanHangTaiQuayService {

    // Các repository cần thiết
    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final SanPhamChiTietRepository sanPhamChiTietRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final GiamGiaRepository giamGiaRepo;
    private final ThanhToanRepository thanhToanRepository; // Đảm bảo đã inject

    /**
     * Lấy danh sách sản phẩm (đã fix LazyInitializationException)
     */
    public List<SanPhamChiTiet> getDanhSachSanPham() {
        return sanPhamChiTietRepo.findAllWithSanPham();
    }

    /**
     * Lấy giỏ hàng (đã fix LazyInitializationException)
     */
    public List<HoaDonChiTiet> getChiTietCuaHoaDon(Integer idHoaDon) {
        return hoaDonChiTietRepo.findByHoaDon_Id_WithSanPham(idHoaDon);
    }

    /**
     * Thêm sản phẩm vào hóa đơn (hoặc cập nhật số lượng nếu đã tồn tại).
     */
    @Transactional
    public HoaDonChiTiet addSanPhamVaoHoaDon(Integer idHoaDon, Integer idSPCT, Integer soLuong) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        SanPhamChiTiet spct = sanPhamChiTietRepo.findById(idSPCT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (spct.getSoLuongTon() < soLuong) {
            throw new RuntimeException("Số lượng tồn kho không đủ (" + spct.getSoLuongTon() + ")");
        }

        HoaDonChiTiet hdct = (HoaDonChiTiet) hoaDonChiTietRepo.findByHoaDonAndSanPhamChiTiet(hoaDon, spct)
                .orElse(null);

        if (hdct != null) {
            int soLuongMoi = hdct.getSoLuong() + soLuong;
            if (spct.getSoLuongTon() < soLuongMoi) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho (" + spct.getSoLuongTon() + ")");
            }
            hdct.setSoLuong(soLuongMoi);
        } else {
            hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(soLuong);
            hdct.setDonGia(spct.getGiaBan());
            hdct.setNgayTao(LocalDateTime.now());
        }

        HoaDonChiTiet savedHDCT = hoaDonChiTietRepo.save(hdct);
        updateTongTienHoaDon(hoaDon);
        return savedHDCT;
    }

    /**
     * Tăng số lượng của một sản phẩm trong giỏ hàng.
     */
    @Transactional
    public HoaDonChiTiet tangSoLuong(Integer idHoaDonChiTiet) {
        HoaDonChiTiet hdct = hoaDonChiTietRepo.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        int soLuongMoi = hdct.getSoLuong() + 1;
        if (hdct.getSanPhamChiTiet().getSoLuongTon() < soLuongMoi) {
            throw new RuntimeException("Không đủ tồn kho");
        }

        hdct.setSoLuong(soLuongMoi);
        HoaDonChiTiet savedHDCT = hoaDonChiTietRepo.save(hdct);
        updateTongTienHoaDon(hdct.getHoaDon());
        return savedHDCT;
    }

    /**
     * Giảm số lượng của một sản phẩm trong giỏ hàng.
     */
    @Transactional
    public void giamSoLuong(Integer idHoaDonChiTiet) {
        HoaDonChiTiet hdct = hoaDonChiTietRepo.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));
        HoaDon hoaDon = hdct.getHoaDon();
        int soLuongMoi = hdct.getSoLuong() - 1;

        if (soLuongMoi <= 0) {
            hoaDonChiTietRepo.delete(hdct);
        } else {
            hdct.setSoLuong(soLuongMoi);
            hoaDonChiTietRepo.save(hdct);
        }
        updateTongTienHoaDon(hoaDon);
    }

    /**
     * Xóa vĩnh viễn 1 sản phẩm khỏi giỏ hàng.
     */
    @Transactional
    public void xoaSanPhamVinhVien(Integer idHoaDonChiTiet) {
        HoaDonChiTiet hdct = hoaDonChiTietRepo.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn để xóa"));
        HoaDon hoaDon = hdct.getHoaDon();
        hoaDonChiTietRepo.delete(hdct);
        updateTongTienHoaDon(hoaDon);
    }

    /**
     * Tìm kiếm khách hàng (dùng cho modal).
     */
    public List<NguoiDung> searchKhachHang(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        // Nếu đã tạo searchKhachHangForPos trong repository thì dùng, không thì dùng method cũ
        return nguoiDungRepo.searchKhachHangForPos(keyword.trim());
        // Hoặc:
        // return nguoiDungRepo.findBySdtContainingOrHoTenContaining(keyword.trim(), keyword.trim());
    }

    /**
     * Lấy tất cả khách hàng (dùng cho modal).
     */
    public List<NguoiDung> getAllKhachHang() {
        return nguoiDungRepo.findByVaiTro("KHACHHANG");
    }

    /**
     * Gán khách hàng (có sẵn) vào hóa đơn.
     */
    @Transactional
    public HoaDon ganKhachHang(Integer idHoaDon, Integer idKhachHang) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        NguoiDung khachHang = nguoiDungRepo.findById(Long.valueOf(idKhachHang))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        hoaDon.setKhachHang(khachHang);
        return hoaDonRepo.save(hoaDon);
    }

    /**
     * Cập nhật thông tin khách hàng từ form nhập tay ở màn POS.
     * KHÔNG tạo NguoiDung mới để tránh lỗi MatKhau NOT NULL.
     */
    @Transactional
    public void capNhatThongTinKhach(Integer idHoaDon,
                                     String hoTen,
                                     String sdt,
                                     String email,
                                     String diaChi) {

        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Nếu user không nhập gì thì thôi khỏi làm gì
        if ((hoTen == null || hoTen.isBlank())
                && (sdt == null || sdt.isBlank())
                && (email == null || email.isBlank())
                && (diaChi == null || diaChi.isBlank())) {
            return;
        }

        NguoiDung khachHang = hoaDon.getKhachHang();

        // Chỉ UPDATE nếu là khách hàng (vai trò KHACHHANG) đã gán trước đó (qua modal)
        if (khachHang != null && "KHACHHANG".equalsIgnoreCase(khachHang.getVaiTro())) {

            if (hoTen != null && !hoTen.isBlank()) khachHang.setHoTen(hoTen);
            if (sdt != null && !sdt.isBlank()) khachHang.setSdt(sdt);
            if (email != null && !email.isBlank()) khachHang.setEmail(email);
            if (diaChi != null && !diaChi.isBlank()) khachHang.setDiaChi(diaChi);

            nguoiDungRepo.save(khachHang);
        }

        // Không tạo khách mới nữa -> tránh lỗi MatKhau NULL
        // Thông tin nhập tay vẫn được dùng làm thông tin giao hàng trong thanhToanHoaDonTaiQuay(...)
    }

    /**
     * Áp dụng mã giảm giá.
     */
    @Transactional
    public HoaDon applyVoucher(Integer idHoaDon, String maGiamGia) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        GiamGia giamGia = giamGiaRepo.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        hoaDon.setGiamGia(giamGia);
        hoaDon.setTienGiamGia(giamGia.getGiaTri());

        updateTongTienHoaDon(hoaDon);
        return hoaDon;
    }

    /**
     * Hủy/Gỡ mã giảm giá.
     */
    @Transactional
    public HoaDon removeVoucher(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setGiamGia(null);
        hoaDon.setTienGiamGia(BigDecimal.ZERO);

        updateTongTienHoaDon(hoaDon);
        return hoaDon;
    }

    /**
     * Logic thanh toán cho hóa đơn tại quầy.
     */
    @Transactional
    public HoaDon thanhToanHoaDonTaiQuay(Integer idHoaDon,
                                         String tenNguoiNhan, String sdtGiaoHang,
                                         String diaChiGiaoHang, BigDecimal phiShip,
                                         BigDecimal soTienKhachDua) {

        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(idHoaDon);

        if (chiTiets.isEmpty()) {
            throw new RuntimeException("Hóa đơn không có sản phẩm nào");
        }

        // 1. Gán nhân viên (tạm fix cứng ID 2)
        NguoiDung nhanVien = nguoiDungRepo.findById(2L)
                .orElseThrow(() -> new RuntimeException("LỖI FIX CỨNG: Không tìm thấy Nhân Viên (ID 2)."));
        hoaDon.setNhanVien(nhanVien);

        // 2. KHÔNG tự gán "Khách lẻ" ID=1 nữa.
        // Nếu cần gán khách thì đã gán trước đó (qua modal).

        // 3. Gán Hình thức thanh toán
        ThanhToan ttMacDinh = thanhToanRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("LỖI CẤU HÌNH: Không tìm thấy 'Hình thức thanh toán' (ID 1)."));
        hoaDon.setThanhToan(ttMacDinh);

        // 4. LƯU THÔNG TIN GIAO HÀNG / NGƯỜI NHẬN
        NguoiDung kh = hoaDon.getKhachHang();

        // Nếu form chưa có tên người nhận nhưng hóa đơn có khách -> lấy tên khách
        if ((tenNguoiNhan == null || tenNguoiNhan.isBlank()) && kh != null) {
            tenNguoiNhan = kh.getHoTen();
        }

        // Nếu form chưa có SĐT giao hàng nhưng hóa đơn có khách -> lấy SĐT khách
        if ((sdtGiaoHang == null || sdtGiaoHang.isBlank()) && kh != null) {
            sdtGiaoHang = kh.getSdt();
        }

        // Nếu form chưa có địa chỉ giao hàng nhưng hóa đơn có khách -> lấy địa chỉ khách
        if ((diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) && kh != null) {
            diaChiGiaoHang = kh.getDiaChi();
        }

        // Không còn bịa "Khách lẻ" nếu vẫn null, cứ để null nếu không có gì
        hoaDon.setTenNguoiNhan(tenNguoiNhan);
        hoaDon.setSdt(sdtGiaoHang);
        hoaDon.setDiaChi(diaChiGiaoHang);
        hoaDon.setPhiShip(phiShip != null ? phiShip : BigDecimal.ZERO);

        // cập nhật tổng tiền
        updateTongTienHoaDon(hoaDon);

        // Xử lý tiền khách đưa (có thể null)
        if (soTienKhachDua != null) {
            BigDecimal tong = hoaDon.getTongThanhToan();
            hoaDon.setSoTienKhachDua(soTienKhachDua);
            hoaDon.setSoTienTraLai(
                    soTienKhachDua.subtract(tong).max(BigDecimal.ZERO)
            );
        }

        // 5. Trừ tồn kho
        for (HoaDonChiTiet hdct : chiTiets) {
            SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
            int tonKhoMoi = spct.getSoLuongTon() - hdct.getSoLuong();
            if (tonKhoMoi < 0) {
                throw new RuntimeException("Hết hàng: " + spct.getSanPham().getTenNuocHoa());
            }
            spct.setSoLuongTon(tonKhoMoi);
            sanPhamChiTietRepo.save(spct);
        }

        // 6. Cập nhật trạng thái
        hoaDon.setTrangThai("DA_THANH_TOAN");
        hoaDon.setNgayThanhToan(LocalDateTime.now());

        return hoaDonRepo.save(hoaDon);
    }

    /**
     * (Helper) Tính toán và cập nhật lại tổng tiền cho hóa đơn.
     */
    private void updateTongTienHoaDon(HoaDon hoaDon) {
        if (hoaDon == null) return;

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(hoaDon.getId());

        BigDecimal tongTienHang = chiTiets.stream()
                .map(hdct -> hdct.getDonGia().multiply(BigDecimal.valueOf(hdct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienHang(tongTienHang);

        // PHÍ SHIP SẼ ĐƯỢC CỘNG VÀO TỔNG THANH TOÁN
        BigDecimal phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : BigDecimal.ZERO;
        BigDecimal giamGia = (hoaDon.getTienGiamGia() != null) ? hoaDon.getTienGiamGia() : BigDecimal.ZERO;

        BigDecimal tongThanhToan = tongTienHang.add(phiShip).subtract(giamGia);
        hoaDon.setTongThanhToan(tongThanhToan.max(BigDecimal.ZERO));

        hoaDonRepo.save(hoaDon);
    }

    public Map<SanPham, List<SanPhamChiTiet>> getGroupedSanPham() {
        // 1. Lấy tất cả chi tiết (đã có dung tích)
        List<SanPhamChiTiet> allSPCT = sanPhamChiTietRepo.findAllWithSanPham();

        // 2. Dùng Java Stream để gộp nhóm
        Map<SanPham, List<SanPhamChiTiet>> groupedProducts = allSPCT.stream()
                .collect(Collectors.groupingBy(
                        SanPhamChiTiet::getSanPham,  // Key là SanPham
                        LinkedHashMap::new,          // Giữ thứ tự
                        Collectors.toList()          // Value là List<SanPhamChiTiet>
                ));

        return groupedProducts;
    }
}
