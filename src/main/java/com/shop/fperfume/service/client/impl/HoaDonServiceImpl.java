package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.dto.CheckoutRequestDTO;
import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.service.client.HoaDonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Refactored HoaDonServiceImpl
 * - Kiểm tra voucher, phạm vi áp dụng, đơn hàng tối thiểu, cap giảm
 * - Nếu phương thức thanh toán là "vnpay" -> tạo đơn ở trạng thái chờ thanh toán, KHÔNG trừ kho, KHÔNG giảm lượt voucher
 * - Ngược lại (COD/khác) -> trừ kho & giảm lượt voucher khi tạo đơn
 * - Toàn bộ createOrder chạy trong transaction -> rollback nếu lỗi xảy ra
 */
@Service
public class HoaDonServiceImpl implements HoaDonClientService {

    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private ThanhToanRepository thanhToanRepo;
    @Autowired private GioHangClientService gioHangClientService;
    @Autowired private GiamGiaRepository giamGiaRepository; // cần để giảm số lượt khi thanh toán ngay

    // =========================
    //        TẠO ĐƠN HÀNG
    // =========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrder(GioHang gioHang, NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {

        // Validate đầu vào
        if (gioHang == null) throw new RuntimeException("Giỏ hàng không tồn tại.");
        Collection<GioHangChiTiet> cartItems = Optional.ofNullable(gioHang.getGioHangChiTiets()).orElse(Collections.emptyList());
        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng trống! Không thể đặt hàng.");

        ThanhToan phuongThucThanhToan = thanhToanRepo.findById(checkoutInfo.getIdThanhToan())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ."));

        GiamGia giamGia = gioHang.getGiamGia();

        // Kiểm tra voucher (nếu có) cơ bản: tồn tại, active, ngày, số lượng > 0
        if (giamGia != null) {
            if (giamGia.getNgayBatDau() != null && LocalDateTime.now().isBefore(giamGia.getNgayBatDau())) {
                throw new RuntimeException("Voucher chưa bắt đầu áp dụng.");
            }
            if (giamGia.getNgayKetThuc() != null && LocalDateTime.now().isAfter(giamGia.getNgayKetThuc())) {
                throw new RuntimeException("Voucher đã hết hạn.");
            }
            if (giamGia.getTrangThai() == null || !giamGia.getTrangThai()) {
                throw new RuntimeException("Voucher không khả dụng.");
            }
            if (giamGia.getSoLuong() != null && giamGia.getSoLuong() <= 0) {
                // nếu bạn muốn cho phép tạo đơn chờ thanh toán mà vẫn giữ voucher, bạn có thể relax ở đây.
                throw new RuntimeException("Voucher đã hết lượt sử dụng.");
            }
        }

