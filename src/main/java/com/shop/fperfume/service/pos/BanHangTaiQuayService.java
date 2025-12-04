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
 * - Tính tiền, áp voucher đúng phạm vi (SP / toàn cửa hàng)
 * - Kiểm tra điều kiện đơn hàng tối thiểu
 * - Áp giảm PERCENT / AMOUNT đúng
 * - Giảm không vượt quá GiamToiDa (nếu có) và không vượt quá phần áp dụng
 */
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

        // ✅ CHẶN SẢN PHẨM NGỪNG KINH DOANH
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

    /**
     * Áp dụng mã giảm giá cho hóa đơn - kiểm tra điều kiện trước khi gán
     * -> Nếu không thỏa, ném RuntimeException với thông báo rõ ràng
     */
    @Transactional
    public HoaDon applyVoucher(Integer idHoaDon, String maGiamGia) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        GiamGia giamGia = giamGiaRepo.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        // Lấy chi tiết hiện tại
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(idHoaDon);
        if (chiTiets.isEmpty()) throw new RuntimeException("Hóa đơn không có sản phẩm");

        // Tính tổng tiền áp dụng theo phạm vi voucher
        BigDecimal tongTienApDung = calculateApplicableSubtotal(giamGia, chiTiets);

        // Kiểm tra điều kiện đơn hàng tối thiểu (nếu có)
        if (giamGia.getDonHangToiThieu() != null && giamGia.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0) {
            if (tongTienApDung.compareTo(giamGia.getDonHangToiThieu()) < 0) {
                throw new RuntimeException("Mã giảm giá không áp dụng: chưa đạt điều kiện đơn hàng tối thiểu");
            }
        }

        // Nếu voucher áp cho 1 sản phẩm nhưng giỏ không có sản phẩm đó -> báo lỗi
        if (giamGia.getSanPhamChiTiet() != null && tongTienApDung.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Mã giảm giá không áp dụng cho sản phẩm này");
        }

        // Tính tiền giảm chính xác (theo phạm vi)
        BigDecimal tienGiam = calculateDiscountAmount(giamGia, tongTienApDung);

        // Gán voucher và tienGiamGia lên hóa đơn
        hoaDon.setGiamGia(giamGia);
        hoaDon.setTienGiamGia(tienGiam);

        // Cập nhật tổng tiền (hàm sẽ dùng hoaDon.getGiamGia() hoặc hoaDon.getTienGiamGia())
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

    @Transactional
    public HoaDon thanhToanHoaDonTaiQuay(Integer idHoaDon,
                                         String tenNguoiNhan, String sdtGiaoHang,
                                         String diaChiGiaoHang, BigDecimal phiShip,
                                         BigDecimal soTienKhachDua) {

        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(idHoaDon);
        if (chiTiets.isEmpty()) throw new RuntimeException("Hóa đơn không có sản phẩm nào");

        NguoiDung nhanVien = nguoiDungRepo.findById(2L)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân Viên (ID 2)."));
        hoaDon.setNhanVien(nhanVien);

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

        // cập nhật tiền (sẽ tính lại giảm nếu voucher tồn tại)
        updateTongTienHoaDon(hoaDon);

        // Xử lý tiền khách đưa
        if (soTienKhachDua != null) {
            BigDecimal tong = hoaDon.getTongThanhToan();
            hoaDon.setSoTienKhachDua(soTienKhachDua);
            hoaDon.setSoTienTraLai(soTienKhachDua.subtract(tong).max(BigDecimal.ZERO));
        }

        // Trừ tồn kho
        for (HoaDonChiTiet hdct : chiTiets) {
            SanPhamChiTiet spct = hdct.getSanPhamChiTiet();

            // CHẶN HÓA ĐƠN CÓ SẢN PHẨM NGỪNG KINH DOANH
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

        hoaDon.setTrangThai("HOAN_THANH");
        hoaDon.setNgayThanhToan(LocalDateTime.now());

        return hoaDonRepo.save(hoaDon);
    }

    /**
     * Tính và cập nhật lại tổng tiền cho hóa đơn.
     * - Tính tongTienHang từ chi tiết
     * - Lấy phiShip
     * - Nếu hoaDon.getGiamGia() != null => tính tienGiamGia đúng phạm vi bằng helper
     * - Cập nhật hoaDon.tongThanhToan = tongTienHang + phiShip - tienGiamGia (không âm)
     */
    private void updateTongTienHoaDon(HoaDon hoaDon) {
        if (hoaDon == null) return;

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(hoaDon.getId());

        BigDecimal tongTienHang = chiTiets.stream()
                .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienHang(tongTienHang);

        BigDecimal phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : BigDecimal.ZERO;

        BigDecimal tienGiamGia = BigDecimal.ZERO;
        // Nếu có voucher gán trên hoadon -> tính lại dựa trên chi tiết hiện tại
        if (hoaDon.getGiamGia() != null) {
            GiamGia gg = hoaDon.getGiamGia();
            BigDecimal tongTienApDung = calculateApplicableSubtotal(gg, chiTiets);

            // Kiểm tra điều kiện tối thiểu (nếu không đủ thì không áp)
            if (gg.getDonHangToiThieu() != null && gg.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0
                    && tongTienApDung.compareTo(gg.getDonHangToiThieu()) < 0) {
                tienGiamGia = BigDecimal.ZERO;
                // giữ voucher nhưng không áp tiền giảm
            } else {
                tienGiamGia = calculateDiscountAmount(gg, tongTienApDung);
            }
        } else {
            // fallback nếu trước đó có set trực tiếp tienGiamGia
            tienGiamGia = (hoaDon.getTienGiamGia() != null) ? hoaDon.getTienGiamGia() : BigDecimal.ZERO;
        }

        // không cho tiền giảm vượt quá tổng áp dụng hoặc tổng hàng
        if (tienGiamGia.compareTo(tongTienHang) > 0) tienGiamGia = tongTienHang;
        hoaDon.setTienGiamGia(tienGiamGia);

        BigDecimal tongThanhToan = tongTienHang.add(phiShip).subtract(tienGiamGia).max(BigDecimal.ZERO);
        hoaDon.setTongThanhToan(tongThanhToan);

        hoaDonRepo.save(hoaDon);
    }

    /**
     * Tính tổng tiền "áp dụng" theo phạm vi giảm của GiamGia:
     * - Nếu giamGia.sanPhamChiTiet != null => chỉ cộng các item có id = sanPhamChiTiet.id
     * - Ngược lại => trả về tổng giỏ (tất cả)
     */
    private BigDecimal calculateApplicableSubtotal(GiamGia giamGia, List<HoaDonChiTiet> chiTiets) {
        if (giamGia == null) return BigDecimal.ZERO;

        if (giamGia.getSanPhamChiTiet() != null) {
            Integer spId = giamGia.getSanPhamChiTiet().getId();
            return chiTiets.stream()
                    .filter(h -> h.getSanPhamChiTiet() != null && h.getSanPhamChiTiet().getId().equals(spId))
                    .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Toàn cửa hàng
            return chiTiets.stream()
                    .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Tính số tiền giảm thực tế dựa trên loại giảm và cap GiamToiDa:
     * - PERCENT: subtotal * (giaTri / 100)
     * - AMOUNT: giaTri (cố định)
     * Sau đó:
     * - nếu giamToiDa != null -> cap = min(cap,giamToiDa)
     * - trả về min(result, subtotal) (không vượt quá phần áp dụng)
     */
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

        // Áp cap GiamToiDa nếu có
        if (giamGia.getGiamToiDa() != null && giamGia.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
            result = result.min(giamGia.getGiamToiDa());
        }

        // Không cho vượt quá subtotal áp dụng
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
}
