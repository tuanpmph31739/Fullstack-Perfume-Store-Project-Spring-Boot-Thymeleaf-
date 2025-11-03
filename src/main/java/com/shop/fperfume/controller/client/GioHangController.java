package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.service.client.GioHangClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal; // Import BigDecimal
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class GioHangController {

    @Autowired
    private GioHangClientService gioHangClientService;

    /**
     * HIỂN THỊ trang giỏ hàng.
     * (ĐÃ CẬP NHẬT: Thêm logic tính toán tổng tiền)
     */
    @GetMapping
    public String viewCart(Model model
            /* @AuthenticationPrincipal NguoiDung khachHang */) {

        // TODO: Lấy NguoiDung thật từ Spring Security
        NguoiDung khachHang = new NguoiDung(); // Giả lập User
        khachHang.setId(2L); // Giả lập User ID 2 (Vũ Hoàng Anh)

        GioHang gioHang = gioHangClientService.getCartByUser(khachHang);

        // === LOGIC TÍNH TOÁN BẮT ĐẦU ===
        BigDecimal tongTienHang = BigDecimal.ZERO;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        BigDecimal tongThanhToan = BigDecimal.ZERO;

        if (gioHang != null && !gioHang.getGioHangChiTiets().isEmpty()) {
            // 1. Tính Tổng Tiền Hàng (Tạm tính)
            for (GioHangChiTiet item : gioHang.getGioHangChiTiets()) {
                BigDecimal giaBan = item.getSanPhamChiTiet().getGiaBan();
                BigDecimal soLuong = new BigDecimal(item.getSoLuong());
                tongTienHang = tongTienHang.add(giaBan.multiply(soLuong));
            }

            // 2. Tính Tiền Giảm Giá (Nếu có)
            GiamGia giamGia = gioHang.getGiamGia();
            if (giamGia != null) {
                if ("PERCENT".equals(giamGia.getLoaiGiam())) {
                    tienGiamGia = tongTienHang.multiply(giamGia.getGiaTri().divide(new BigDecimal(100)));
                } else { // "AMOUNT"
                    tienGiamGia = giamGia.getGiaTri();
                }
                // Đảm bảo tiền giảm không lớn hơn tổng tiền hàng
                tienGiamGia = tienGiamGia.min(tongTienHang);
            }
        }

        // 3. Tính Tổng Thanh Toán (Chưa bao gồm ship)
        tongThanhToan = tongTienHang.subtract(tienGiamGia);
        // === LOGIC TÍNH TOÁN KẾT THÚC ===


        // Đẩy các giá trị ra View (cart.html)
        model.addAttribute("gioHang", gioHang);
        model.addAttribute("tongTienHang", tongTienHang);
        model.addAttribute("tienGiamGia", tienGiamGia);
        model.addAttribute("tongThanhToan", tongThanhToan);

        return "client/cart"; // Trả về file /resources/templates/client/cart.html
    }

    /**
     * THÊM sản phẩm vào giỏ (dùng cho AJAX).
     * (Giữ nguyên)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addItemToCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong
            /* @AuthenticationPrincipal NguoiDung khachHang */) {

        // TODO: Lấy NguoiDung thật
        NguoiDung khachHang = new NguoiDung();
        khachHang.setId(2L);

        try {
            GioHang gioHang = gioHangClientService.addItemToCart(khachHang, idSanPhamChiTiet, soLuong);

            // Tính toán số lượng item trong giỏ (để cập nhật icon giỏ hàng)
            int cartSize = gioHang.getGioHangChiTiets().stream()
                    .mapToInt(GioHangChiTiet::getSoLuong)
                    .sum();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng!");
            response.put("cartSize", cartSize);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage()); // e.g., "Không đủ hàng..."
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * CẬP NHẬT số lượng sản phẩm (dùng cho AJAX).
     * (Giữ nguyên)
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateItemQuantity(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong
            /* @AuthenticationPrincipal NguoiDung khachHang */) {

        // ... (Code logic của hàm update giữ nguyên) ...
        return null; // Tạm thời
    }

    /**
     * XÓA sản phẩm khỏi giỏ (dùng cho AJAX).
     * (Giữ nguyên)
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItemFromCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet
            /* @AuthenticationPrincipal NguoiDung khachHang */) {

        // ... (Code logic của hàm remove giữ nguyên) ...
        return null; // Tạm thời
    }

    // === CÁC HÀM XỬ LÝ VOUCHER ===

    /**
     * ÁP DỤNG MÃ GIẢM GIÁ
     */
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam("maGiamGia") String maGiamGia,
                               RedirectAttributes redirectAttributes
            /* @AuthenticationPrincipal NguoiDung khachHang */) {
        // TODO: Lấy NguoiDung thật
        NguoiDung khachHang = new NguoiDung();
        khachHang.setId(2L);

        try {
            gioHangClientService.applyVoucher(khachHang, maGiamGia);
            redirectAttributes.addFlashAttribute("successMessage", "Áp dụng mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * GỠ MÃ GIẢM GIÁ
     */
    @PostMapping("/remove-voucher")
    public String removeVoucher(RedirectAttributes redirectAttributes
            /* @AuthenticationPrincipal NguoiDung khachHang */) {
        // TODO: Lấy NguoiDung thật
        NguoiDung khachHang = new NguoiDung();
        khachHang.setId(2L);

        try {
            gioHangClientService.removeVoucher(khachHang);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ mã giảm giá.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }
}