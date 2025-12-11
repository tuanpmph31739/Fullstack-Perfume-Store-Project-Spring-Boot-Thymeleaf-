package com.shop.fperfume.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LienHeController {

    @GetMapping("/lien-he")
    public String lienHe() {
        return "client/lien_he/index";
    }
}
