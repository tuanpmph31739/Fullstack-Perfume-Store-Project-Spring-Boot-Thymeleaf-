package com.shop.fperfume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorPageController {

    @GetMapping("/403")
    public String accessDenied() {
        // Trả về template: src/main/resources/templates/error/403.html
        return "error/403";
    }
}
