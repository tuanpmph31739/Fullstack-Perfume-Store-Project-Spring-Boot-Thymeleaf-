package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.service.client.GioHangClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class GioHangClientServiceImpl implements GioHangClientService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private GiamGiaRepository giamGiaRepository;

    /**
     * Lấy giỏ hàng theo người dùng (Giữ nguyên)
     */
    @Override
    public GioHang getCartByUser(NguoiDung khachHang) {
        return gioHangRepository.findByKhachHang(khachHang)
                .orElseGet(() -> {
                    GioHang gioHang = new GioHang();
                    gioHang.setKhachHang(khachHang);
                    return gioHangRepository.save(gioHang);
                });
    }

    /**
     * Thêm sản phẩm vào giỏ (SỬA LỖI KIỂM TRA TỒN KHO)
     */
    @Override
    public GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuongThem) {
        // THÊM: Kiểm tra số lượng đầu vào
        if (soLuongThem <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        // --- BƯỚC 1: Lấy sản phẩm và kiểm tra tồn kho ---
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        int soLuongTonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;

        if (soLuongTonKho == 0) {
            throw new RuntimeException("Sản phẩm [" + spct.getSanPham().getTenNuocHoa() + "] đã hết hàng");
        }

        // --- BƯỚC 2: Lấy giỏ hàng và kiểm tra số lượng hiện có ---
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        Optional<GioHangChiTiet> existing = gioHangChiTietRepository
                .findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet);

        int soLuongTrongGio = 0;
        if (existing.isPresent()) {
            soLuongTrongGio = existing.get().getSoLuong();
        }

        // --- BƯỚC 3: Kiểm tra tổng số lượng mong muốn ---
        int tongSoLuongMongMuon = soLuongTrongGio + soLuongThem;

        if (tongSoLuongMongMuon > soLuongTonKho) {
            int coTheThem = soLuongTonKho - soLuongTrongGio;
            if (coTheThem <= 0) {
                throw new RuntimeException("Bạn đã có tối đa sản phẩm này trong giỏ. Tồn kho: " + soLuongTonKho);
            }
            // Ném lỗi với thông báo rõ ràng
            throw new RuntimeException("Chỉ còn " + soLuongTonKho + " sản phẩm. Bạn chỉ có thể thêm " + coTheThem + " sản phẩm nữa.");
        }

        // --- BƯỚC 4: Lưu (Nếu mọi thứ hợp lệ) ---
        if (existing.isPresent()) {
            GioHangChiTiet item = existing.get();
            item.setSoLuong(tongSoLuongMongMuon); // SỬA: Cập nhật tổng số lượng mới
            gioHangChiTietRepository.save(item);
        } else {
            GioHangChiTiet item = new GioHangChiTiet();
            item.setId(id);
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(spct); // SỬA: Tái sử dụng spct đã lấy
            item.setSoLuong(soLuongThem); // Thêm mới
            gioHangChiTietRepository.save(item);
        }

        return gioHangRepository.findById(gioHang.getId()).orElseThrow();
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng (SỬA LỖI KIỂM TRA TỒN KHO)
     */
    @Override
    public GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong) {
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);

        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (newSoLuong <= 0) {
            // Logic xóa của bạn đã đúng (Giữ nguyên)
            gioHangChiTietRepository.delete(chiTiet);
            if (gioHang.getGioHangChiTiets() != null) {
                gioHang.getGioHangChiTiets().remove(chiTiet);
            }
        } else {
            // --- THÊM: KIỂM TRA TỒN KHO ---
            SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
            int soLuongTonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;

            if (newSoLuong > soLuongTonKho) {
                // Ném lỗi -> Controller sẽ bắt và báo cho client
                throw new RuntimeException("Số lượng cập nhật vượt quá tồn kho! (Tồn kho: " + soLuongTonKho + ")");
            }
            // --- HẾT PHẦN KIỂM TRA ---

            chiTiet.setSoLuong(newSoLuong);
            gioHangChiTietRepository.save(chiTiet);
        }

        return gioHang;
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng (Giữ nguyên)
     */
    @Override
    public GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet) {
        GioHang gioHang = getCartByUser(khachHang);

        Optional<GioHangChiTiet> chiTietOpt =
                gioHangChiTietRepository.findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet);

        if (chiTietOpt.isPresent()) {
            GioHangChiTiet chiTiet = chiTietOpt.get();
            gioHangChiTietRepository.delete(chiTiet);

            if (gioHang.getGioHangChiTiets() != null) {
                gioHang.getGioHangChiTiets().remove(chiTiet);
            }
        }
        return gioHangRepository.findById(gioHang.getId()).orElse(gioHang);
    }

    /**
     * Áp dụng mã giảm giá (Giữ nguyên)
     */
    @Override
    public GioHang applyVoucher(NguoiDung khachHang, String maGiamGia) {
        GioHang gioHang = getCartByUser(khachHang);
        GiamGia voucher = giamGiaRepository.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));

        gioHang.setGiamGia(voucher);
        return gioHangRepository.save(gioHang);
    }

    /**
     * Gỡ bỏ mã giảm giá (Giữ nguyên)
     */
    @Override
    public GioHang removeVoucher(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHang.setGiamGia(null);
        return gioHangRepository.save(gioHang);
    }
}