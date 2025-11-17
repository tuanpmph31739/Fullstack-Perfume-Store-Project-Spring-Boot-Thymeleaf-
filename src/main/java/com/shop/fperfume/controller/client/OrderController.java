package com.shop.fperfume.controller.client;

import com.shop.fperfume.DTO.CheckoutRequestDTO;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.HoaDonRepository; // <<< THÊM: Để dùng ở trang success
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.CartHelperService;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.service.client.HoaDonClientService;
import com.shop.fperfume.service.client.VnPayService; // <<< THÊM: Dịch vụ VNPay

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private static final String SESSION_CART_KEY = "GUEST_CART"; // Giống trong GioHangController

    // === Tiêm (Inject) tất cả service cần thiết ===
    @Autowired private HoaDonClientService hoaDonClientService;
    @Autowired private GioHangClientService gioHangClientService;
    @Autowired private CartHelperService cartHelperService;
    @Autowired private VnPayService vnPayService;
    @Autowired private HoaDonRepository hoaDonRepository; // Dùng cho trang success

    /**
     * HIỂN THỊ TRANG CHECKOUT
     * Logic "thông minh": Tự điền form nếu đã đăng nhập.
     */
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model,
                                   HttpSession session,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {

        GioHang cart;
        // Dùng DTO bạn đã cung cấp
        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();

        if (userDetails != null) {
            // === 1. ĐÃ ĐĂNG NHẬP ===
            NguoiDung khachHang = userDetails.getUser();
            cart = gioHangClientService.getCartByUser(khachHang);

            // TỰ ĐỘNG ĐIỀN FORM (Giả sử NguoiDung có các trường này)
            checkoutRequest.setTenNguoiNhan(khachHang.getHoTen());
            checkoutRequest.setSdt(khachHang.getSdt());
            checkoutRequest.setDiaChi(khachHang.getDiaChi());
            checkoutRequest.setEmail(khachHang.getEmail());

        } else {
            // === 2. LÀ KHÁCH (GUEST) ===
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> guestCartMap =
                    (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);

            if (guestCartMap == null || guestCartMap.isEmpty()) {
                return "redirect:/cart"; // Giỏ rỗng thì về giỏ hàng
            }
            cart = cartHelperService.buildVirtualCartFromSession(guestCartMap);
            // Form checkoutRequest sẽ rỗng
        }

        // Kiểm tra lại nếu giỏ hàng rỗng
        if (cart.getGioHangChiTiets() == null || cart.getGioHangChiTiets().isEmpty()) {
            return "redirect:/cart";
        }

        Map<String, Object> cartData = cartHelperService.calculateCartData(cart);

        model.addAttribute("gioHang", cart);
        model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
        model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
        model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));
        model.addAttribute("checkoutForm", checkoutRequest); // Gửi form ra view

        // TODO: Bạn cần lấy danh sách phương thức thanh toán và gửi qua model
        // model.addAttribute("phuongThucThanhToans", thanhToanRepository.findAll());

        return "client/checkout"; // Trả về trang checkout.html
    }


    /**
     * XỬ LÝ ĐẶT HÀNG (CHO CẢ GUEST VÀ USER)
     */
    @PostMapping("/submit")
    public Object submitOrder(
            @Validated @ModelAttribute("checkoutForm") CheckoutRequestDTO checkoutInfo,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        GioHang cart;
        NguoiDung khachHang = (userDetails != null) ? userDetails.getUser() : null;

        System.out.println("DEBUG: ID ThanhToan nhan duoc: " + checkoutInfo.getIdThanhToan());

        // === 1. LẤY LẠI GIỎ HÀNG (Giống hệt logic của GET) ===
        if (khachHang != null) {
            cart = gioHangClientService.getCartByUser(khachHang);
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> guestCartMap =
                    (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
            cart = cartHelperService.buildVirtualCartFromSession(guestCartMap);
        }

        // === 2. KIỂM TRA VALIDATION FORM ===
        // TODO: Thêm annotation (@NotBlank, @NotNull) vào file CheckoutRequestDTO.java
        if (bindingResult.hasErrors()) {
            // Nếu form lỗi, trả lại trang checkout và hiển thị lỗi
            Map<String, Object> cartData = cartHelperService.calculateCartData(cart);
            model.addAttribute("gioHang", cart);
            model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
            model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
            model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));

            // TODO: Nhớ thêm lại danh sách phương thức thanh toán
            // model.addAttribute("phuongThucThanhToans", thanhToanRepository.findAll());
            return "client/checkout"; // Trả về trang checkout.html
        }

        if (cart.getGioHangChiTiets() == null || cart.getGioHangChiTiets().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn bị rỗng!");
            return "redirect:/cart";
        }

        try {
            // === 3. GỌI SERVICE ĐỂ TẠO HÓA ĐƠN ===
            HoaDon hoaDon = hoaDonClientService.createOrder(cart, khachHang, checkoutInfo);

            // === 4. XÓA GIỎ HÀNG SAU KHI THÀNH CÔNG ===
            if (khachHang == null) {
                // Chỉ xóa session nếu là GUEST
                session.removeAttribute(SESSION_CART_KEY);
            }
            // (Nếu là USER, HoaDonServiceImpl đã tự động xóa giỏ hàng DB)

            // === 5. XỬ LÝ KẾT QUẢ (PHỤ THUỘC VÀO THANH TOÁN) ===
            String trangThai = hoaDon.getTrangThai();

            if (trangThai.equals("CHỜ XÁC NHẬN")) {
                // ----- LÃNH VỰC CỦA COD -----
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đặt hàng thành công! Mã đơn hàng của bạn là: " + hoaDon.getMa());
                return "redirect:/order/success/" + hoaDon.getId();

            } else if (trangThai.equals("ĐANG CHỜ THANH TOÁN")) {
                // ----- LÃNH VỰC CỦA VNPAY -----
                String paymentUrl = vnPayService.createPaymentUrl(hoaDon, request);
                // Chuyển hướng người dùng sang VNPay
                return new RedirectView(paymentUrl);

            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái đơn hàng không xác định.");
                return "redirect:/cart";
            }

        } catch (Exception e) {
            // === 6. XỬ LÝ LỖI (VÍ DỤ: HẾT HÀNG, LỖI TẠO URL) ===
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * THÊM: Trang hiển thị khi đặt hàng thành công (cho cả COD và VNPay)
     */
    @GetMapping("/success/{id}")
    public String orderSuccess(@PathVariable("id") Integer hoaDonId, Model model) {

        // Lấy lại hóa đơn để hiển thị thông tin
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            return "redirect:/"; // Không tìm thấy đơn
        }

        model.addAttribute("hoaDon", hoaDon);
        return "client/payment_success"; // (Bạn cần tạo trang HTML này)
    }

    /**
     * THÊM: Trang hiển thị khi thanh toán VNPay thất bại
     * (PaymentController sẽ redirect về đây)
     */
    @GetMapping("/failure")
    public String orderFailure(Model model) {
        model.addAttribute("errorMessage", "Thanh toán không thành công hoặc đã bị hủy.");
        return "client/payment_failure"; // (Bạn cần tạo trang HTML này)
    }
}