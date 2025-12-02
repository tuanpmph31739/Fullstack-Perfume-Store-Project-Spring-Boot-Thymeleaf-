package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.service.admin.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

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

        user.setMa("ND" + System.currentTimeMillis());

        try {
            String siteURL = "http://" + host;
            nguoiDungService.register(user, siteURL);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", "Không gửi được email xác minh: " + e.getMessage());
            return "redirect:/register";
        }

        redirect.addFlashAttribute("success", "Đăng ký thành công! Vui lòng kiểm tra email để xác minh.");
        return "redirect:/register";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("code") String code, Model model) {
        boolean verified = nguoiDungService.verify(code);
        model.addAttribute("message",
                verified ? "Tài khoản của bạn đã được xác minh!"
                        : "Mã xác minh không hợp lệ hoặc đã được sử dụng.");
        return "auth/verify_result";
    }
}
