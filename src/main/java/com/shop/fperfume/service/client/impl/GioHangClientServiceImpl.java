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
     * Lấy giỏ hàng theo người dùng, nếu chưa có thì tạo mới
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
     * Thêm sản phẩm vào giỏ
     */
    @Override
    public GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuong) {
        GioHang gioHang = getCartByUser(khachHang);

        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        Optional<GioHangChiTiet> existing = gioHangChiTietRepository.findById(id);

        if (existing.isPresent()) {
            // Nếu sản phẩm đã tồn tại → cộng dồn số lượng
            GioHangChiTiet item = existing.get();
            item.setSoLuong(item.getSoLuong() + soLuong);
            gioHangChiTietRepository.save(item);
        } else {
            // Nếu chưa có → thêm mới
            GioHangChiTiet item = new GioHangChiTiet();
            item.setId(id);
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(
                    sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")));
            item.setSoLuong(soLuong);
            gioHangChiTietRepository.save(item);
        }

        return gioHangRepository.findById(gioHang.getId()).orElseThrow();
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @Override
    public GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong) {
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);

        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (newSoLuong <= 0) {
            gioHangChiTietRepository.delete(chiTiet);
            if (gioHang.getGioHangChiTiets() != null) {
                gioHang.getGioHangChiTiets().remove(chiTiet);
            }
        } else {
            chiTiet.setSoLuong(newSoLuong);
            gioHangChiTietRepository.save(chiTiet);
        }

        return gioHang;
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng (đã fix lỗi ObjectDeletedException)
     */
    @Override
    public GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet) {
        GioHang gioHang = getCartByUser(khachHang);

        Optional<GioHangChiTiet> chiTietOpt =
                gioHangChiTietRepository.findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet);

        if (chiTietOpt.isPresent()) {
            GioHangChiTiet chiTiet = chiTietOpt.get();

            // Xóa chi tiết khỏi DB
            gioHangChiTietRepository.delete(chiTiet);

            // Xóa khỏi danh sách trong bộ nhớ (tránh lỗi deleted instance)
            if (gioHang.getGioHangChiTiets() != null) {
                gioHang.getGioHangChiTiets().remove(chiTiet);
            }
        }

        // Không gọi save(gioHang) nữa → tránh lỗi Hibernate merge
        return gioHangRepository.findById(gioHang.getId()).orElse(gioHang);
    }

    /**
     * Áp dụng mã giảm giá
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
     * Gỡ bỏ mã giảm giá
     */
    @Override
    public GioHang removeVoucher(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHang.setGiamGia(null);
        return gioHangRepository.save(gioHang);
    }
}
