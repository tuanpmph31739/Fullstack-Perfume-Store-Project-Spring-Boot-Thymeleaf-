package com.shop.fperfume.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // Khi truy cập /admin → hiển thị layout tổng
    @GetMapping("/admin")
    public String adminLayout() {
        return "admin/layout";
    }
}