        // Tạo đối tượng hóa đơn (chưa lưu)
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTenNguoiNhan(checkoutInfo.getTenNguoiNhan());
        hoaDon.setDiaChi(checkoutInfo.getDiaChi());
        hoaDon.setSdt(checkoutInfo.getSdt());
        hoaDon.setGhiChu(checkoutInfo.getGhiChu());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setThanhToan(phuongThucThanhToan);
        hoaDon.setPhiShip(new BigDecimal(30000));
        hoaDon.setKenhBan("WEB");
        hoaDon.setMa("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        boolean isPendingPayment = phuongThucThanhToan.getHinhThucThanhToan() != null
                && phuongThucThanhToan.getHinhThucThanhToan().toLowerCase().contains("vnpay");

        // Set trạng thái ban đầu
        hoaDon.setTrangThai(isPendingPayment ? "DANG_CHO_THANH_TOAN" : "CHO_XAC_NHAN");

        // Tạo chi tiết hóa đơn (nhưng chỉ trừ kho nếu không phải pending payment)
        BigDecimal tongTienHang = BigDecimal.ZERO;
        List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

        // Trước hết kiểm tra tồn kho cho tất cả item (không trừ), để đảm bảo atomic
        for (GioHangChiTiet item : cartItems) {
            Integer spctId = item.getSanPhamChiTiet().getId();
            SanPhamChiTiet spct = sanPhamChiTietRepo.findByIdAndTrangThaiTrue(spctId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm đã ngừng kinh doanh hoặc không tồn tại"));

            int soLuongTrongGio = item.getSoLuong() != null ? item.getSoLuong() : 0;
            if (soLuongTrongGio <= 0) throw new RuntimeException("Số lượng không hợp lệ cho sản phẩm: " + getTenSanPhamSafe(spct));

            // Kiểm tra tồn (chỉ kiểm tra, nếu là pending payment vẫn phải kiểm tra)
            int ton = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            if (ton < soLuongTrongGio) {
                throw new RuntimeException("Sản phẩm " + getTenSanPhamSafe(spct) + " không đủ số lượng (tồn: " + ton + ").");
            }
        }

        // Nếu không pending payment -> THỰC HIỆN TRỪ KHO ngay khi tạo đơn (để tránh sell-through)
        // Nếu pending -> chỉ tạo chi tiết mà không trừ kho ở đây
        for (GioHangChiTiet item : cartItems) {
            SanPhamChiTiet spct = sanPhamChiTietRepo.findByIdAndTrangThaiTrue(item.getSanPhamChiTiet().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm đã ngừng kinh doanh hoặc không tồn tại"));

            int qty = item.getSoLuong();
            // Trừ kho ngay nếu cần
            if (!isPendingPayment) {
                int newTon = spct.getSoLuongTon() - qty;
                if (newTon < 0) {
                    throw new RuntimeException("Sản phẩm " + getTenSanPhamSafe(spct) + " không đủ tồn khi trừ kho.");
                }
                spct.setSoLuongTon(newTon);
                sanPhamChiTietRepo.save(spct);
            }

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(qty);
            hdct.setDonGia(spct.getGiaBan());
            hdct.setNgayTao(LocalDateTime.now());
            hoaDonChiTiets.add(hdct);

            tongTienHang = tongTienHang.add(spct.getGiaBan().multiply(BigDecimal.valueOf(qty)));
        }
        hoaDon.setTongTienHang(tongTienHang);

        // Tính giảm giá chuẩn theo phạm vi
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        if (giamGia != null) {
            // Tính tổng áp dụng (nếu voucher áp cho 1 sản phẩm -> chỉ phần đó, ngược lại toàn giỏ)
            BigDecimal subtotalApDung = calculateApplicableSubtotalForGiamGia(giamGia, hoaDonChiTiets);

            // Kiểm tra điều kiện đơn hàng tối thiểu (nếu có)
            if (giamGia.getDonHangToiThieu() != null
                    && giamGia.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0
                    && subtotalApDung.compareTo(giamGia.getDonHangToiThieu()) < 0) {
                // Không đạt điều kiện -> không áp dụng giảm
                tienGiamGia = BigDecimal.ZERO;
            } else {
                // Tính giảm (PERCENT/AMOUNT), áp cap giamToiDa, không vượt quá phần áp dụng
                tienGiamGia = calculateDiscountAmountForGiamGia(giamGia, subtotalApDung);
            }

            // Gán giamGia lên hoaDon (luôn gán để hiển thị mã trên đơn)
            hoaDon.setGiamGia(giamGia);
        }
        hoaDon.setTienGiamGia(tienGiamGia);

        // Tổng thanh toán
        BigDecimal tongThanhToan = tongTienHang.subtract(tienGiamGia).add(hoaDon.getPhiShip());
        hoaDon.setTongThanhToan(tongThanhToan.max(BigDecimal.ZERO));

        // Attach chi tiết và lưu
        hoaDon.setHoaDonChiTiets(hoaDonChiTiets);
        HoaDon savedHoaDon = hoaDonRepo.save(hoaDon);

        // Nếu không đang chờ thanh toán (ví dụ COD, hoặc thanh toán ngay) -> giảm lượt voucher (nếu có)
        if (!isPendingPayment && giamGia != null) {
            // Tăng an toàn: kiểm tra lại số lượng voucher trước khi giảm (đề phòng race)
            GiamGia gg = giamGiaRepository.findById(giamGia.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher khi xác nhận đơn"));
            if (gg.getSoLuong() != null && gg.getSoLuong() > 0) {
                gg.setSoLuong(gg.getSoLuong() - 1);
                giamGiaRepository.save(gg);
            } else {
                // Nếu voucher hết (có thể bị dùng ở 1 order khác), rollback để thông báo
                throw new RuntimeException("Voucher đã hết lượt sử dụng lúc xác nhận đơn. Vui lòng thử lại.");
            }
        }

        // Xóa giỏ hàng nếu là user đăng nhập
        if (khachHang != null) {
            gioHangClientService.clearCart(khachHang);
        }

        return savedHoaDon;
    }

    // =========================
    //    Helper methods
    // =========================

    // An toàn lấy tên sản phẩm
    private String getTenSanPhamSafe(SanPhamChiTiet spct) {
        try {
            return (spct.getSanPham() != null && spct.getSanPham().getTenNuocHoa() != null)
                    ? spct.getSanPham().getTenNuocHoa()
                    : ("SPCT#" + spct.getId());
        } catch (Exception e) {
            return "SPCT#" + spct.getId();
        }
    }

    /**
     * Tính tổng tiền áp dụng cho voucher:
     * - Nếu voucher.getSanPhamChiTiet() != null -> chỉ lấy các hdct có cùng id spct
     * - Ngược lại -> tổng cả giỏ (sum donGia * soLuong)
     */
    private BigDecimal calculateApplicableSubtotalForGiamGia(GiamGia giamGia, List<HoaDonChiTiet> hdcts) {
        if (giamGia == null) return BigDecimal.ZERO;
        if (hdcts == null || hdcts.isEmpty()) return BigDecimal.ZERO;

        if (giamGia.getSanPhamChiTiet() != null) {
            Integer spId = giamGia.getSanPhamChiTiet().getId();
            return hdcts.stream()
                    .filter(h -> h.getSanPhamChiTiet() != null && Objects.equals(h.getSanPhamChiTiet().getId(), spId))
                    .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            return hdcts.stream()
                    .map(h -> h.getDonGia().multiply(BigDecimal.valueOf(h.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Tính tiền giảm thực tế:
     * - PERCENT: subtotal * (giaTri / 100)
     * - AMOUNT: giaTri (cố định)
     * Sau đó áp giamToiDa nếu có và đảm bảo <= subtotalApDung
     */
    private BigDecimal calculateDiscountAmountForGiamGia(GiamGia giamGia, BigDecimal subtotalApDung) {
        if (giamGia == null || subtotalApDung == null) return BigDecimal.ZERO;

        BigDecimal result = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            if (giamGia.getGiaTri() == null) return BigDecimal.ZERO;
            result = subtotalApDung.multiply(giamGia.getGiaTri().divide(BigDecimal.valueOf(100)));
        } else if ("AMOUNT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            result = giamGia.getGiaTri() != null ? giamGia.getGiaTri() : BigDecimal.ZERO;
        } else {
            return BigDecimal.ZERO;
        }

        // Áp cap GiamToiDa nếu có
        if (giamGia.getGiamToiDa() != null && giamGia.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
            result = result.min(giamGia.getGiamToiDa());
        }

        // Không vượt quá phần áp dụng
        if (result.compareTo(subtotalApDung) > 0) result = subtotalApDung;

        return result.max(BigDecimal.ZERO);
    }

    // =========================
    //  Các method khác (giữ nguyên)
    // =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {
        GioHang gioHang = gioHangRepo.findByKhachHang(khachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng."));
        return this.createOrder(gioHang, khachHang, checkoutInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDon> getOrdersByUser(NguoiDung khachHang, String keyword, String fromDateStr, String toDateStr) {
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        try {
            if (fromDateStr != null && !fromDateStr.isEmpty()) {
                fromDate = LocalDate.parse(fromDateStr).atStartOfDay();
            }
            if (toDateStr != null && !toDateStr.isEmpty()) {
                toDate = LocalDate.parse(toDateStr).atTime(23, 59, 59);
            }
        } catch (Exception ignored) {}

        return hoaDonRepo.findHistory(khachHang, keyword, fromDate, toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDon getOrderDetailForUser(Integer hoaDonId, NguoiDung khachHang) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (hoaDon.getKhachHang() == null || !hoaDon.getKhachHang().getId().equals(khachHang.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập đơn hàng này.");
        }
        return hoaDon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Integer hoaDonId, NguoiDung khachHang, String lyDoHuy) {
        HoaDon hoaDon = getOrderDetailForUser(hoaDonId, khachHang);

        String trangThai = hoaDon.getTrangThai();
        if ("CHO_XAC_NHAN".equals(trangThai) || "DANG_CHO_THANH_TOAN".equals(trangThai)) {

            hoaDon.setTrangThai("DA_HUY");

            // Nối lý do hủy vào Ghi chú
            String ghiChuCu = hoaDon.getGhiChu() == null ? "" : hoaDon.getGhiChu();
            String ghiChuMoi = ghiChuCu + " | [Khách hủy: " + lyDoHuy + "]";
            if (ghiChuMoi.length() > 255) ghiChuMoi = ghiChuMoi.substring(0, 255);
            hoaDon.setGhiChu(ghiChuMoi);

            // Nếu đơn chưa trừ kho thì không cần hoàn kho.
            // Nếu bạn trừ kho ngay khi tạo đơn (không pending), thì ở đây cần hoàn kho:
            if (!"DANG_CHO_THANH_TOAN".equals(trangThai)) {
                for (HoaDonChiTiet item : hoaDon.getHoaDonChiTiets()) {
                    SanPhamChiTiet spct = item.getSanPhamChiTiet();
                    spct.setSoLuongTon(spct.getSoLuongTon() + item.getSoLuong());
                    sanPhamChiTietRepo.save(spct);
                }
            }

            // Nếu bạn đã giảm lượt voucher khi tạo và muốn hoàn voucher khi hủy => xử lý ở đây (tuỳ business)
            hoaDonRepo.save(hoaDon);

        } else {
            throw new RuntimeException("Đơn hàng đang được xử lý hoặc đã giao, không thể hủy.");
        }
    }
}
