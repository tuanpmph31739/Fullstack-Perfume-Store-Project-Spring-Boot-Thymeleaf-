package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.GioHangClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * HIỂN THỊ trang giỏ hàng.
     * Đã tối ưu để gọi hàm tính toán chung.
     */
    @GetMapping
    public String viewCart(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        NguoiDung khachHang = userDetails.getUser();
        GioHang gioHang = gioHangClientService.getCartByUser(khachHang);

        // Gọi hàm helper để lấy dữ liệu tính toán
        Map<String, Object> cartData = calculateCartData(gioHang);

        model.addAttribute("gioHang", gioHang);
        model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
        model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
        model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));

        return "client/cart";
    }

    /**
     * THÊM sản phẩm vào giỏ (AJAX).
     * Trả về JSON chứa cartSize và cartSummary.
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addItemToCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();

        try {
            GioHang gioHang = gioHangClientService.addItemToCart(khachHang, idSanPhamChiTiet, soLuong);

            // Tạo response chuẩn bằng hàm helper
            Map<String, Object> response = createAjaxResponse(gioHang, "Đã thêm sản phẩm vào giỏ hàng!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * CẬP NHẬT số lượng (AJAX).
     * Trả về JSON chứa cartSize và cartSummary.
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateItemQuantity(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();

        try {
            GioHang gioHang = gioHangClientService.updateItemQuantity(khachHang, idSanPhamChiTiet, soLuong);

            // Tạo response chuẩn bằng hàm helper
            Map<String, Object> response = createAjaxResponse(gioHang, "Cập nhật số lượng thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * XÓA sản phẩm (AJAX).
     * Trả về JSON chứa cartSize và cartSummary.
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItemFromCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();

        try {
            GioHang gioHang = gioHangClientService.removeItemFromCart(khachHang, idSanPhamChiTiet);

            // Tạo response chuẩn bằng hàm helper
            Map<String, Object> response = createAjaxResponse(gioHang, "Đã xóa sản phẩm khỏi giỏ hàng.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    /* =====================================================================
     * CÁC HÀM VOUCHER (Giữ nguyên - Dùng cơ chế tải lại trang)
     * ===================================================================== */

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam("maGiamGia") String maGiamGia,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
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
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
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


    /* =====================================================================
     * HÀM HELPER TÍNH TOÁN VÀ TẠO RESPONSE
     * ===================================================================== */

    /**
     * HÀM HELPER 1: Chỉ tính toán và trả về dữ liệu.
     * Được sử dụng bởi cả `viewCart` và `createAjaxResponse`.
     */
    private Map<String, Object> calculateCartData(GioHang gioHang) {
        BigDecimal tongTienHang = BigDecimal.ZERO;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        BigDecimal tongThanhToan = BigDecimal.ZERO;
        int cartSize = 0; // Tổng số lượng CÁC SẢN PHẨM

        if (gioHang != null && gioHang.getGioHangChiTiets() != null && !gioHang.getGioHangChiTiets().isEmpty()) {

            // 1. Tính Tổng Tiền Hàng và Cart Size
            for (GioHangChiTiet item : gioHang.getGioHangChiTiets()) {
                cartSize += item.getSoLuong(); // Cộng dồn số lượng từng món

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
                // Đảm bảo tiền giảm không vượt quá tổng tiền hàng
                tienGiamGia = tienGiamGia.min(tongTienHang);
            }
        }

        // 3. Tính Tổng Thanh Toán
        tongThanhToan = tongTienHang.subtract(tienGiamGia);

        // 4. Đóng gói dữ liệu
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("tongTienHang", tongTienHang);
        cartData.put("tienGiamGia", tienGiamGia);
        cartData.put("tongThanhToan", tongThanhToan);
        cartData.put("cartSize", cartSize);

        return cartData;
    }

    /**
     * HÀM HELPER 2: Tạo response JSON chuẩn cho JavaScript.
     * Tái sử dụng hàm `calculateCartData`
     */
    private Map<String, Object> createAjaxResponse(GioHang gioHang, String message) {
        // Lấy dữ liệu tính toán
        Map<String, Object> cartData = calculateCartData(gioHang);

        // Tạo đối tượng cartSummary mà JavaScript cần
        Map<String, Object> cartSummary = new HashMap<>();
        cartSummary.put("subtotal", cartData.get("tongTienHang"));
        cartSummary.put("discount", cartData.get("tienGiamGia"));
        cartSummary.put("total", cartData.get("tongThanhToan"));

        // Tạo response JSON cuối cùng
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("cartSize", cartData.get("cartSize"));
        response.put("cartSummary", cartSummary); // Gửi cả tóm tắt về

        return response;
    }
}