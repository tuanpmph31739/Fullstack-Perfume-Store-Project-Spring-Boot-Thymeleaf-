package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.GioHangChiTiet;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Giỏ hàng hỗ trợ:
 * - User đăng nhập: dùng DB (GioHang, GioHangChiTiet)
 * - Khách vãng lai: dùng Session (Map<Integer, Integer> = <idSpct, soLuong>)
 */
@Controller
@RequestMapping("/cart")
public class GioHangController {

    private static final String SESSION_CART_KEY = "GUEST_CART";

    @Autowired
    private GioHangClientService gioHangClientService;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    /* =========================================================
     * 1. HIỂN THỊ GIỎ HÀNG
     * ========================================================= */
    @GetMapping
    public String viewCart(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           HttpSession session) {

        GioHang gioHangToView;
        Map<String, Object> cartData;

        if (userDetails != null) {
            // --- ĐÃ ĐĂNG NHẬP: dùng giỏ DB như cũ ---
            NguoiDung khachHang = userDetails.getUser();
            GioHang gioHang = gioHangClientService.getCartByUser(khachHang);
            gioHangToView = gioHang;
            cartData = calculateCartData(gioHang);
        } else {
            // --- CHƯA ĐĂNG NHẬP: build GioHang "ảo" từ session ---
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> guestCart =
                    (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);

            gioHangToView = buildVirtualCartFromSession(guestCart);
            cartData = calculateCartData(gioHangToView);
        }

        model.addAttribute("gioHang", gioHangToView);
        model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
        model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
        model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));

        return "client/cart";
    }

    /* =========================================================
     * 2. THÊM SẢN PHẨM VÀO GIỎ (AJAX)
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
                // ===== ĐÃ ĐĂNG NHẬP → dùng service cũ =====
                NguoiDung khachHang = userDetails.getUser();
                GioHang gioHang = gioHangClientService.addItemToCart(khachHang, idSanPhamChiTiet, soLuong);
                Map<String, Object> response = createAjaxResponse(gioHang, "Đã thêm sản phẩm vào giỏ hàng!");
                System.out.println(">>> /cart/add (USER) spctId = " + idSanPhamChiTiet + ", soLuong = " + soLuong);
                return ResponseEntity.ok(response);

            } else {
                // ===== CHƯA ĐĂNG NHẬP → dùng SESSION =====
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> guestCart =
                        (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
                if (guestCart == null) {
                    guestCart = new HashMap<>();
                }

                SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                int tonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                int currentQty = guestCart.getOrDefault(idSanPhamChiTiet, 0);
                int totalDesired = currentQty + soLuong;

                if (tonKho <= 0) {
                    throw new RuntimeException("Sản phẩm đã hết hàng");
                }
                if (totalDesired > tonKho) {
                    int coTheThem = tonKho - currentQty;
                    if (coTheThem <= 0) {
                        throw new RuntimeException("Bạn đã thêm tối đa sản phẩm này trong giỏ. Tồn kho: " + tonKho);
                    }
                    throw new RuntimeException("Chỉ còn " + tonKho +
                            " sản phẩm. Bạn chỉ có thể thêm " + coTheThem + " sản phẩm nữa.");
                }

                // Cập nhật map trong session
                guestCart.put(idSanPhamChiTiet, totalDesired);
                session.setAttribute(SESSION_CART_KEY, guestCart);

                // Tạo GioHang ảo để tái sử dụng logic tính tổng / response
                GioHang virtualCart = buildVirtualCartFromSession(guestCart);
                Map<String, Object> response = createAjaxResponse(virtualCart, "Đã thêm sản phẩm vào giỏ hàng!");
                System.out.println(">>> /cart/add (GUEST) spctId = " + idSanPhamChiTiet + ", soLuong = " + soLuong);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 3. CẬP NHẬT SỐ LƯỢNG (AJAX)
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
                // ===== USER: dùng DB =====
                NguoiDung khachHang = userDetails.getUser();
                GioHang gioHang = gioHangClientService.updateItemQuantity(khachHang, idSanPhamChiTiet, soLuong);
                Map<String, Object> response = createAjaxResponse(gioHang, "Cập nhật số lượng thành công!");
                return ResponseEntity.ok(response);

            } else {
                // ===== GUEST: dùng SESSION =====
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> guestCart =
                        (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
                if (guestCart == null) {
                    guestCart = new HashMap<>();
                }

                if (soLuong <= 0) {
                    // Xóa khỏi giỏ
                    guestCart.remove(idSanPhamChiTiet);
                } else {
                    SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                    int tonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                    if (soLuong > tonKho) {
                        throw new RuntimeException("Số lượng cập nhật vượt quá tồn kho! (Tồn kho: " + tonKho + ")");
                    }
                    guestCart.put(idSanPhamChiTiet, soLuong);
                }

                session.setAttribute(SESSION_CART_KEY, guestCart);

                GioHang virtualCart = buildVirtualCartFromSession(guestCart);
                Map<String, Object> response = createAjaxResponse(virtualCart, "Cập nhật số lượng thành công!");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 4. XÓA SẢN PHẨM (AJAX)
     * ========================================================= */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItemFromCart(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session) {

        try {
            if (userDetails != null) {
                // ===== USER: DB =====
                NguoiDung khachHang = userDetails.getUser();
                GioHang gioHang = gioHangClientService.removeItemFromCart(khachHang, idSanPhamChiTiet);
                Map<String, Object> response = createAjaxResponse(gioHang, "Đã xóa sản phẩm khỏi giỏ hàng.");
                return ResponseEntity.ok(response);

            } else {
                // ===== GUEST: SESSION =====
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> guestCart =
                        (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
                if (guestCart == null) {
                    guestCart = new HashMap<>();
                }

                guestCart.remove(idSanPhamChiTiet);
                session.setAttribute(SESSION_CART_KEY, guestCart);

                GioHang virtualCart = buildVirtualCartFromSession(guestCart);
                Map<String, Object> response = createAjaxResponse(virtualCart, "Đã xóa sản phẩm khỏi giỏ hàng.");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* =========================================================
     * 5. VOUCHER (tạm thời vẫn yêu cầu đăng nhập)
     * ========================================================= */
    @PostMapping("/apply-voucher")
    @ResponseBody
    public ResponseEntity<?> applyVoucher(@RequestParam("maGiamGia") String maGiamGia,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
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
     * 6. HELPER: Build GioHang "ảo" từ session
     * ========================================================= */
    private GioHang buildVirtualCartFromSession(Map<Integer, Integer> guestCart) {
        GioHang gioHang = new GioHang();
        if (guestCart == null || guestCart.isEmpty()) {
            gioHang.setGioHangChiTiets(Collections.emptyList());
            return gioHang;
        }

        List<GioHangChiTiet> chiTietList = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : guestCart.entrySet()) {
            Integer spctId = entry.getKey();
            Integer soLuong = entry.getValue();

            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(spctId).orElse(null);
            if (spct == null) continue;

            GioHangChiTiet item = new GioHangChiTiet();
            // Id composite có thể để null, vì ta chỉ hiển thị, không save
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(spct);
            item.setSoLuong(soLuong);

            chiTietList.add(item);
        }

        gioHang.setGioHangChiTiets(chiTietList);
        // Guest không dùng voucher nên set null
        gioHang.setGiamGia(null);
        return gioHang;
    }

    /* =========================================================
     * 7. HELPER: Tính toán tiền
     * ========================================================= */
    private Map<String, Object> calculateCartData(GioHang gioHang) {
        BigDecimal tongTienHang = BigDecimal.ZERO;
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        BigDecimal tongThanhToan;
        int cartSize = 0;

        if (gioHang != null && gioHang.getGioHangChiTiets() != null && !gioHang.getGioHangChiTiets().isEmpty()) {

            for (GioHangChiTiet item : gioHang.getGioHangChiTiets()) {
                cartSize += item.getSoLuong();

                if (item.getSanPhamChiTiet() != null && item.getSanPhamChiTiet().getGiaBan() != null) {
                    BigDecimal giaBan = item.getSanPhamChiTiet().getGiaBan();
                    BigDecimal soLuong = new BigDecimal(item.getSoLuong());
                    tongTienHang = tongTienHang.add(giaBan.multiply(soLuong));
                }
            }

            GiamGia giamGia = gioHang.getGiamGia();
            if (giamGia != null) {
                if ("PERCENT".equals(giamGia.getLoaiGiam())) {
                    tienGiamGia = tongTienHang.multiply(giamGia.getGiaTri().divide(new BigDecimal(100)));
                } else {
                    tienGiamGia = giamGia.getGiaTri();
                }
                tienGiamGia = tienGiamGia.min(tongTienHang);
            }
        }

        tongThanhToan = tongTienHang.subtract(tienGiamGia);

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("tongTienHang", tongTienHang);
        cartData.put("tienGiamGia", tienGiamGia);
        cartData.put("tongThanhToan", tongThanhToan);
        cartData.put("cartSize", cartSize);

        return cartData;
    }

    /* =========================================================
     * 8. HELPER: Tạo JSON trả về cho AJAX
     * ========================================================= */
    private Map<String, Object> createAjaxResponse(GioHang gioHang, String message) {
        Map<String, Object> cartData = calculateCartData(gioHang);

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
