package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import com.shop.fperfume.service.client.CartHelperService; // <<< THÊM IMPORT MỚI
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class GioHangController {

    private static final String SESSION_CART_KEY = "GUEST_CART";

    @Autowired
    private GioHangClientService gioHangClientService;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private CartHelperService cartHelperService; // <<< THÊM SERVICE MỚI

    /* =========================================================
     * 1. HIỂN THỊ GIỎ HÀNG (ĐÃ SỬA)
     * ========================================================= */
    @GetMapping
    public String viewCart(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           HttpSession session) {

        GioHang gioHangToView;
        Map<String, Object> cartData;

        if (userDetails != null) {
            // --- ĐÃ ĐĂNG NHẬP ---
            NguoiDung khachHang = userDetails.getUser();
            GioHang gioHang = gioHangClientService.getCartByUser(khachHang);
            gioHangToView = gioHang;
            cartData = cartHelperService.calculateCartData(gioHang); // <<< SỬA
        } else {
            // --- CHƯA ĐĂNG NHẬP ---
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> guestCart =
                    (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);

            gioHangToView = cartHelperService.buildVirtualCartFromSession(guestCart); // <<< SỬA
            cartData = cartHelperService.calculateCartData(gioHangToView); // <<< SỬA
        }

        model.addAttribute("gioHang", gioHangToView);
        model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
        model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
        model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));

        return "client/cart";
    }

    /* =========================================================
     * 2. THÊM SẢN PHẨM VÀO GIỎ (AJAX) (ĐÃ SỬA)
     * ========================================================= */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addItemToCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session) {

        try {
            if (soLuong == null || soLuong <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Số lượng phải lớn hơn 0"));
            }

            if (userDetails != null) {
                // ===== ĐÃ ĐĂNG NHẬP (Giữ nguyên) =====
                NguoiDung khachHang = userDetails.getUser();
                GioHang gioHang = gioHangClientService.addItemToCart(khachHang, idSanPhamChiTiet, soLuong);
                Map<String, Object> response = createAjaxResponse(gioHang, "Đã thêm sản phẩm vào giỏ hàng!");
                System.out.println(">>> /cart/add (USER) spctId = " + idSanPhamChiTiet + ", soLuong = " + soLuong);
                return ResponseEntity.ok(response);

            } else {
                // ===== CHƯA ĐĂNG NHẬP (Logic nghiệp vụ giữ nguyên) =====
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> guestCart =
                        (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
                if (guestCart == null) {
                    guestCart = new HashMap<>();
                }

                // (Logic kiểm tra tồn kho của GUEST giữ nguyên)
                SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                int tonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                int currentQty = guestCart.getOrDefault(idSanPhamChiTiet, 0);
                int totalDesired = currentQty + soLuong;
                if (tonKho <= 0) { throw new RuntimeException("Sản phẩm đã hết hàng"); }
                if (totalDesired > tonKho) {
                    int coTheThem = tonKho - currentQty;
                    if (coTheThem <= 0) { throw new RuntimeException("Bạn đã thêm tối đa sản phẩm này trong giỏ. Tồn kho: " + tonKho); }
                    throw new RuntimeException("Chỉ còn " + tonKho + " sản phẩm. Bạn chỉ có thể thêm " + coTheThem + " sản phẩm nữa.");
                }

                guestCart.put(idSanPhamChiTiet, totalDesired);
                session.setAttribute(SESSION_CART_KEY, guestCart);

                // Tạo GioHang ảo để tái sử dụng logic tính tổng / response
                GioHang virtualCart = cartHelperService.buildVirtualCartFromSession(guestCart); // <<< SỬA
                Map<String, Object> response = createAjaxResponse(virtualCart, "Đã thêm sản phẩm vào giỏ hàng!");
                System.out.println(">>> /cart/add (GUEST) spctId = " + idSanPhamChiTiet + ", soLuong = " + soLuong);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 3. CẬP NHẬT SỐ LƯỢNG (AJAX) (ĐÃ SỬA)
     * ========================================================= */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateItemQuantity(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("soLuong") Integer soLuong,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session) {

        try {
            if (soLuong == null) soLuong = 0;

            if (userDetails != null) {
                // ===== USER: (Giữ nguyên) =====
                NguoiDung khachHang = userDetails.getUser();
                GioHang gioHang = gioHangClientService.updateItemQuantity(khachHang, idSanPhamChiTiet, soLuong);
                Map<String, Object> response = createAjaxResponse(gioHang, "Cập nhật số lượng thành công!");
                return ResponseEntity.ok(response);

            } else {
                // ===== GUEST: (Logic nghiệp vụ giữ nguyên) =====
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> guestCart =
                        (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
                if (guestCart == null) { guestCart = new HashMap<>(); }

                if (soLuong <= 0) {
                    guestCart.remove(idSanPhamChiTiet);
                } else {
                    // (Logic kiểm tra tồn kho của GUEST giữ nguyên)
                    SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                    int tonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                    if (soLuong > tonKho) {
                        throw new RuntimeException("Số lượng cập nhật vượt quá tồn kho! (Tồn kho: " + tonKho + ")");
                    }
                    guestCart.put(idSanPhamChiTiet, soLuong);
                }

                session.setAttribute(SESSION_CART_KEY, guestCart);

                GioHang virtualCart = cartHelperService.buildVirtualCartFromSession(guestCart); // <<< SỬA
                Map<String, Object> response = createAjaxResponse(virtualCart, "Cập nhật số lượng thành công!");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 4. XÓA SẢN PHẨM (AJAX) (ĐÃ SỬA)
     * ========================================================= */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItemFromCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session) {

        try {
            if (userDetails != null) {
                // ===== USER: (Giữ nguyên) =====
                NguoiDung khachHang = userDetails.getUser();
                GioHang gioHang = gioHangClientService.removeItemFromCart(khachHang, idSanPhamChiTiet);
                Map<String, Object> response = createAjaxResponse(gioHang, "Đã xóa sản phẩm khỏi giỏ hàng.");
                return ResponseEntity.ok(response);

            } else {
                // ===== GUEST: (Giữ nguyên) =====
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> guestCart =
                        (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
                if (guestCart == null) { guestCart = new HashMap<>(); }

                guestCart.remove(idSanPhamChiTiet);
                session.setAttribute(SESSION_CART_KEY, guestCart);

                GioHang virtualCart = cartHelperService.buildVirtualCartFromSession(guestCart); // <<< SỬA
                Map<String, Object> response = createAjaxResponse(virtualCart, "Đã xóa sản phẩm khỏi giỏ hàng.");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 5. VOUCHER (Giữ nguyên)
     * ========================================================= */
    @PostMapping("/apply-voucher")
    @ResponseBody
    public ResponseEntity<?> applyVoucher(@RequestParam("maGiamGia") String maGiamGia,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        // (Giữ nguyên logic)
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Chỉ khách hàng thành viên có thể sử dụng mã giảm giá!"));
        }
        NguoiDung khachHang = userDetails.getUser();
        try {
            gioHangClientService.applyVoucher(khachHang, maGiamGia);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/remove-voucher")
    @ResponseBody
    public ResponseEntity<?> removeVoucher(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // (Giữ nguyên logic)
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        NguoiDung khachHang = userDetails.getUser();
        try {
            gioHangClientService.removeVoucher(khachHang);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 6. HELPER: Build GioHang "ảo" (XÓA BỎ)
     * ========================================================= */
    // private GioHang buildVirtualCartFromSession(...) { ... } // <<< ĐÃ XÓA

    /* =========================================================
     * 7. HELPER: Tính toán tiền (ĐÃ SỬA)
     * ========================================================= */
    private Map<String, Object> calculateCartData(GioHang gioHang) {
        // <<< SỬA: Gọi service helper >>>
        return cartHelperService.calculateCartData(gioHang);
    }

    /* =========================================================
     * 8. HELPER: Tạo JSON trả về cho AJAX (ĐÃ SỬA)
     * ========================================================= */
    private Map<String, Object> createAjaxResponse(GioHang gioHang, String message) {
        // <<< SỬA: Gọi service helper >>>
        Map<String, Object> cartData = cartHelperService.calculateCartData(gioHang);

        Map<String, Object> cartSummary = new HashMap<>();
        cartSummary.put("subtotal", cartData.get("tongTienHang"));
        cartSummary.put("discount", cartData.get("tienGiamGia"));
        cartSummary.put("total", cartData.get("tongThanhToan"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("cartSize", cartData.get("cartSize"));
        response.put("cartSummary", cartSummary);

        return response;
    }
}