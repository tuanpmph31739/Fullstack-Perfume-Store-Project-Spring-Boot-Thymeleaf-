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

    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private GioHangChiTietRepository gioHangChiTietRepository;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired private GiamGiaRepository giamGiaRepository;

    @Override
    public GioHang getCartByUser(NguoiDung khachHang) {
        return gioHangRepository.findByKhachHang(khachHang)
                .orElseGet(() -> {
                    GioHang gioHang = new GioHang();
                    gioHang.setKhachHang(khachHang);
                    return gioHangRepository.save(gioHang);
                });
    }

    @Override
    public GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuongThem) {
        if (soLuongThem <= 0) throw new RuntimeException("Số lượng phải lớn hơn 0");

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        int soLuongTonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
        if (soLuongTonKho == 0) throw new RuntimeException("Sản phẩm đã hết hàng");

        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        Optional<GioHangChiTiet> existing = gioHangChiTietRepository
                .findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet);

        int soLuongTrongGio = existing.map(GioHangChiTiet::getSoLuong).orElse(0);
        int tongSoLuongMongMuon = soLuongTrongGio + soLuongThem;

        if (tongSoLuongMongMuon > soLuongTonKho) {
            int coTheThem = soLuongTonKho - soLuongTrongGio;
            throw new RuntimeException("Chỉ còn " + soLuongTonKho + " sản phẩm. Bạn chỉ có thể thêm " + coTheThem + " nữa.");
        }

        if (existing.isPresent()) {
            GioHangChiTiet item = existing.get();
            item.setSoLuong(tongSoLuongMongMuon);
            gioHangChiTietRepository.save(item);
        } else {
            GioHangChiTiet item = new GioHangChiTiet();
            item.setId(id);
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(spct);
            item.setSoLuong(soLuongThem);
            gioHangChiTietRepository.save(item);
        }
        return gioHangRepository.findById(gioHang.getId()).orElseThrow();
    }

    @Override
    public GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong) {
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (newSoLuong <= 0) {
            gioHangChiTietRepository.delete(chiTiet);
            if (gioHang.getGioHangChiTiets() != null) gioHang.getGioHangChiTiets().remove(chiTiet);
        } else {
            SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
            int soLuongTonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            if (newSoLuong > soLuongTonKho) {
                throw new RuntimeException("Số lượng cập nhật vượt quá tồn kho! (Tồn kho: " + soLuongTonKho + ")");
            }
            chiTiet.setSoLuong(newSoLuong);
            gioHangChiTietRepository.save(chiTiet);
        }
        return gioHang;
    }

    @Override
    public GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHangChiTietRepository.findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet)
                .ifPresent(chiTiet -> {
                    gioHangChiTietRepository.delete(chiTiet);
                    if (gioHang.getGioHangChiTiets() != null) gioHang.getGioHangChiTiets().remove(chiTiet);
                });
        return gioHangRepository.findById(gioHang.getId()).orElse(gioHang);
    }

    @Override
    public GioHang applyVoucher(NguoiDung khachHang, String maGiamGia) {
        GioHang gioHang = getCartByUser(khachHang);
        GiamGia voucher = giamGiaRepository.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));
        gioHang.setGiamGia(voucher);
        return gioHangRepository.save(gioHang);
    }

    @Override
    public GioHang removeVoucher(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHang.setGiamGia(null);
        return gioHangRepository.save(gioHang);
    }

    // --- MỚI: IMPLEMENT HÀM XÓA GIỎ HÀNG ---
    @Override
    public void clearCart(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);
        if (gioHang.getGioHangChiTiets() != null && !gioHang.getGioHangChiTiets().isEmpty()) {
            // Xóa tất cả chi tiết trong DB
            gioHangChiTietRepository.deleteAll(gioHang.getGioHangChiTiets());
            // Xóa list trong Object để đồng bộ Hibernate
            gioHang.getGioHangChiTiets().clear();
            // Lưu lại
            gioHangRepository.save(gioHang);
        }
    }
}