package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.service.admin.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/admin/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanController {

    private final NguoiDungService nguoiDungService; // vẫn dùng cho findByEmail nếu muốn
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/thong-tin")
    public String viewThongTinTaiKhoan(Model model,
                                       Authentication authentication,
                                       @ModelAttribute("success") String success,
                                       @ModelAttribute("passwordError") String passwordError) {
        String email = authentication.getName();

        Optional<NguoiDung> optUser = nguoiDungService.findByEmail(email);
        if (optUser.isEmpty()) return "redirect:/login";

        model.addAttribute("nguoiDung", optUser.get());
        return "admin/tai_khoan/thong-tin";
    }

    @PostMapping("/thong-tin")
    public String updateThongTinTaiKhoan(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("sdt") String sdt,
            @RequestParam(value = "ngaySinh", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate ngaySinh,
            @RequestParam(value = "gioiTinh", required = false) Integer gioiTinh,
            @RequestParam(value = "diaChi", required = false) String diaChi,

            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,

            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        String email = authentication.getName();
        NguoiDung current = nguoiDungService.findByEmail(email)
                .orElse(null);
        if (current == null) return "redirect:/login";

        // validate cơ bản giống client (tuỳ bạn giữ/bỏ)
        if (hoTen == null || hoTen.trim().length() < 3) {
            redirectAttributes.addFlashAttribute("passwordError", "Họ tên phải từ 3 ký tự trở lên.");
            return "redirect:/admin/tai-khoan/thong-tin";
        }

        String phone = (sdt != null) ? sdt.trim() : "";
        if (!phone.isBlank() && !phone.matches("^0(3|5|7|8|9)[0-9]{8}$")) {
            redirectAttributes.addFlashAttribute("passwordError", "Số điện thoại không hợp lệ.");
            return "redirect:/admin/tai-khoan/thong-tin";
        }

        // muốn đổi mật khẩu?
        boolean wantChangePassword =
                (currentPassword != null && !currentPassword.isBlank()) ||
                        (newPassword != null && !newPassword.isBlank()) ||
                        (confirmPassword != null && !confirmPassword.isBlank());

        if (wantChangePassword) {
            if (currentPassword == null || currentPassword.isBlank()
                    || newPassword == null || newPassword.isBlank()
                    || confirmPassword == null || confirmPassword.isBlank()) {
                redirectAttributes.addFlashAttribute("passwordError",
                        "Vui lòng nhập đủ mật khẩu hiện tại, mật khẩu mới và xác nhận.");
                return "redirect:/admin/tai-khoan/thong-tin";
            }

            if (!passwordEncoder.matches(currentPassword, current.getMatKhau())) {
                redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không chính xác.");
                return "redirect:/admin/tai-khoan/thong-tin";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới phải ít nhất 6 ký tự.");
                return "redirect:/admin/tai-khoan/thong-tin";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới và xác nhận không khớp.");
                return "redirect:/admin/tai-khoan/thong-tin";
            }

            current.setMatKhau(passwordEncoder.encode(newPassword));
        }

        // cập nhật thông tin
        current.setHoTen(hoTen.trim());
        current.setSdt(phone.isBlank() ? null : phone);
        current.setNgaySinh(ngaySinh);
        current.setGioiTinh(gioiTinh);
        current.setDiaChi(diaChi != null ? diaChi.trim() : null);

        nguoiDungRepository.save(current);

        redirectAttributes.addFlashAttribute("success", "Cập nhật tài khoản thành công!");
        return "redirect:/admin/tai-khoan/thong-tin";
    }

}
