package com.shop.fperfume.service.client; // Hoặc package service của bạn

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartHelperService {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    /**
     * Chuyển từ Map<Integer, Integer> trong Session thành một đối tượng GioHang "ảo"
     * dùng cho khách GUEST (chưa đăng nhập)
     */
    public GioHang buildVirtualCartFromSession(Map<Integer, Integer> guestCart) {
        GioHang gioHang = new GioHang();
        if (guestCart == null || guestCart.isEmpty()) {
            gioHang.setGioHangChiTiets(Collections.emptyList());
            gioHang.setGiamGia(null);
            return gioHang;
        }

        List<GioHangChiTiet> chiTietList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : guestCart.entrySet()) {
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(entry.getKey()).orElse(null);
            if (spct == null) continue;

            GioHangChiTiet item = new GioHangChiTiet();
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(spct);
            item.setSoLuong(entry.getValue());
            chiTietList.add(item);
        }
        gioHang.setGioHangChiTiets(chiTietList);
        gioHang.setGiamGia(null); // Guest không dùng voucher
        return gioHang;
    }

    /**
     * Tính toán:
     * - tổng tiền hàng (tongTienHang)
     * - tiền giảm giá (tienGiamGia) => có áp:
     *      + phạm vi áp dụng (1 sản phẩm / toàn giỏ)
     *      + đơn hàng tối thiểu (donHangToiThieu)
     *      + giảm tối đa (giamToiDa)
     *      + không vượt quá phần áp dụng
     * - tổng thanh toán (tongThanhToan)
     * - cartSize: tổng số lượng sản phẩm trong giỏ
     */
    public Map<String, Object> calculateCartData(GioHang gioHang) {
        BigDecimal tongTienHang = BigDecimal.ZERO;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        BigDecimal tongThanhToan;
        int cartSize = 0;

        List<GioHangChiTiet> items = (gioHang != null && gioHang.getGioHangChiTiets() != null)
                ? gioHang.getGioHangChiTiets()
                : Collections.emptyList();

        if (!items.isEmpty()) {

            // --- 1. Tổng số lượng & tổng tiền hàng ---
            for (GioHangChiTiet item : items) {
                int soLuong = item.getSoLuong() != null ? item.getSoLuong() : 0;
                cartSize += soLuong;

                SanPhamChiTiet spct = item.getSanPhamChiTiet();
                if (spct != null && spct.getGiaBan() != null) {
                    BigDecimal giaBan = spct.getGiaBan();
                    tongTienHang = tongTienHang.add(giaBan.multiply(BigDecimal.valueOf(soLuong)));
                }
            }

            // --- 2. Tính giảm giá nếu có voucher ---
            GiamGia giamGia = gioHang.getGiamGia();
            if (giamGia != null) {

                // 2.1 Tính tổng tiền "áp dụng" cho voucher (1 sản phẩm / toàn giỏ)
                BigDecimal tongTienApDung = calculateApplicableSubtotal(giamGia, items);

                // 2.2 Kiểm tra đơn hàng tối thiểu (nếu có)
                if (giamGia.getDonHangToiThieu() != null
                        && giamGia.getDonHangToiThieu().compareTo(BigDecimal.ZERO) > 0
                        && tongTienApDung.compareTo(giamGia.getDonHangToiThieu()) < 0) {

                    // Không đủ điều kiện => không giảm
                    tienGiamGia = BigDecimal.ZERO;

                } else {
                    // 2.3 Tính số tiền giảm thực tế (PERCENT/AMOUNT + giamToiDa)
                    tienGiamGia = calculateDiscountAmount(giamGia, tongTienApDung);
                }

                // 2.4 Không cho tiền giảm vượt quá tổng tiền hàng
                if (tienGiamGia.compareTo(tongTienHang) > 0) {
                    tienGiamGia = tongTienHang;
                }
            }
        }

        // --- 3. Tổng thanh toán ---
        tongThanhToan = tongTienHang.subtract(tienGiamGia).max(BigDecimal.ZERO);

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("tongTienHang", tongTienHang);
        cartData.put("tienGiamGia", tienGiamGia);
        cartData.put("tongThanhToan", tongThanhToan);
        cartData.put("cartSize", cartSize);
        return cartData;
    }

    /**
     * Tính tổng tiền "áp dụng" cho voucher:
     * - Nếu giamGia.sanPhamChiTiet != null -> chỉ cộng tiền của sản phẩm đó trong giỏ
     * - Ngược lại -> toàn bộ giỏ hàng
     */
    private BigDecimal calculateApplicableSubtotal(GiamGia giamGia, List<GioHangChiTiet> chiTiets) {
        if (giamGia == null) return BigDecimal.ZERO;

        if (giamGia.getSanPhamChiTiet() != null) {
            Integer spId = giamGia.getSanPhamChiTiet().getId();
            return chiTiets.stream()
                    .filter(ct -> ct.getSanPhamChiTiet() != null
                            && Objects.equals(ct.getSanPhamChiTiet().getId(), spId))
                    .map(ct -> {
                        SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                        BigDecimal giaBan = (spct != null && spct.getGiaBan() != null)
                                ? spct.getGiaBan()
                                : BigDecimal.ZERO;
                        return giaBan.multiply(BigDecimal.valueOf(ct.getSoLuong()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Toàn giỏ hàng
            return chiTiets.stream()
                    .map(ct -> {
                        SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                        BigDecimal giaBan = (spct != null && spct.getGiaBan() != null)
                                ? spct.getGiaBan()
                                : BigDecimal.ZERO;
                        return giaBan.multiply(BigDecimal.valueOf(ct.getSoLuong()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Tính tiền giảm:
     * - PERCENT: subtotal * (giaTri / 100)
     * - AMOUNT : giaTri cố định
     * Sau đó:
     * - Áp giamToiDa nếu có
     * - Không vượt quá subtotal áp dụng
     */
    private BigDecimal calculateDiscountAmount(GiamGia giamGia, BigDecimal subtotalApDung) {
        if (giamGia == null || subtotalApDung == null) return BigDecimal.ZERO;

        BigDecimal result = BigDecimal.ZERO;

        if ("PERCENT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            if (giamGia.getGiaTri() == null) return BigDecimal.ZERO;
            result = subtotalApDung.multiply(
                    giamGia.getGiaTri().divide(BigDecimal.valueOf(100))
            );
        } else if ("AMOUNT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            result = (giamGia.getGiaTri() != null) ? giamGia.getGiaTri() : BigDecimal.ZERO;
        } else {
            return BigDecimal.ZERO;
        }

        // Áp cap GiamToiDa nếu có
        if (giamGia.getGiamToiDa() != null && giamGia.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
            result = result.min(giamGia.getGiamToiDa());
        }

        // Không cho vượt quá phần áp dụng
        if (result.compareTo(subtotalApDung) > 0) {
            result = subtotalApDung;
        }

        return result.max(BigDecimal.ZERO);
    }
}
