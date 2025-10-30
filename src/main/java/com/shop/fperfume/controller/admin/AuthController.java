package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.service.admin.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
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
    public String register(@ModelAttribute NguoiDung user, RedirectAttributes redirect) {
        if (nguoiDungRepository.findByEmail(user.getEmail()).isPresent()) {
            redirect.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/register";
        }


        if (user.getMa() == null || user.getMa().isEmpty()) {
            user.setMa("ND" + System.currentTimeMillis());
        }

        user.setMatKhau(passwordEncoder.encode(user.getMatKhau()));
        nguoiDungRepository.save(user);

        redirect.addFlashAttribute("success", "Đăng ký thành công!");
        return "redirect:/login";
    }


    @GetMapping("/verify")
    public String verifyUser(@RequestParam("code") String code, Model model) {
        boolean verified = nguoiDungService.verify(code);
        model.addAttribute("verified", verified);
        return "auth/verify_result";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "auth/logout_success";
    }
}