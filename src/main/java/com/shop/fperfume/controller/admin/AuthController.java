package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.service.admin.NguoiDungService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;

@Controller
public class AuthController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute NguoiDung user,
                           RedirectAttributes redirect,
                           @RequestHeader("Host") String host) {

        if (nguoiDungRepository.findByEmail(user.getEmail()).isPresent()) {
            redirect.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/register";
        }

        // Sinh mã người dùng & mã xác minh
        user.setMa("ND" + System.currentTimeMillis());
        user.setMatKhau(passwordEncoder.encode(user.getMatKhau()));
        user.setEnabled(false); // chưa xác minh
        user.setVerificationCode(java.util.UUID.randomUUID().toString());

        nguoiDungRepository.save(user);

        // Gửi email xác minh
        String siteURL = "http://" + host;
        try {
            nguoiDungService.sendVerificationEmail(user, siteURL);
        } catch (MessagingException | UnsupportedEncodingException e) {
            redirect.addFlashAttribute("error", "Không gửi được email xác minh: " + e.getMessage());
            return "redirect:/register";
        }

        return "auth/register_success";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("code") String code, Model model) {
        boolean verified = nguoiDungService.verify(code);
        model.addAttribute("message",
                verified ? "Tài khoản của bạn đã được xác minh!"
                        : "Mã xác minh không hợp lệ hoặc đã được sử dụng.");
        return "auth/verify_result";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "auth/logout_success";
    }
}
