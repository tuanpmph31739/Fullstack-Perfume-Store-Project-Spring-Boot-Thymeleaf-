package com.shop.fperfume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class testController {
    @GetMapping("/thong")
    public String hi() {
        return "admin/thong_ke/index";
    }
}
