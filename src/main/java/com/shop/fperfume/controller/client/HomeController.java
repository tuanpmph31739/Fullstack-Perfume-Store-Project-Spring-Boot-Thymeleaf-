package com.shop.fperfume.controller.client;

import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import com.shop.fperfume.service.client.ThuongHieuClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private SanPhamClientService sanPhamClientService;

    @Autowired
    private ThuongHieuClientService thuongHieuClientService;

    @ModelAttribute("brands")
    public List<ThuongHieuResponse> getAllBrands() {
        return thuongHieuClientService.getAllThuongHieu();
    }

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
        model.addAttribute("sanPhamChunks", sanPhamChunks);
        return "client/index";
    }

    @GetMapping("/thuong-hieu/{slug}")
    public String viewBrandProducts(@PathVariable("slug") String slug, Model model) {
        // Lấy thông tin thương hiệu theo slug
        ThuongHieuResponse thuongHieu = thuongHieuClientService.getBySlug(slug);
        if (thuongHieu == null) {
            return "redirect:/"; // nếu không tồn tại thì về trang chủ
        }

        // Lấy danh sách sản phẩm của thương hiệu này
        List<SanPhamChiTietResponse> sanPhams = sanPhamClientService.getSanPhamByThuongHieu(slug);


        model.addAttribute("thuongHieu", thuongHieu);
        model.addAttribute("sanPhams", sanPhams);
        return "client/thuong-hieu";
    }

    @GetMapping("/thuong-hieu")
    public String viewAllBrands(Model model) {
        List<ThuongHieuResponse> allBrands = thuongHieuClientService.getAllThuongHieu();
        model.addAttribute("brands", allBrands);
        return "client/thuong-hieu-tat-ca"; // tạo file templates/client/thuong-hieu-tat-ca.html
    }

    @ModelAttribute("brandsHot")
    public List<ThuongHieuResponse> getHotBrands() {
        return thuongHieuClientService.getAllThuongHieu()
                .stream()
                .limit(6)
                .toList();
    }



}
