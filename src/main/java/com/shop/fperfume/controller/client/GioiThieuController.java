package com.shop.fperfume.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GioiThieuController {

    @GetMapping("/gioi-thieu")
    public String gioiThieu() {
        return "client/gioi_thieu/index"; // trỏ tới file index.html trong thư mục templates/client/
    }
}
