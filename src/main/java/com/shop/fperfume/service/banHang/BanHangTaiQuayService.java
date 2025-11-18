package com.shop.fperfume.service.banHang;

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
        return nguoiDungRepo.findBySdtContainingOrHoTenContaining(keyword, keyword);
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
     * Áp dụng mã giảm giá.
     */
    @Transactional
    public HoaDon applyVoucher(Integer idHoaDon, String maGiamGia) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        GiamGia giamGia = giamGiaRepo.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        // (Bạn có thể thêm logic kiểm tra mã ở đây)
        // ...

        hoaDon.setGiamGia(giamGia);
        hoaDon.setTienGiamGia(giamGia.getGiaTri()); // Giả sử GiamGia có getGiaTriGiam()

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
     * Logic thanh toán (fix cứng khách hàng/nhân viên).
     */
    @Transactional
    public HoaDon thanhToanHoaDonTaiQuay(Integer idHoaDon,
                                         String tenNguoiNhan, String sdtGiaoHang,
                                         String diaChiGiaoHang, BigDecimal phiShip) {

        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepo.findByHoaDon_Id(idHoaDon);

        if (chiTiets.isEmpty()) {
            throw new RuntimeException("Hóa đơn không có sản phẩm nào");
        }

        // --- XÓA BỎ FIX CỨNG - THAY BẰNG LOGIC MỚI ---

        // 1. Gán nhân viên (Vẫn fix cứng ID 2 làm nhân viên chốt đơn)
        NguoiDung nhanVien = nguoiDungRepo.findById(2L) // ID 2 = Nhân viên
                .orElseThrow(() -> new RuntimeException("LỖI FIX CỨNG: Không tìm thấy Nhân Viên (ID 2)."));
        hoaDon.setNhanVien(nhanVien);

        // 2. Gán Khách Lẻ (ID 1) NẾU chưa chọn khách hàng nào
        if (hoaDon.getKhachHang() == null) {
            NguoiDung khachLe = nguoiDungRepo.findById(1L) // ID 1 = Khách Lẻ
                    .orElseThrow(() -> new RuntimeException("LỖI FIX CỨNG: Không tìm thấy Khách Lẻ (ID 1)."));
            hoaDon.setKhachHang(khachLe);
        }

        // 3. Gán Hình thức thanh toán (ID 1, ví dụ: Tiền mặt)
        ThanhToan ttMacDinh = thanhToanRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("LỖI CẤU HÌNH: Không tìm thấy 'Hình thức thanh toán' (ID 1)."));
        hoaDon.setThanhToan(ttMacDinh);

        // 4. LƯU THÔNG TIN GIAO HÀNG (NẾU CÓ)
        if (tenNguoiNhan != null && !tenNguoiNhan.isEmpty()) {
            hoaDon.setTenNguoiNhan(tenNguoiNhan);
            hoaDon.setSdt(sdtGiaoHang);
            hoaDon.setDiaChi(diaChiGiaoHang);
            hoaDon.setPhiShip(phiShip != null ? phiShip : BigDecimal.ZERO);
            // Cập nhật lại tổng tiền lần cuối nếu có phí ship
            updateTongTienHoaDon(hoaDon);
        }

        // --- KẾT THÚC LOGIC MỚI ---

        // 5. Trừ số lượng tồn kho
        for (HoaDonChiTiet hdct : chiTiets) {
            SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
            int tonKhoMoi = spct.getSoLuongTon() - hdct.getSoLuong();
            if (tonKhoMoi < 0) {
                throw new RuntimeException("Hết hàng: " + spct.getSanPham().getTenNuocHoa());
            }
            spct.setSoLuongTon(tonKhoMoi);
            sanPhamChiTietRepo.save(spct);
        }

        // 6. Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThai("Đã thanh toán"); // 1 = Đã thanh toán
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
        // Gộp theo SanPham (sản phẩm chính)
        Map<SanPham, List<SanPhamChiTiet>> groupedProducts = allSPCT.stream()
                .collect(Collectors.groupingBy(
                        SanPhamChiTiet::getSanPham,  // Key là SanPham
                        LinkedHashMap::new,           // Giữ thứ tự
                        Collectors.toList()           // Value là List<SanPhamChiTiet>
                ));

        return groupedProducts;
    }
}