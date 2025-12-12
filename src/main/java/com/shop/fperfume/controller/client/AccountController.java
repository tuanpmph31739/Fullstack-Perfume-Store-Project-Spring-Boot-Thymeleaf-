package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.HoaDonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private HoaDonClientService hoaDonService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ================== 1. LỊCH SỬ ĐƠN HÀNG ==================
    @GetMapping("/orders")
    public String orderHistory(Model model,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "fromDate", required = false) String fromDate,
                               @RequestParam(value = "toDate", required = false) String toDate,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return "redirect:/login";

        NguoiDung user = userDetails.getUser();
        List<HoaDon> orders = hoaDonService.getOrdersByUser(user, keyword, fromDate, toDate);

        model.addAttribute("orders", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "client/account/order-history";
    }

    // ================== 2. CHI TIẾT ĐƠN HÀNG ==================
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Integer id,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            HoaDon order = hoaDonService.getOrderDetailForUser(id, userDetails.getUser());
            model.addAttribute("order", order);
            return "client/account/order-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/orders";
        }
    }

    // ================== 3. HỦY ĐƠN HÀNG ==================
    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam("id") Integer id,
                              @RequestParam("lyDo") String lyDo,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            hoaDonService.cancelOrder(id, userDetails.getUser(), lyDo);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/account/orders/" + id;
    }

    // ================== 4. FORM ĐỊA CHỈ NGƯỜI DÙNG ==================
    @GetMapping("/address")
    public String showAddressForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        NguoiDung user = nguoiDungRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        model.addAttribute("user", user);
        return "client/account/address";   // -> templates/client/account/address.html
    }

    // ================== 5. LƯU ĐỊA CHỈ NGƯỜI DÙNG ==================
    @PostMapping("/address")
    public String updateAddress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("hoTen") String hoTen,
                                @RequestParam("sdt") String sdt,
                                @RequestParam("diaChi") String diaChi,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        NguoiDung user = nguoiDungRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setHoTen(hoTen != null ? hoTen.trim() : null);
        user.setSdt(sdt != null ? sdt.trim() : null);
        user.setDiaChi(diaChi != null ? diaChi.trim() : null);

        nguoiDungRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");
        return "redirect:/account/address";
    }

    @GetMapping("/profile")
    public String profile(Model model,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return "redirect:/login";

        NguoiDung user = userDetails.getUser();
        model.addAttribute("user", user);
        return "client/account/profile";
    }

    // ========== 5. CẬP NHẬT THÔNG TIN NGƯỜI DÙNG ==========
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("hoTen") String hoTen,
                                @RequestParam("sdt") String sdt,
                                @RequestParam(value = "ngaySinh", required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngaySinh,
                                @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        NguoiDung user = nguoiDungRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // --- VALIDATE HỌ TÊN ---
        if (hoTen == null || hoTen.trim().length() < 3) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ tên phải từ 3 ký tự trở lên.");
            return "redirect:/account/profile";
        }

        // --- VALIDATE SĐT ---
        String phone = sdt != null ? sdt.trim() : "";
        if (!phone.matches("^0(3|5|7|8|9)[0-9]{8}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không hợp lệ.");
            return "redirect:/account/profile";
        }

        // --- XỬ LÝ ĐỔI MẬT KHẨU (TÙY CHỌN) ---
        boolean wantChangePassword =
                (currentPassword != null && !currentPassword.isBlank()) ||
                        (newPassword != null && !newPassword.isBlank()) ||
                        (confirmPassword != null && !confirmPassword.isBlank());

        if (wantChangePassword) {
            // 1) Bắt buộc nhập đầy đủ 3 ô
            if (currentPassword == null || currentPassword.isBlank()
                    || newPassword == null || newPassword.isBlank()
                    || confirmPassword == null || confirmPassword.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu.");
                return "redirect:/account/profile";
            }

            // 2) Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(currentPassword, user.getMatKhau())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác.");
                return "redirect:/account/profile";
            }

            // 3) Kiểm tra độ dài mật khẩu mới
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải từ 6 ký tự trở lên.");
                return "redirect:/account/profile";
            }

            // 4) Kiểm tra trùng khớp
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
                return "redirect:/account/profile";
            }

            // 5) Cập nhật mật khẩu
            user.setMatKhau(passwordEncoder.encode(newPassword));
        }

        // --- CẬP NHẬT THÔNG TIN CƠ BẢN ---
        user.setHoTen(hoTen.trim());
        user.setSdt(phone);
        user.setNgaySinh(ngaySinh);

        nguoiDungRepository.save(user);

        // Cập nhật lại object trong session (CustomUserDetails)
        userDetails.getUser().setHoTen(user.getHoTen());
        userDetails.getUser().setSdt(user.getSdt());
        userDetails.getUser().setNgaySinh(user.getNgaySinh());

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin tài khoản thành công.");
        return "redirect:/account/profile";
    }
}
