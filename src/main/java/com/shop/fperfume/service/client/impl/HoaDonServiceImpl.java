package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.dto.CheckoutRequestDTO;
import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.service.client.CartHelperService;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.service.client.HoaDonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HoaDonServiceImpl implements HoaDonClientService {

    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private ThanhToanRepository thanhToanRepo;
    @Autowired private GioHangClientService gioHangClientService;
    @Autowired private GiamGiaRepository giamGiaRepository; // cần để giảm số lượt khi thanh toán ngay
    @Autowired private CartHelperService cartHelperService;
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepo;

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

        // LƯU EMAIL VÀO HÓA ĐƠN
        String email = null;

        // Ưu tiên email khách nhập ở form checkout
        if (checkoutInfo.getEmail() != null && !checkoutInfo.getEmail().isBlank()) {
            email = checkoutInfo.getEmail().trim();
        }
        // Nếu form không có email mà khách đã đăng nhập → fallback lấy email tài khoản
        else if (khachHang != null
                && khachHang.getEmail() != null
                && !khachHang.getEmail().isBlank()) {
            email = khachHang.getEmail().trim();
        }

        hoaDon.setEmail(email);

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

        // Nếu không pending payment -> THỰC HIỆN TRỪ KHO ngay khi tạo đơn (COD)
        // Nếu pending (VNPay) -> chỉ tạo chi tiết, KHÔNG trừ kho ở đây
        // Nếu không pending payment -> TRỪ KHO NGAY
        // Nếu pending (VNPay) -> CHƯA trừ kho, chỉ tạo chi tiết
        for (GioHangChiTiet item : cartItems) {
            SanPhamChiTiet spct = sanPhamChiTietRepo.findByIdAndTrangThaiTrue(item.getSanPhamChiTiet().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm đã ngừng kinh doanh hoặc không tồn tại"));

            int qty = item.getSoLuong();

            // 🔹 COD, chuyển khoản thường... => trừ kho ngay
            if (!isPendingPayment) {
                int tonCu = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                int newTon = tonCu - qty;
                if (newTon < 0) {
                    throw new RuntimeException("Sản phẩm " + getTenSanPhamSafe(spct) + " không đủ tồn khi trừ kho.");
                }
                spct.setSoLuongTon(newTon);
                sanPhamChiTietRepo.save(spct);
            }
            // 🔹 VNPay: KHÔNG trừ kho ở đây, để dành sang lúc VNPay báo thành công

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


// Gán voucher lên hóa đơn (nếu có) để lưu
        if (giamGia != null) {
            hoaDon.setGiamGia(giamGia);
        }

// 👉 Dùng lại logic tính tiền của CartHelperService cho GIỎ HÀNG
        Map<String, Object> cartData = cartHelperService.calculateCartData(gioHang);
        BigDecimal tongTienHangCart   = (BigDecimal) cartData.get("tongTienHang");
        BigDecimal tienGiamGiaCart    = (BigDecimal) cartData.get("tienGiamGia");
        BigDecimal tongThanhToanCart  = (BigDecimal) cartData.get("tongThanhToan");

// (Nếu cẩn thận) bạn có thể log/so sánh:
        if (tongTienHangCart.compareTo(tongTienHang) != 0) {
            System.out.println("⚠ WARNING: tongTienHangCart != tongTienHang trong createOrder");
        }

// Set lên hóa đơn theo đúng số đã dùng ở giỏ hàng/checkout
        hoaDon.setTienGiamGia(tienGiamGiaCart);

// tongThanhToanCart hiện mới là: tổng hàng - giảm giá
// => Cộng thêm phí ship
        BigDecimal tongThanhToan = tongThanhToanCart.add(hoaDon.getPhiShip());
        hoaDon.setTongThanhToan(tongThanhToan.max(BigDecimal.ZERO));


        hoaDon.setHoaDonChiTiets(hoaDonChiTiets);
        HoaDon savedHoaDon = hoaDonRepo.save(hoaDon);

        if (khachHang != null) {
            gioHang.setGiamGia(null);      // xóa idGiamGia trong giỏ
            gioHangRepo.save(gioHang);
        }

        // Nếu không đang chờ thanh toán (ví dụ COD) -> giảm lượt voucher (nếu có)
        if (!isPendingPayment && giamGia != null) {
            GiamGia gg = giamGiaRepository.findById(giamGia.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher khi xác nhận đơn"));
            if (gg.getSoLuong() != null && gg.getSoLuong() > 0) {
                gg.setSoLuong(gg.getSoLuong() - 1);
                giamGiaRepository.save(gg);
            } else {
                throw new RuntimeException("Voucher đã hết lượt sử dụng lúc xác nhận đơn. Vui lòng thử lại.");
            }
        }

        // Xóa giỏ hàng nếu là user đăng nhập (CHỈ cho COD)
        if (!isPendingPayment && khachHang != null) {
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
        // Lấy đơn, đồng thời kiểm tra quyền sở hữu
        HoaDon hoaDon = getOrderDetailForUser(hoaDonId, khachHang);

        String old = hoaDon.getTrangThai() == null ? "" : hoaDon.getTrangThai().trim().toUpperCase();

// Chỉ cho huỷ khi đơn còn ở 2 trạng thái này
        if (!"CHO_XAC_NHAN".equals(old) && !"DANG_CHO_THANH_TOAN".equals(old)) {
            throw new RuntimeException("Đơn hàng đang được xử lý hoặc đã giao, không thể hủy.");
        }

// Xác định có cần hoàn kho không
        boolean isVnPay = hoaDon.getThanhToan() != null
                && hoaDon.getThanhToan().getHinhThucThanhToan() != null
                && hoaDon.getThanhToan().getHinhThucThanhToan().toLowerCase().contains("vnpay");

// Đơn VNPay ở trạng thái DANG_CHO_THANH_TOAN => chưa trừ tồn, KHÔNG hoàn kho
        boolean canRefundStock = !(isVnPay && "DANG_CHO_THANH_TOAN".equals(old));

        if (canRefundStock) {
            var chiTietList = hoaDonChiTietRepo.findByHoaDon_Id(hoaDonId);

            for (HoaDonChiTiet item : chiTietList) {
                SanPhamChiTiet spct = item.getSanPhamChiTiet();
                if (spct == null) continue;

                Integer tonCu = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                Integer soLuongHoan = item.getSoLuong() == null ? 0 : item.getSoLuong();

                spct.setSoLuongTon(tonCu + soLuongHoan);
                sanPhamChiTietRepo.save(spct);
            }
        }


        // Cập nhật trạng thái + ghi chú
        hoaDon.setTrangThai("DA_HUY");

        String ghiChuCu = hoaDon.getGhiChu() == null ? "" : hoaDon.getGhiChu();
        String ghiChuMoi = ghiChuCu + " | [Khách hủy: " + lyDoHuy + "]";
        if (ghiChuMoi.length() > 255) ghiChuMoi = ghiChuMoi.substring(0, 255);
        hoaDon.setGhiChu(ghiChuMoi);

        hoaDonRepo.save(hoaDon);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon cancelOrderGuest(Integer hoaDonId, String lyDoHuy) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));


        String old = hoaDon.getTrangThai() == null ? "" : hoaDon.getTrangThai().trim().toUpperCase();

        if (!"CHO_XAC_NHAN".equals(old) && !"DANG_CHO_THANH_TOAN".equals(old)) {
            throw new RuntimeException("Đơn hàng đang được xử lý hoặc đã giao, không thể hủy.");
        }

        // ✅ Hoàn kho
        var chiTietList = hoaDonChiTietRepo.findByHoaDon_Id(hoaDonId);
        for (HoaDonChiTiet item : chiTietList) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            if (spct == null) continue;

            int tonCu = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
            int soLuongHoan = item.getSoLuong() == null ? 0 : item.getSoLuong();
            spct.setSoLuongTon(tonCu + soLuongHoan);
            sanPhamChiTietRepo.save(spct);
        }

        hoaDon.setTrangThai("DA_HUY");

        String ghiChuCu = hoaDon.getGhiChu() == null ? "" : hoaDon.getGhiChu();
        String ghiChuMoi = ghiChuCu + " | [Khách (guest) hủy: " + lyDoHuy + "]";
        if (ghiChuMoi.length() > 255) ghiChuMoi = ghiChuMoi.substring(0, 255);
        hoaDon.setGhiChu(ghiChuMoi);

        // TRẢ VỀ ĐƠN ĐÃ LƯU
        return hoaDonRepo.save(hoaDon);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void truTonKhoSauThanhToanThanhCong(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn."));

        String oldStatus = hoaDon.getTrangThai() == null
                ? ""
                : hoaDon.getTrangThai().trim().toUpperCase();

        // Chỉ xử lý cho đơn đang CHỜ THANH TOÁN (VNPay)
        if (!"DANG_CHO_THANH_TOAN".equals(oldStatus)) {
            // tránh trừ kho nhiều lần
            return;
        }

        // 1. Trừ tồn kho theo chi tiết hóa đơn
        var chiTietList = hoaDonChiTietRepo.findByHoaDon_Id(hoaDonId);
        for (HoaDonChiTiet item : chiTietList) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            if (spct == null) continue;

            int ton = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
            int qty = item.getSoLuong() == null ? 0 : item.getSoLuong();

            if (ton < qty) {
                throw new RuntimeException("Sản phẩm " + getTenSanPhamSafe(spct) + " không đủ tồn kho.");
            }

            spct.setSoLuongTon(ton - qty);
            sanPhamChiTietRepo.save(spct);
        }

        // 2. Giảm lượt voucher (nếu có)
        GiamGia giamGia = hoaDon.getGiamGia();
        if (giamGia != null) {
            GiamGia gg = giamGiaRepository.findById(giamGia.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher khi xác nhận thanh toán."));
            if (gg.getSoLuong() != null && gg.getSoLuong() > 0) {
                gg.setSoLuong(gg.getSoLuong() - 1);
                giamGiaRepository.save(gg);
            } else {
                throw new RuntimeException("Voucher đã hết lượt sử dụng lúc thanh toán. Vui lòng thử lại.");
            }
        }

        // 3. Xóa giỏ hàng nếu là user đăng nhập
        NguoiDung khachHang = hoaDon.getKhachHang();
        if (khachHang != null) {
            gioHangClientService.clearCart(khachHang);
        }

        // 4. Cập nhật trạng thái + ngày thanh toán
        // (tuỳ business: nếu muốn chỉ là CHO_XAC_NHAN thì đổi "HOAN_THANH" -> "CHO_XAC_NHAN")
        hoaDon.setTrangThai("HOAN_THANH");
        hoaDon.setNgayThanhToan(LocalDateTime.now());

        hoaDonRepo.save(hoaDon);
    }

}
