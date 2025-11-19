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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class HoaDonServiceImpl implements HoaDonClientService {

    // === (Giữ nguyên tất cả @Autowired) ===
    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepo;
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private GioHangChiTietRepository gioHangChiTietRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private ThanhToanRepository thanhToanRepo;
    @Autowired private GiamGiaRepository giamGiaRepo;
    @Autowired
    private GioHangClientService gioHangClientService;



    /**
     * HÀM MỚI: Xử lý cả Guest và User
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrder(GioHang gioHang, NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {

        // === 2. Lấy chi tiết giỏ hàng ===
        Collection<GioHangChiTiet> cartItems = gioHang.getGioHangChiTiets();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống! Không thể đặt hàng.");
        }

        // === 3. Lấy thông tin thanh toán và giảm giá ===
        ThanhToan phuongThucThanhToan = thanhToanRepo.findById(checkoutInfo.getIdThanhToan())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ."));
        GiamGia giamGia = gioHang.getGiamGia();

        // === 4. Tạo Hóa Đơn cha (HoaDon) ===
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang); // Sẽ set NULL nếu là Guest
        hoaDon.setTenNguoiNhan(checkoutInfo.getTenNguoiNhan());
        hoaDon.setDiaChi(checkoutInfo.getDiaChi());
        hoaDon.setSdt(checkoutInfo.getSdt());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setThanhToan(phuongThucThanhToan);
        hoaDon.setPhiShip(new BigDecimal(30000));
        hoaDon.setKenhBan("WEB");
        hoaDon.setMa("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // === 5. Quyết định trạng thái ===
        if (phuongThucThanhToan.getHinhThucThanhToan().toLowerCase().contains("vnpay")) {
            hoaDon.setTrangThai("DANG_CHO_THANH_TOAN");
        } else {
            hoaDon.setTrangThai("CHO_XAC_NHAN");
        }

        // === 6. Tạo Hóa Đơn Chi Tiết và Tính Tiền Hàng ===
        BigDecimal tongTienHang = BigDecimal.ZERO;
        List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

        for (GioHangChiTiet item : cartItems) {
            SanPhamChiTiet spct = sanPhamChiTietRepo.findById(item.getSanPhamChiTiet().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm trong giỏ hàng không còn tồn tại."));

            // Kiểm tra tồn kho
            if (spct.getSoLuongTon() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenNuocHoa() + " không đủ hàng.");
            }

            // TRỪ KHO
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

        // === 7. Tính toán giảm giá ===
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

        // === 8. Tính tổng thanh toán cuối cùng ===
        BigDecimal tongThanhToan = tongTienHang.subtract(tienGiamGia).add(hoaDon.getPhiShip());
        hoaDon.setTongThanhToan(tongThanhToan.max(BigDecimal.ZERO));

        // === 9. Lưu Hóa Đơn và Xóa Giỏ Hàng ===
        hoaDon.setHoaDonChiTiets(hoaDonChiTiets);
        HoaDon savedHoaDon = hoaDonRepo.save(hoaDon);

        if (khachHang != null) {
            gioHangClientService.clearCart(khachHang);  // tái dùng hàm đã viết đúng
        }


        return savedHoaDon;
    }


    /**
     * HÀM CŨ: (Gọi hàm mới)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {
        GioHang gioHang = gioHangRepo.findByKhachHang(khachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của bạn."));
        return this.createOrder(gioHang, khachHang, checkoutInfo);
    }

    /**
     * THÊM LẠI: Lấy lịch sử đơn hàng
     */
    @Override
    @Transactional(readOnly = true)
    public List<HoaDon> getOrdersByUser(NguoiDung khachHang) {
        return hoaDonRepo.findByKhachHangOrderByNgayTaoDesc(khachHang);
    }

    /**
     * THÊM LẠI: Lấy chi tiết một đơn hàng
     * (Đây là hàm bị thiếu gây ra lỗi)
     */
    @Override
    @Transactional(readOnly = true)
    public HoaDon getOrderDetailForUser(Integer hoaDonId, NguoiDung khachHang) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + hoaDonId));

        // KIỂM TRA BẢO MẬT
        if (!hoaDon.getKhachHang().getId().equals(khachHang.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này.");
        }


        return hoaDon;
    }
}