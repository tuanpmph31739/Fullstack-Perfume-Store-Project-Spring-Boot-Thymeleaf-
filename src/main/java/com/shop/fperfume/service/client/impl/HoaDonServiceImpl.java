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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class HoaDonServiceImpl implements HoaDonClientService {

    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private ThanhToanRepository thanhToanRepo;
    @Autowired private GioHangClientService gioHangClientService;

    // === 1. TẠO ĐƠN HÀNG ===
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrder(GioHang gioHang, NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {

        // 1. Kiểm tra giỏ hàng
        Collection<GioHangChiTiet> cartItems = gioHang.getGioHangChiTiets();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống! Không thể đặt hàng.");
        }

        // 2. Lấy phương thức thanh toán
        ThanhToan phuongThucThanhToan = thanhToanRepo.findById(checkoutInfo.getIdThanhToan())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ."));
        GiamGia giamGia = gioHang.getGiamGia();

        // 3. Khởi tạo Hóa Đơn
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTenNguoiNhan(checkoutInfo.getTenNguoiNhan());
        hoaDon.setDiaChi(checkoutInfo.getDiaChi());
        hoaDon.setSdt(checkoutInfo.getSdt());
        hoaDon.setGhiChu(checkoutInfo.getGhiChu()); // Lưu ghi chú
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setThanhToan(phuongThucThanhToan);
        hoaDon.setPhiShip(new BigDecimal(30000));
        hoaDon.setKenhBan("WEB");
        hoaDon.setMa("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // 4. Set trạng thái ban đầu
        if (phuongThucThanhToan.getHinhThucThanhToan().toLowerCase().contains("vnpay")) {
            hoaDon.setTrangThai("DANG_CHO_THANH_TOAN");
        } else {
            hoaDon.setTrangThai("CHO_XAC_NHAN");
        }

        // 5. Duyệt sản phẩm & Trừ kho
        BigDecimal tongTienHang = BigDecimal.ZERO;
        List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

        for (GioHangChiTiet item : cartItems) {
            SanPhamChiTiet spct = sanPhamChiTietRepo.findById(item.getSanPhamChiTiet().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại ID: " + item.getSanPhamChiTiet().getId()));

            if (spct.getSoLuongTon() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenNuocHoa() + " không đủ hàng.");
            }

            // Trừ kho
            spct.setSoLuongTon(spct.getSoLuongTon() - item.getSoLuong());
            sanPhamChiTietRepo.save(spct);

            // Tạo chi tiết hóa đơn
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(spct.getGiaBan());
            hdct.setNgayTao(LocalDateTime.now());
            hoaDonChiTiets.add(hdct);

            tongTienHang = tongTienHang.add(spct.getGiaBan().multiply(new BigDecimal(item.getSoLuong())));
        }
        hoaDon.setTongTienHang(tongTienHang);

        // 6. Tính giảm giá
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        if (giamGia != null) {
            if ("PERCENT".equals(giamGia.getLoaiGiam())) {
                tienGiamGia = tongTienHang.multiply(giamGia.getGiaTri().divide(new BigDecimal(100)));
            } else {
                tienGiamGia = giamGia.getGiaTri();
            }
            hoaDon.setGiamGia(giamGia);
        }
        hoaDon.setTienGiamGia(tienGiamGia);

        // 7. Tổng tiền cuối cùng
        BigDecimal tongThanhToan = tongTienHang.subtract(tienGiamGia).add(hoaDon.getPhiShip());
        hoaDon.setTongThanhToan(tongThanhToan.max(BigDecimal.ZERO));

        // 8. Lưu DB
        hoaDon.setHoaDonChiTiets(hoaDonChiTiets);
        HoaDon savedHoaDon = hoaDonRepo.save(hoaDon);

        // 9. Xóa giỏ hàng (Nếu là User đăng nhập)
        if (khachHang != null) {
            gioHangClientService.clearCart(khachHang);
        }

        return savedHoaDon;
    }

    // Hàm tương thích cũ
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {
        GioHang gioHang = gioHangRepo.findByKhachHang(khachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng."));
        return this.createOrder(gioHang, khachHang, checkoutInfo);
    }

    // === 2. LẤY DANH SÁCH ĐƠN HÀNG (TÌM KIẾM & LỌC) ===
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
        } catch (Exception e) {
            // Bỏ qua lỗi ngày tháng
        }

        return hoaDonRepo.findHistory(khachHang, keyword, fromDate, toDate);
    }

    // === 3. LẤY CHI TIẾT ĐƠN HÀNG ===
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

    // === 4. HỦY ĐƠN HÀNG ===
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Integer hoaDonId, NguoiDung khachHang, String lyDoHuy) {
        HoaDon hoaDon = getOrderDetailForUser(hoaDonId, khachHang); // Check quyền sở hữu

        String trangThai = hoaDon.getTrangThai();
        if ("CHO_XAC_NHAN".equals(trangThai) || "DANG_CHO_THANH_TOAN".equals(trangThai)) {

            hoaDon.setTrangThai("DA_HUY");

            // Nối lý do hủy vào Ghi chú
            String ghiChuCu = hoaDon.getGhiChu() == null ? "" : hoaDon.getGhiChu();
            String ghiChuMoi = ghiChuCu + " | [Khách hủy: " + lyDoHuy + "]";
            if (ghiChuMoi.length() > 255) ghiChuMoi = ghiChuMoi.substring(0, 255); // Cắt nếu quá dài
            hoaDon.setGhiChu(ghiChuMoi);

            // Hoàn kho
            for (HoaDonChiTiet item : hoaDon.getHoaDonChiTiets()) {
                SanPhamChiTiet spct = item.getSanPhamChiTiet();
                spct.setSoLuongTon(spct.getSoLuongTon() + item.getSoLuong());
                sanPhamChiTietRepo.save(spct);
            }

            hoaDonRepo.save(hoaDon);
        } else {
            throw new RuntimeException("Đơn hàng đang được xử lý hoặc đã giao, không thể hủy.");
        }
    }
}