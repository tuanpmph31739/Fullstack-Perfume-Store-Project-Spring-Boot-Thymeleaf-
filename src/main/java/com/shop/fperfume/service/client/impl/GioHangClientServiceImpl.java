package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.GiamGiaRepository;
import com.shop.fperfume.repository.GioHangChiTietRepository;
import com.shop.fperfume.repository.GioHangRepository;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import com.shop.fperfume.service.client.GioHangClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GioHangClientServiceImpl implements GioHangClientService {

    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private GioHangChiTietRepository gioHangChiTietRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private GiamGiaRepository giamGiaRepo;

    /**
     * Logic "Tìm hoặc Tạo" (vay mượn từ CartServiceImpl.java)
     */
    @Override
    @Transactional // Đảm bảo hàm này có @Transactional
    public GioHang getCartByUser(NguoiDung khachHang) {

        // Logic "Tìm hoặc Tạo"
        GioHang gioHang = gioHangRepo.findByKhachHang(khachHang)
                .orElseGet(() -> {
                    GioHang newCart = new GioHang();
                    newCart.setKhachHang(khachHang);
                    newCart.setNgayTao(LocalDateTime.now());
                    newCart.setNgaySua(LocalDateTime.now());
                    return gioHangRepo.save(newCart);
                });

        // === THÊM DÒNG NÀY ĐỂ SỬA LỖI ===
        // Dòng này "chạm" vào danh sách, buộc Hibernate
        // phải tải nó trước khi Session bị đóng.
        if (gioHang.getGioHangChiTiets() != null) {
            gioHang.getGioHangChiTiets().size();
        }
        // ================================

        return gioHang;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        GioHang gioHang = getCartByUser(khachHang);
        SanPhamChiTiet spct = sanPhamChiTietRepo.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        // Kiểm tra tồn kho
        if (spct.getSoLuongTon() < soLuong) {
            throw new RuntimeException("Không đủ hàng, chỉ còn " + spct.getSoLuongTon() + " sản phẩm.");
        }

        // Tạo ID tổng hợp để tìm kiếm
        GioHangChiTietId itemId = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);

        // Logic "Merge" (Vay mượn từ Cart.java)
        GioHangChiTiet existingItem = gioHangChiTietRepo.findById(itemId).orElse(null);

        if (existingItem != null) {
            // Đã có -> Cộng dồn số lượng
            int newSoLuong = existingItem.getSoLuong() + soLuong;
            if (spct.getSoLuongTon() < newSoLuong) {
                throw new RuntimeException("Số lượng trong giỏ (" + newSoLuong + ") vượt quá tồn kho (" + spct.getSoLuongTon() + ").");
            }
            existingItem.setSoLuong(newSoLuong);
            gioHangChiTietRepo.save(existingItem);
        } else {
            // Chưa có -> Tạo mới
            GioHangChiTiet newItem = new GioHangChiTiet();
            newItem.setId(itemId);
            newItem.setGioHang(gioHang);
            newItem.setSanPhamChiTiet(spct);
            newItem.setSoLuong(soLuong);
            gioHangChiTietRepo.save(newItem);
        }

        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong) {
        if (newSoLuong <= 0) {
            // Nếu số lượng mới <= 0, coi như là xóa
            return removeItemFromCart(khachHang, idSanPhamChiTiet);
        }

        GioHang gioHang = getCartByUser(khachHang);
        SanPhamChiTiet spct = sanPhamChiTietRepo.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        // Kiểm tra tồn kho
        if (spct.getSoLuongTon() < newSoLuong) {
            throw new RuntimeException("Không đủ hàng, chỉ còn " + spct.getSoLuongTon() + " sản phẩm.");
        }

        GioHangChiTietId itemId = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        GioHangChiTiet item = gioHangChiTietRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng."));

        item.setSoLuong(newSoLuong);
        gioHangChiTietRepo.save(item);

        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang);
    }

    @Override
    @Transactional
    public GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet) {
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId itemId = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);

        if (!gioHangChiTietRepo.existsById(itemId)) {
            throw new RuntimeException("Sản phẩm không có trong giỏ hàng.");
        }

        gioHangChiTietRepo.deleteById(itemId);

        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang);
    }

    @Override
    @Transactional
    public GioHang applyVoucher(NguoiDung khachHang, String maGiamGia) {
        GioHang gioHang = getCartByUser(khachHang);
        GiamGia giamGia = giamGiaRepo.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ."));

        // (Bạn có thể thêm logic kiểm tra ngày hết hạn, số lượng... của GiamGia ở đây)

        gioHang.setGiamGia(giamGia);
        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang);
    }

    @Override
    @Transactional
    public GioHang removeVoucher(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHang.setGiamGia(null);
        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang);
    }
}