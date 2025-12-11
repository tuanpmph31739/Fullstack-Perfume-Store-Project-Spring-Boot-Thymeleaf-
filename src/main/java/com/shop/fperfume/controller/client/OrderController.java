package com.shop.fperfume.controller.client;

import com.shop.fperfume.dto.CheckoutRequestDTO;
import com.shop.fperfume.entity.GioHang;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.CartHelperService;
import com.shop.fperfume.service.client.GioHangClientService;
import com.shop.fperfume.service.client.HoaDonClientService;
import com.shop.fperfume.service.client.VnPayService;

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

    private static final String SESSION_CART_KEY = "GUEST_CART";

    @Autowired private HoaDonClientService hoaDonClientService;
    @Autowired private GioHangClientService gioHangClientService;
    @Autowired private CartHelperService cartHelperService;
    @Autowired private VnPayService vnPayService;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;


    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        GioHang cart;
        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setIdThanhToan(1L);

        if (userDetails != null) {

            Long userId = userDetails.getUser().getId();
            NguoiDung khachHang = nguoiDungRepository.findById(userId)
                    .orElse(userDetails.getUser());

            cart = gioHangClientService.getCartByUser(khachHang);
            checkoutRequest.setTenNguoiNhan(khachHang.getHoTen());
            checkoutRequest.setSdt(khachHang.getSdt());
            checkoutRequest.setDiaChi(khachHang.getDiaChi());
            checkoutRequest.setEmail(khachHang.getEmail());

            model.addAttribute("isLoggedIn", true);

        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> guestCartMap = (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
            if (guestCartMap == null || guestCartMap.isEmpty()) return "redirect:/cart";
            cart = cartHelperService.buildVirtualCartFromSession(guestCartMap);
        }

        if (cart.getGioHangChiTiets() == null || cart.getGioHangChiTiets().isEmpty()) {
            return "redirect:/cart";
        }

        Map<String, Object> cartData = cartHelperService.calculateCartData(cart);
        model.addAttribute("gioHang", cart);
        model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
        model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
        model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));
        model.addAttribute("checkoutForm", checkoutRequest);

        return "client/checkout";
    }


    @PostMapping("/submit")
    public Object submitOrder(
            @Validated @ModelAttribute("checkoutForm") CheckoutRequestDTO checkoutInfo,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request, HttpSession session,
            RedirectAttributes redirectAttributes, Model model) {

        GioHang cart;
        NguoiDung khachHang = (userDetails != null) ? userDetails.getUser() : null;

        if (khachHang != null) {
            cart = gioHangClientService.getCartByUser(khachHang);
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> guestCartMap = (Map<Integer, Integer>) session.getAttribute(SESSION_CART_KEY);
            cart = cartHelperService.buildVirtualCartFromSession(guestCartMap);
        }

        if (bindingResult.hasErrors()) {
            Map<String, Object> cartData = cartHelperService.calculateCartData(cart);
            model.addAttribute("gioHang", cart);
            model.addAttribute("tongTienHang", cartData.get("tongTienHang"));
            model.addAttribute("tienGiamGia", cartData.get("tienGiamGia"));
            model.addAttribute("tongThanhToan", cartData.get("tongThanhToan"));
            return "client/checkout";
        }

        if (cart.getGioHangChiTiets() == null || cart.getGioHangChiTiets().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng rỗng!");
            return "redirect:/cart";
        }

        try {
            // 1. TẠO HÓA ĐƠN (CHƯA XÓA GIỎ HÀNG VỘI)
            HoaDon hoaDon = hoaDonClientService.createOrder(cart, khachHang, checkoutInfo);
            String trangThai = hoaDon.getTrangThai();

            // 🟢 Nếu là khách đã đăng nhập, xử lý lưu địa chỉ mặc định
            if (khachHang != null) {
                String diaChiMoi = checkoutInfo.getDiaChi();
                boolean coDiaChiMoi = (diaChiMoi != null && !diaChiMoi.trim().isEmpty());

                if (coDiaChiMoi) {
                    boolean diaChiTrongDbDangNullHoacRong =
                            (khachHang.getDiaChi() == null || khachHang.getDiaChi().trim().isEmpty());

                    boolean userTickLuuMacDinh =
                            Boolean.TRUE.equals(checkoutInfo.getLuuDiaChiMacDinh());

                    // Điều kiện:
                    // 1. Nếu DiaChi trong DB đang null/rỗng -> auto lưu địa chỉ mới
                    // HOẶC
                    // 2. Nếu user tick "Lưu thành địa chỉ mặc định" -> lưu (ghi đè)
                    if (diaChiTrongDbDangNullHoacRong || userTickLuuMacDinh) {
                        khachHang.setDiaChi(diaChiMoi.trim());
                        nguoiDungRepository.save(khachHang);
                    }
                }
            }


            // 2. XỬ LÝ THEO LOẠI THANH TOÁN
            if (trangThai.equals("CHO_XAC_NHAN")) {
                // === COD (TIỀN MẶT) ===
                // Đặt xong là coi như thành công -> Xóa giỏ hàng NGAY
                if (khachHang != null) {
                    gioHangClientService.clearCart(khachHang);
                } else {
                    session.removeAttribute(SESSION_CART_KEY);
                }
                try {
                    gioHangClientService.GuiMailDonHang(hoaDon, checkoutInfo.getEmail());
                } catch (Exception e) {
                    System.err.println("Lỗi gửi mail: " + e.getMessage());
                }

                redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn: " + hoaDon.getMa());
                return "redirect:/order/success/" + hoaDon.getId();

            } else if (trangThai.equals("DANG_CHO_THANH_TOAN")) {
                // === VNPAY ===
                // GIỮ NGUYÊN GIỎ HÀNG (Để phòng trường hợp thất bại/hủy)
                // Việc xóa giỏ sẽ thực hiện ở PaymentController khi Success

                String paymentUrl = vnPayService.createPaymentUrl(hoaDon, request);
                return new RedirectView(paymentUrl);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái đơn hàng không xác định.");
                return "redirect:/cart";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/success/{id}")
    public String orderSuccess(@PathVariable("id") Integer hoaDonId, Model model) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) return "redirect:/";
        model.addAttribute("hoaDon", hoaDon);

        return "client/payment_success";
    }

    @GetMapping("/failure")
    public String orderFailure(Model model) {
        model.addAttribute("errorMessage", "Thanh toán không thành công hoặc đã bị hủy.");
        return "client/payment_failure";
    }
}