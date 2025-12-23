package com.shop.fperfume.service.pos;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BanHangTaiQuayService {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final SanPhamChiTietRepository sanPhamChiTietRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final GiamGiaRepository giamGiaRepo;
    private final ThanhToanRepository thanhToanRepository;

    public List<SanPhamChiTiet> getDanhSachSanPham() {
        return sanPhamChiTietRepo.findAllWithSanPham();
    }

    public List<HoaDonChiTiet> getChiTietCuaHoaDon(Integer idHoaDon) {
        return hoaDonChiTietRepo.findByHoaDon_Id_WithSanPham(idHoaDon);
    }

    @Transactional
    public HoaDonChiTiet addSanPhamVaoHoaDon(Integer idHoaDon, Integer idSPCT, Integer soLuong) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        SanPhamChiTiet spct = sanPhamChiTietRepo.findById(idSPCT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (spct.getTrangThai() == null || !spct.getTrangThai()) {
            throw new IllegalArgumentException("Sản phẩm "
                    + spct.getSanPham().getTenNuocHoa()
                    + " đã ngừng kinh doanh");
        }
        int ton = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
        if (ton < soLuong) throw new RuntimeException("Số lượng tồn kho không đủ (" + ton + ")");

        HoaDonChiTiet hdct = hoaDonChiTietRepo.findByHoaDonAndSanPhamChiTiet(hoaDon, spct).orElse(null);

        if (hdct != null) {
            int soLuongMoi = hdct.getSoLuong() + soLuong;
            if (soLuongMoi > ton) throw new RuntimeException("Tổng số lượng vượt quá tồn kho (" + ton + ")");
            hdct.setSoLuong(soLuongMoi);
        } else {
            hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(soLuong);
            hdct.setDonGia(spct.getGiaBan());
            hdct.setNgayTao(LocalDateTime.now());
        }

        HoaDonChiTiet saved = hoaDonChiTietRepo.save(hdct);
        updateTongTienHoaDon(hoaDon);
        return saved;
    }

    @Transactional
    public HoaDonChiTiet tangSoLuong(Integer idHoaDonChiTiet) {
        HoaDonChiTiet hdct = hoaDonChiTietRepo.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        SanPhamChiTiet spct = hdct.getSanPhamChiTiet();

        if (spct.getTrangThai() == null || !spct.getTrangThai()) {
            throw new RuntimeException("Sản phẩm "
                    + spct.getSanPham().getTenNuocHoa()
                    + " đã ngừng kinh doanh, không thể tăng số lượng");
        }

        int soLuongMoi = hdct.getSoLuong() + 1;
        int ton = hdct.getSanPhamChiTiet().getSoLuongTon() != null ? hdct.getSanPhamChiTiet().getSoLuongTon() : 0;
        if (soLuongMoi > ton) throw new RuntimeException("Không đủ tồn kho");

        hdct.setSoLuong(soLuongMoi);
        HoaDonChiTiet saved = hoaDonChiTietRepo.save(hdct);
        updateTongTienHoaDon(hdct.getHoaDon());
        return saved;
    }

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

    @Transactional
    public void xoaSanPhamVinhVien(Integer idHoaDonChiTiet) {
        HoaDonChiTiet hdct = hoaDonChiTietRepo.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn để xóa"));
        HoaDon hoaDon = hdct.getHoaDon();
        hoaDonChiTietRepo.delete(hdct);
        updateTongTienHoaDon(hoaDon);
    }

    public List<NguoiDung> searchKhachHang(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        return nguoiDungRepo.searchKhachHangForPos(keyword.trim());
    }

    public List<NguoiDung> getAllKhachHang() {
        return nguoiDungRepo.findByVaiTro("KHACHHANG");
    }

    @Transactional
    public HoaDon ganKhachHang(Integer idHoaDon, Integer idKhachHang) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        NguoiDung kh = nguoiDungRepo.findById(Long.valueOf(idKhachHang))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        hoaDon.setKhachHang(kh);
        return hoaDonRepo.save(hoaDon);
    }

    @Transactional
    public void capNhatThongTinKhach(Integer idHoaDon,
                                     String hoTen,
                                     String sdt,
                                     String email,
                                     String diaChi) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if ((hoTen == null || hoTen.isBlank())
                && (sdt == null || sdt.isBlank())
                && (email == null || email.isBlank())
                && (diaChi == null || diaChi.isBlank())) {
            return;
        }

        NguoiDung khachHang = hoaDon.getKhachHang();
        if (khachHang != null && "KHACHHANG".equalsIgnoreCase(khachHang.getVaiTro())) {
            if (hoTen != null && !hoTen.isBlank()) khachHang.setHoTen(hoTen);
            if (sdt != null && !sdt.isBlank()) khachHang.setSdt(sdt);
            if (email != null && !email.isBlank()) khachHang.setEmail(email);
            if (diaChi != null && !diaChi.isBlank()) khachHang.setDiaChi(diaChi);
            nguoiDungRepo.save(khachHang);
        }
    }

    // =========================
    // ✅ VALIDATE VOUCHER CHUẨN
    // =========================
    private void validateVoucherStillValidOrThrow(GiamGia voucher) {
        if (voucher == null) return;

        LocalDateTime now = LocalDateTime.now();

        if (voucher.getTrangThai() == null || !voucher.getTrangThai()) {
            throw new RuntimeException("Voucher đang bị khóa hoặc không khả dụng.");
        }

        if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) {
            throw new RuntimeException("Voucher chưa bắt đầu áp dụng.");
        }

        if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) {
            throw new RuntimeException("Voucher đã hết hạn.");
        }

        // soLuong null = không giới hạn (theo logic repo findAllActive)
        if (voucher.getSoLuong() != null && voucher.getSoLuong() <= 0) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng.");
        }
    }

    /**
     * Áp dụng voucher (POS) - bản chuẩn:
     * ✅ check ngày, trạng thái, số lượng
     * ✅ check điều kiện tối thiểu + phạm vi sản phẩm
     * ✅ tính giảm và cập nhật tổng
     */
    @Transactional
    public HoaDon applyVoucher(Integer idHoaDon, String maGiamGia) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // ✅ fetch spct để không bị lazy khi redirect về view
        GiamGia giamGia = giamGiaRepo.findByMaFetchSpct(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        // ✅ validate như client
        validateVoucherStillValidOrThrow(giamGia);

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(idHoaDon);
        if (chiTiets.isEmpty()) throw new RuntimeException("Hóa đơn không có sản phẩm");

        BigDecimal tongTienApDung = calculateApplicableSubtotal(giamGia, chiTiets);

        // Điều kiện đơn tối thiểu (nếu có)
        if (giamGia.getDonHangToiThieu() != null && giamGia.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0) {
            if (tongTienApDung.compareTo(giamGia.getDonHangToiThieu()) < 0) {
                throw new RuntimeException("Mã giảm giá không áp dụng: chưa đạt điều kiện đơn hàng tối thiểu");
            }
        }

        // Voucher áp cho 1 SP nhưng giỏ không có SP đó
        if (giamGia.getSanPhamChiTiet() != null && tongTienApDung.compareTo(BigDecimal.ZERO) == 0) {
            String maSku = (giamGia.getSanPhamChiTiet() != null) ? giamGia.getSanPhamChiTiet().getMaSKU() : null;
            if (maSku != null && !maSku.isBlank()) {
                throw new RuntimeException("Mã giảm giá chỉ áp dụng cho sản phẩm có mã SKU: " + maSku);
            }
            throw new RuntimeException("Mã giảm giá chỉ áp dụng cho một sản phẩm cụ thể trong giỏ hàng");
        }

        BigDecimal tienGiam = calculateDiscountAmount(giamGia, tongTienApDung);

        hoaDon.setGiamGia(giamGia);
        hoaDon.setTienGiamGia(tienGiam);

        updateTongTienHoaDon(hoaDon);
        return hoaDonRepo.save(hoaDon);
    }

    @Transactional
    public HoaDon removeVoucher(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        hoaDon.setGiamGia(null);
        hoaDon.setTienGiamGia(BigDecimal.ZERO);
        updateTongTienHoaDon(hoaDon);
        return hoaDonRepo.save(hoaDon);
    }

    private NguoiDung getCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Không tìm thấy người dùng đăng nhập!");
        }

        String username = auth.getName();

        return nguoiDungRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + username));
    }

    /**
     * ✅ Thanh toán: kiểm tra lại voucher trước khi trừ kho và trừ lượt
     * => chặn trường hợp: apply xong nhưng voucher hết hạn / về 0 vẫn thanh toán được
     */
    @Transactional
    public HoaDon thanhToanHoaDonTaiQuay(Integer idHoaDon,
                                         String tenNguoiNhan, String sdtGiaoHang,
                                         String diaChiGiaoHang, BigDecimal phiShip,
                                         BigDecimal soTienKhachDua) {

        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(idHoaDon);
        if (chiTiets.isEmpty()) throw new RuntimeException("Hóa đơn không có sản phẩm nào");

        NguoiDung nhanVien = hoaDon.getNhanVien();
        if (nhanVien == null) {
            nhanVien = getCurrentNhanVien();
            hoaDon.setNhanVien(nhanVien);
        }

        ThanhToan ttMacDinh = thanhToanRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy 'Hình thức thanh toán' (ID 1)."));
        hoaDon.setThanhToan(ttMacDinh);

        NguoiDung kh = hoaDon.getKhachHang();
        if ((tenNguoiNhan == null || tenNguoiNhan.isBlank()) && kh != null) tenNguoiNhan = kh.getHoTen();
        if ((sdtGiaoHang == null || sdtGiaoHang.isBlank()) && kh != null) sdtGiaoHang = kh.getSdt();
        if ((diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) && kh != null) diaChiGiaoHang = kh.getDiaChi();

        hoaDon.setTenNguoiNhan(tenNguoiNhan);
        hoaDon.setSdt(sdtGiaoHang);
        hoaDon.setDiaChi(diaChiGiaoHang);
        hoaDon.setPhiShip(phiShip != null ? phiShip : BigDecimal.ZERO);

        // ✅ Re-check voucher tại thời điểm thanh toán (nạp mới từ DB cho chắc)
        if (hoaDon.getGiamGia() != null) {
            GiamGia fresh = giamGiaRepo.findById(hoaDon.getGiamGia().getId())
                    .orElseThrow(() -> new RuntimeException("Voucher không còn tồn tại."));
            validateVoucherStillValidOrThrow(fresh);
            hoaDon.setGiamGia(fresh); // gắn lại bản fresh
        }

        // cập nhật tiền (tính lại giảm nếu voucher tồn tại)
        updateTongTienHoaDon(hoaDon);

        // Tiền khách đưa
        if (soTienKhachDua != null) {
            BigDecimal tong = hoaDon.getTongThanhToan();
            hoaDon.setSoTienKhachDua(soTienKhachDua);
            hoaDon.setSoTienTraLai(soTienKhachDua.subtract(tong).max(BigDecimal.ZERO));
        }

        // Trừ tồn kho
        for (HoaDonChiTiet hdct : chiTiets) {
            SanPhamChiTiet spct = hdct.getSanPhamChiTiet();

            if (spct.getTrangThai() == null || !spct.getTrangThai()) {
                throw new RuntimeException("Sản phẩm "
                        + spct.getSanPham().getTenNuocHoa()
                        + " đã ngừng kinh doanh, không thể thanh toán hóa đơn này");
            }

            int tonMoi = spct.getSoLuongTon() - hdct.getSoLuong();
            if (tonMoi < 0) throw new RuntimeException("Hết hàng: " + spct.getSanPham().getTenNuocHoa());
            spct.setSoLuongTon(tonMoi);
            sanPhamChiTietRepo.save(spct);
        }

        // Trừ lượt voucher (nếu có) – phải check lại lần nữa để tránh âm
        if (hoaDon.getGiamGia() != null) {
            GiamGia voucher = giamGiaRepo.findById(hoaDon.getGiamGia().getId())
                    .orElseThrow(() -> new RuntimeException("Voucher không còn tồn tại."));

            validateVoucherStillValidOrThrow(voucher);

            if (voucher.getSoLuong() != null) {
                if (voucher.getSoLuong() <= 0) {
                    throw new RuntimeException("Voucher đã hết lượt sử dụng.");
                }
                voucher.setSoLuong(voucher.getSoLuong() - 1);
                giamGiaRepo.save(voucher);
            }
        }

        hoaDon.setTrangThai("HOAN_THANH");
        hoaDon.setNgayThanhToan(LocalDateTime.now());

        return hoaDonRepo.save(hoaDon);
    }

    private void updateTongTienHoaDon(HoaDon hoaDon) {
        if (hoaDon == null) return;

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(hoaDon.getId());

        BigDecimal tongTienHang = chiTiets.stream()
                .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienHang(tongTienHang);

        BigDecimal phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : BigDecimal.ZERO;

        BigDecimal tienGiamGia = BigDecimal.ZERO;

        if (hoaDon.getGiamGia() != null) {
            GiamGia gg = hoaDon.getGiamGia();

            // ✅ Nếu voucher bỗng dưng invalid (hết hạn / về 0 / bị khóa) => không áp nữa
            try {
                validateVoucherStillValidOrThrow(gg);
            } catch (RuntimeException ex) {
                hoaDon.setGiamGia(null);
                hoaDon.setTienGiamGia(BigDecimal.ZERO);

                BigDecimal tongThanhToan = tongTienHang.add(phiShip).max(BigDecimal.ZERO);
                hoaDon.setTongThanhToan(tongThanhToan);
                hoaDonRepo.save(hoaDon);
                return;
            }

            BigDecimal tongTienApDung = calculateApplicableSubtotal(gg, chiTiets);

            if (gg.getDonHangToiThieu() != null && gg.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0
                    && tongTienApDung.compareTo(gg.getDonHangToiThieu()) < 0) {
                tienGiamGia = BigDecimal.ZERO; // giữ voucher nhưng không áp tiền giảm
            } else {
                tienGiamGia = calculateDiscountAmount(gg, tongTienApDung);
            }
        } else {
            tienGiamGia = (hoaDon.getTienGiamGia() != null) ? hoaDon.getTienGiamGia() : BigDecimal.ZERO;
        }

        if (tienGiamGia.compareTo(tongTienHang) > 0) tienGiamGia = tongTienHang;

        hoaDon.setTienGiamGia(tienGiamGia);

        BigDecimal tongThanhToan = tongTienHang.add(phiShip).subtract(tienGiamGia).max(BigDecimal.ZERO);
        hoaDon.setTongThanhToan(tongThanhToan);

        hoaDonRepo.save(hoaDon);
    }

    private BigDecimal calculateApplicableSubtotal(GiamGia giamGia, List<HoaDonChiTiet> chiTiets) {
        if (giamGia == null) return BigDecimal.ZERO;

        if (giamGia.getSanPhamChiTiet() != null) {
            Integer spId = giamGia.getSanPhamChiTiet().getId();
            return chiTiets.stream()
                    .filter(h -> h.getSanPhamChiTiet() != null && h.getSanPhamChiTiet().getId().equals(spId))
                    .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            return chiTiets.stream()
                    .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private BigDecimal calculateDiscountAmount(GiamGia giamGia, BigDecimal subtotalApDung) {
        if (giamGia == null || subtotalApDung == null) return BigDecimal.ZERO;

        BigDecimal result = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            if (giamGia.getGiaTri() == null) return BigDecimal.ZERO;
            result = subtotalApDung.multiply(giamGia.getGiaTri().divide(BigDecimal.valueOf(100)));
        } else if ("AMOUNT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            result = (giamGia.getGiaTri() != null) ? giamGia.getGiaTri() : BigDecimal.ZERO;
        } else {
            return BigDecimal.ZERO;
        }

        if (giamGia.getGiamToiDa() != null && giamGia.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
            result = result.min(giamGia.getGiamToiDa());
        }

        if (result.compareTo(subtotalApDung) > 0) result = subtotalApDung;

        return result.max(BigDecimal.ZERO);
    }

    public Map<SanPham, List<SanPhamChiTiet>> getGroupedSanPham() {
        List<SanPhamChiTiet> allSPCT = sanPhamChiTietRepo.findAllWithSanPham();
        return allSPCT.stream()
                .collect(Collectors.groupingBy(
                        SanPhamChiTiet::getSanPham,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    // ✅ Quan trọng: dùng findAllActiveFetchSpct để không lazy khi render datalist
    @Transactional(readOnly = true)
    public List<GiamGia> findVoucherPhuHopChoHoaDon(HoaDon hoaDon, List<HoaDonChiTiet> chiTiets) {
        if (hoaDon == null || chiTiets == null || chiTiets.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();
        List<GiamGia> allActive = giamGiaRepo.findAllActiveFetchSpct(now);

        return allActive.stream()
                .filter(v -> isVoucherApDungChoGioHang(v, chiTiets))
                .toList();
    }

    private boolean isVoucherApDungChoGioHang(GiamGia giamGia, List<HoaDonChiTiet> chiTiets) {
        BigDecimal tongTienApDung = calculateApplicableSubtotal(giamGia, chiTiets);
        if (tongTienApDung.compareTo(BigDecimal.ZERO) <= 0) return false;

        if (giamGia.getDonHangToiThieu() != null
                && giamGia.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0) {
            if (tongTienApDung.compareTo(giamGia.getDonHangToiThieu()) < 0) return false;
        }

        return true;
    }
}
