package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.security.CustomUserDetails; // <-- THÊM IMPORT NÀY
import com.shop.fperfume.service.client.GioHangClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class GioHangController {

    @Autowired
    private GioHangClientService gioHangClientService;

    /**
     * HIỂN THỊ trang giỏ hàng (ĐÃ SỬA: Dùng khách hàng thật)
     */
    @GetMapping
    public String viewCart(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- LẤY USER THẬT

        // === SỬA CODE GIẢ LẬP ===
        if (userDetails == null) {
            // (Nếu muốn cho khách vãng lai xem giỏ hàng session, xử lý ở đây)
            // Tạm thời, yêu cầu đăng nhập
            return "redirect:/login";
        }
        NguoiDung khachHang = userDetails.getUser(); // <-- Lấy NguoiDung từ CustomUserDetails
        // ======================

        GioHang gioHang = gioHangClientService.getCartByUser(khachHang);

        // === (Logic tính toán giữ nguyên) ===
        BigDecimal tongTienHang = BigDecimal.ZERO;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        BigDecimal tongThanhToan = BigDecimal.ZERO;

        if (gioHang != null && gioHang.getGioHangChiTiets() != null && !gioHang.getGioHangChiTiets().isEmpty()) {
            // 1. Tính Tổng Tiền Hàng
            for (GioHangChiTiet item : gioHang.getGioHangChiTiets()) {
                if (item.getSanPhamChiTiet() != null && item.getSanPhamChiTiet().getGiaBan() != null) {
                    BigDecimal giaBan = item.getSanPhamChiTiet().getGiaBan();
                    BigDecimal soLuong = new BigDecimal(item.getSoLuong());
                    tongTienHang = tongTienHang.add(giaBan.multiply(soLuong));
                }
            }

            // 2. Tính Tiền Giảm Giá
            GiamGia giamGia = gioHang.getGiamGia();
            if (giamGia != null) {
                if ("PERCENT".equals(giamGia.getLoaiGiam())) {
                    tienGiamGia = tongTienHang.multiply(giamGia.getGiaTri().divide(new BigDecimal(100)));
                } else { // "AMOUNT"
                    tienGiamGia = giamGia.getGiaTri();
                }
                tienGiamGia = tienGiamGia.min(tongTienHang);
            }
        }

        // 3. Tính Tổng Thanh Toán
        tongThanhToan = tongTienHang.subtract(tienGiamGia);
        // ======================

        model.addAttribute("gioHang", gioHang);
        model.addAttribute("tongTienHang", tongTienHang);
        model.addAttribute("tienGiamGia", tienGiamGia);
        model.addAttribute("tongThanhToan", tongThanhToan);

        return "client/cart";
    }

    /**
     * THÊM sản phẩm vào giỏ (ĐÃ SỬA: Dùng khách hàng thật)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addItemToCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- LẤY USER THẬT

        // === SỬA CODE GIẢ LẬP ===
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();
        // ======================

        try {
            GioHang gioHang = gioHangClientService.addItemToCart(khachHang, idSanPhamChiTiet, soLuong);

            int cartSize = gioHang.getGioHangChiTiets().stream()
                    .mapToInt(GioHangChiTiet::getSoLuong)
                    .sum();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng!");
            response.put("cartSize", cartSize);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * CẬP NHẬT số lượng (ĐÃ SỬA: Dùng khách hàng thật)
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateItemQuantity(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- LẤY USER THẬT

        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();

        try {
            gioHangClientService.updateItemQuantity(khachHang, idSanPhamChiTiet, soLuong);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật số lượng thành công!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * XÓA sản phẩm (ĐÃ SỬA: Dùng khách hàng thật)
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItemFromCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- LẤY USER THẬT

        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();

        try {
            gioHangClientService.removeItemFromCart(khachHang, idSanPhamChiTiet);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam("maGiamGia") String maGiamGia,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- LẤY USER THẬT
        if (userDetails == null) return "redirect:/login";
        NguoiDung khachHang = userDetails.getUser();

        try {
            gioHangClientService.applyVoucher(khachHang, maGiamGia);
            redirectAttributes.addFlashAttribute("successMessage", "Áp dụng mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove-voucher")
    public String removeVoucher(RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- LẤY USER THẬT
        if (userDetails == null) return "redirect:/login";
        NguoiDung khachHang = userDetails.getUser();

        try {
            gioHangClientService.removeVoucher(khachHang);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ mã giảm giá.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }
}