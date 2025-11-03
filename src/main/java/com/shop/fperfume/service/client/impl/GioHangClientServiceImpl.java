package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.service.client.GioHangClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList; // <-- THÊM IMPORT NÀY
import java.util.List;     // <-- THÊM IMPORT NÀY

@Service
public class GioHangClientServiceImpl implements GioHangClientService {

    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private GioHangChiTietRepository gioHangChiTietRepo;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepo;
    @Autowired private GiamGiaRepository giamGiaRepo;
    @Autowired private NguoiDungRepository nguoiDungRepo;

    /**
     * Logic "Tìm hoặc Tạo"
     * (Sử dụng @Query JOIN FETCH từ GioHangRepository)
     *
     * === ĐÃ SỬA LỖI ===
     * Đảm bảo 'gioHang.gioHangChiTiets' không bao giờ null,
     * ngay cả khi giỏ hàng mới được tạo hoặc rỗng.
     */
    @Override
    @Transactional
    public GioHang getCartByUser(NguoiDung khachHang) {

        // TODO: Xóa 2 dòng giả lập này khi có đăng nhập
        // Chúng ta dùng ID 2 (Vũ Hoàng Anh) vì CSDL có dữ liệu giỏ hàng cho user này.
        NguoiDung user = nguoiDungRepo.findById(2L).orElse(khachHang);

        GioHang gioHang = gioHangRepo.findByKhachHang(user)
                .orElseGet(() -> {
                    // Nếu user chưa có giỏ hàng, tạo mới
                    GioHang newCart = new GioHang();
                    newCart.setKhachHang(user);
                    newCart.setNgayTao(LocalDateTime.now());
                    newCart.setNgaySua(LocalDateTime.now());
                    // 1. Khởi tạo danh sách rỗng để tránh lỗi NullPointerException
                    newCart.setGioHangChiTiets(new ArrayList<>());
                    return gioHangRepo.save(newCart);
                });

        // 2. Nếu giỏ hàng đã tồn tại nhưng list là null (do fetch không tìm thấy),
        // cũng khởi tạo nó để tránh lỗi NullPointerException
        if (gioHang.getGioHangChiTiets() == null) {
            gioHang.setGioHangChiTiets(new ArrayList<>());
        }

        // Dòng này vẫn cần thiết để kích hoạt JOIN FETCH (nếu không dùng @Query)
        gioHang.getGioHangChiTiets().size();

        return gioHang;
    }

    /**
     * Thêm sản phẩm vào giỏ.
     * Logic này đã chính xác (sử dụng bidirectional association)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        GioHang gioHang = getCartByUser(khachHang);
        SanPhamChiTiet spct = sanPhamChiTietRepo.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        if (spct.getSoLuongTon() < soLuong) {
            throw new RuntimeException("Không đủ hàng, chỉ còn " + spct.getSoLuongTon() + " sản phẩm.");
        }

        GioHangChiTietId itemId = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);

        // Tìm item trong danh sách (đã được tải bởi JOIN FETCH)
        GioHangChiTiet existingItem = gioHang.getGioHangChiTiets().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst().orElse(null);

        if (existingItem != null) {
            // Đã có -> Cộng dồn
            int newSoLuong = existingItem.getSoLuong() + soLuong;
            if (spct.getSoLuongTon() < newSoLuong) {
                throw new RuntimeException("Số lượng trong giỏ (" + newSoLuong + ") vượt quá tồn kho (" + spct.getSoLuongTon() + ").");
            }
            existingItem.setSoLuong(newSoLuong);
        } else {
            // Chưa có -> Tạo mới
            GioHangChiTiet newItem = new GioHangChiTiet();
            newItem.setId(itemId);
            newItem.setGioHang(gioHang);
            newItem.setSanPhamChiTiet(spct);
            newItem.setSoLuong(soLuong);
            // Thêm item mới vào danh sách của Cha
            gioHang.getGioHangChiTiets().add(newItem);
        }

        gioHang.setNgaySua(LocalDateTime.now());
        // Chỉ cần save Cha (GioHang),
        // JPA sẽ tự động cập nhật/thêm ChiTiet (nhờ CascadeType.ALL)
        return gioHangRepo.save(gioHang);
    }

    /**
     * Cập nhật số lượng. Logic này đã chính xác.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong) {
        GioHang gioHang = getCartByUser(khachHang);

        if (newSoLuong <= 0) {
            // Nếu số lượng mới <= 0, gọi hàm xóa
            return removeItemFromCart(khachHang, idSanPhamChiTiet);
        }

        SanPhamChiTiet spct = sanPhamChiTietRepo.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        if (spct.getSoLuongTon() < newSoLuong) {
            throw new RuntimeException("Không đủ hàng, chỉ còn " + spct.getSoLuongTon() + " sản phẩm.");
        }

        GioHangChiTietId itemId = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        GioHangChiTiet item = gioHang.getGioHangChiTiets().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng."));

        item.setSoLuong(newSoLuong);
        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang); // Save Cha
    }

    /**
     * HÀM REMOVEITEMFROMCART
     *
     * === ĐÃ SỬA LỖI ===
     * Sửa lỗi 'ObjectDeletedException' bằng cách
     * chỉ xóa item khỏi List của 'gioHang'.
     * JPA sẽ tự động xóa item (nhờ orphanRemoval=true).
     */
    @Override
    @Transactional
    public GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet) {
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId itemId = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);

        // 1. Tìm item trong danh sách của giỏ hàng
        GioHangChiTiet itemToRemove = gioHang.getGioHangChiTiets().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng."));

        // 2. Xóa item khỏi danh sách của Cha (Quan trọng)
        // Nếu Entity GioHang có @OneToMany(..., orphanRemoval = true)
        // thì bước này là đủ để Hibernate tự động xóa
        gioHang.getGioHangChiTiets().remove(itemToRemove);

        // 3. (Xóa dòng này)
        // gioHangChiTietRepo.delete(itemToRemove); // <-- Dòng này gây lỗi ObjectDeletedException

        gioHang.setNgaySua(LocalDateTime.now());
        return gioHangRepo.save(gioHang); // Lưu lại Cha
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