package com.shop.fperfume.controller.client;

import com.shop.fperfume.DTO.CheckoutRequestDTO;
import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.service.client.HoaDonClientService;
//import com.shop.fperfume.service.VnPayService; // (Chúng ta sẽ tạo service này ở bước sau)
//import com.shop.fperfume.service.EmailService; // (Service gửi mail)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Dùng để lấy user đăng nhập
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order") // Tiền tố chung cho các URL liên quan đến đơn hàng
public class OrderController {

    @Autowired
    private HoaDonClientService hoaDonClientService;

    // (Chúng ta sẽ @Autowired 2 service này ở bước tiếp theo)
    // @Autowired
    // private VnPayService vnPayService;

    // @Autowired
    // private EmailService emailService;

    /**
     * Xử lý khi người dùng nhấn nút "Hoàn tất Đặt hàng" từ form checkout.
     */
    @PostMapping("/submit")
    public Object submitOrder(
            @ModelAttribute("checkoutForm") CheckoutRequestDTO checkoutInfo,
            // @AuthenticationPrincipal NguoiDung khachHang, // <-- Cách lấy user lý tưởng nếu dùng Spring Security
            HttpServletRequest request, // Cần cho VNPay
            RedirectAttributes redirectAttributes) {

        // --- BƯỚC TẠM THỜI: Lấy user (Vì chưa có Spring Security) ---
        // TODO: Thay thế bằng @AuthenticationPrincipal khi có Spring Security
        // Giả sử chúng ta lấy user_id=1 (Admin) để test
        NguoiDung khachHang = new NguoiDung(); // Cần thay thế
        khachHang.setId(1L); // <<<<<<<<<<< GIẢ LẬP USER ID = 1
        // (Bạn cần một hàm NguoiDungRepository.findById(1L) ở đây)
        // --- HẾT BƯỚC TẠM THỜI ---

        try {
            // === 1. GỌI SERVICE ĐỂ TẠO HÓA ĐƠN ===
            // Toàn bộ logic (tính tiền, kiểm tra kho) nằm trong service này.
            HoaDon hoaDon = hoaDonClientService.createOrderFromCart(khachHang, checkoutInfo);

            // === 2. XỬ LÝ KẾT QUẢ (PHỤ THUỘC VÀO THANH TOÁN) ===
            String trangThai = hoaDon.getTrangThai();

            if (trangThai.equals("CHỜ XÁC NHẬN")) {
                //
                // ----- LÃNH VỰC CỦA COD (VÀ EMAIL) -----
                //

                // (Tùy chọn: Gửi email xác nhận ngay lập tức)
                // emailService.sendOrderConfirmationEmail(hoaDon);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Đặt hàng thành công! Mã đơn hàng của bạn là: " + hoaDon.getMa());

                // Trả về một trang HTML "Đặt hàng thành công"
                return "redirect:/order/success";

            } else if (trangThai.equals("ĐANG CHỜ THANH TOÁN")) {
                //
                // ----- LÃNH VỰC CỦA VNPAY -----
                //

                // (Code này sẽ chạy ở bước tiếp theo, khi chúng ta tạo VnPayService)

                // String paymentUrl = vnPayService.createPaymentUrl(hoaDon, request);

                // Chuyển hướng trình duyệt của người dùng sang cổng VNPay
                // return new RedirectView(paymentUrl);

                // Tạm thời, vì chưa có VnPayService, chúng ta báo lỗi
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Chức năng VNPay đang được bảo trì. Vui lòng chọn COD.");
                return "redirect:/cart";

            } else {
                // Trường hợp không mong muốn
                redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái đơn hàng không xác định.");
                return "redirect:/cart";
            }

        } catch (RuntimeException e) {
            // === 3. XỬ LÝ LỖI (VÍ DỤ: HẾT HÀNG) ===
            // Bất kỳ lỗi nào (như "Hết hàng") từ Service sẽ được bắt ở đây.
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            // Quay trở lại giỏ hàng và hiển thị lỗi
            return "redirect:/cart";
        }
    }

    // (Bạn cũng cần tạo một @GetMapping("/order/success") để hiển thị trang HTML thành công)
}