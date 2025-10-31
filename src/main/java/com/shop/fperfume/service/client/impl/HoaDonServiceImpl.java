package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.DTO.CheckoutRequestDTO;
import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.service.client.HoaDonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Dùng để tạo mã hóa đơn ngẫu nhiên

/**
 * Lớp thực thi logic nghiệp vụ Hóa Đơn cho phía Client.
 * Chứa logic tạo đơn hàng và xem lịch sử đơn hàng.
 */
@Service // Đánh dấu đây là một Service Bean
public class HoaDonServiceImpl implements HoaDonClientService {

    // === 1. Tiêm (Inject) tất cả Repository cần thiết ===
    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepo;
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private GioHangChiTietRepository gioHangChiTietRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private ThanhToanRepository thanhToanRepo;
    @Autowired private GiamGiaRepository giamGiaRepo;

    /**
     * @Transactional(rollbackFor = Exception.class)
     * Đảm bảo tất cả các bước trong hàm này phải cùng thành công.
     * Nếu có 1 lỗi (ví dụ: hết hàng), toàn bộ giao dịch (kể cả việc tạo HĐ) sẽ bị hủy.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon createOrderFromCart(NguoiDung khachHang, CheckoutRequestDTO checkoutInfo) {

        // === 2. Lấy giỏ hàng của khách ===
        GioHang gioHang = gioHangRepo.findByKhachHang(khachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của bạn."));

        List<GioHangChiTiet> cartItems = gioHangChiTietRepo.findByGioHang(gioHang);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống! Không thể đặt hàng.");
        }

        // === 3. Lấy thông tin thanh toán và giảm giá ===
        ThanhToan phuongThucThanhToan = thanhToanRepo.findById(checkoutInfo.getIdThanhToan())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ."));

        GiamGia giamGia = (gioHang.getGiamGia() != null)
                ? giamGiaRepo.findById(gioHang.getGiamGia().getId()).orElse(null)
                : null;

        // === 4. Tạo Hóa Đơn cha (HoaDon) ===
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTenNguoiNhan(checkoutInfo.getTenNguoiNhan());
        hoaDon.setDiaChi(checkoutInfo.getDiaChi());
        hoaDon.setSdt(checkoutInfo.getSdt());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setThanhToan(phuongThucThanhToan);
        hoaDon.setPhiShip(new BigDecimal(30000)); // Lấy giá trị mặc định (30k)
        hoaDon.setMa("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()); // Tạo mã HĐ ngẫu nhiên

        // === 5. Quyết định trạng thái ban đầu (Rất quan trọng cho VNPay) ===
        // Đây là mấu chốt để "thuận tiện cho thanh toán"
        if (phuongThucThanhToan.getHinhThucThanhToan().toLowerCase().contains("vnpay")) {
            hoaDon.setTrangThai("ĐANG CHỜ THANH TOÁN");
        } else {
            hoaDon.setTrangThai("CHỜ XÁC NHẬN"); // Mặc định cho COD
        }

        // === 6. Tạo Hóa Đơn Chi Tiết và Tính Tiền Hàng ===
        BigDecimal tongTienHang = BigDecimal.ZERO;
        List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

        for (GioHangChiTiet item : cartItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();

            // 6a. Kiểm tra tồn kho
            if (spct.getSoLuongTon() < item.getSoLuong()) {
                // Ném lỗi -> Transactional sẽ tự động rollback
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenNuocHoa() + " không đủ hàng (chỉ còn " + spct.getSoLuongTon() + ").");
            }

            // 6b. Tạo chi tiết hóa đơn
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon); // Liên kết với Hóa đơn cha
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(spct.getGiaBan()); // <-- "Đóng băng" giá bán tại thời điểm mua
            hdct.setNgayTao(LocalDateTime.now());

            hoaDonChiTiets.add(hdct);

            // 6c. Cộng dồn tổng tiền hàng
            tongTienHang = tongTienHang.add(spct.getGiaBan().multiply(new BigDecimal(item.getSoLuong())));
        }

        hoaDon.setTongTienHang(tongTienHang);

        // === 7. Tính toán giảm giá ===
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        if (giamGia != null) {
            if ("PERCENT".equals(giamGia.getLoaiGiam())) {
                tienGiamGia = tongTienHang.multiply(giamGia.getGiaTri().divide(new BigDecimal(100)));
            } else { // "AMOUNT"
                tienGiamGia = giamGia.getGiaTri();
            }
            hoaDon.setGiamGia(giamGia);
        }
        hoaDon.setTienGiamGia(tienGiamGia);

        // === 8. Tính tổng thanh toán cuối cùng ===
        BigDecimal tongThanhToan = tongTienHang.subtract(tienGiamGia).add(hoaDon.getPhiShip());
        hoaDon.setTongThanhToan(tongThanhToan.max(BigDecimal.ZERO)); // Đảm bảo không bị âm

        // === 9. Lưu Hóa Đơn và Xóa Giỏ Hàng ===
        hoaDon.setHoaDonChiTiets(hoaDonChiTiets); // Gán danh sách con vào cha

        HoaDon savedHoaDon = hoaDonRepo.save(hoaDon); // JPA sẽ tự động lưu các chi tiết con (do cascade)

        gioHangChiTietRepo.deleteAllInBatch(cartItems); // Xóa các sản phẩm trong giỏ hàng

        gioHang.setGiamGia(null); // Reset mã giảm giá trong giỏ hàng
        gioHangRepo.save(gioHang);

        return savedHoaDon;
    }

    /**
     * Lấy lịch sử đơn hàng cho khách hàng
     * (Học hỏi từ logic file OrderServiceImpl.java)
     */
    @Override
    @Transactional(readOnly = true) // readOnly = true vì chỉ đọc dữ liệu, tăng hiệu suất
    public List<HoaDon> getOrdersByUser(NguoiDung khachHang) {
        return hoaDonRepo.findByKhachHangOrderByNgayTaoDesc(khachHang);
    }

    /**
     * Lấy chi tiết một đơn hàng, đồng thời kiểm tra
     * xem khách hàng có đúng là chủ của đơn hàng đó không.
     */
    @Override
    @Transactional(readOnly = true)
    public HoaDon getOrderDetailForUser(Integer hoaDonId, NguoiDung khachHang) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + hoaDonId));

        // KIỂM TRA BẢO MẬT: Đảm bảo người dùng này sở hữu hóa đơn
        if (!hoaDon.getKhachHang().getId().equals(khachHang.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này.");
            // (Trong Spring Security, đây nên là AccessDeniedException)
        }

        return hoaDon;
    }
}