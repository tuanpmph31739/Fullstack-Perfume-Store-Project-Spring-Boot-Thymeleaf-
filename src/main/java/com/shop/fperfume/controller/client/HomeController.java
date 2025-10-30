package com.shop.fperfume.controller.client;

import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.service.admin.SanPhamChiTietService;
import com.shop.fperfume.service.client.SanPhamClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private SanPhamClientService sanPhamClientService;

    @GetMapping("/")
    public String home(Model model) {
        List<SanPhamChiTietResponse> sanPhams = sanPhamClientService.getSanPhamNoiBat();
        List<List<SanPhamChiTietResponse>> sanPhamChunks = new ArrayList<>();
        int chunkSize = 6;
        for (int i = 0; i < sanPhams.size(); i += chunkSize) {
            sanPhamChunks.add(
                    sanPhams.subList(i, Math.min(i + chunkSize, sanPhams.size()))
            );
        }

        model.addAttribute("sanPhamChunks", sanPhamChunks); // gửi xuống view
        return "client/index";
    }

}
