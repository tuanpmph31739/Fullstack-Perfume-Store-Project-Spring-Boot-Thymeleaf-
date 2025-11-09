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

    private static final int CHUNK_SIZE = 6;

    private static <T> List<List<T>> chunk(List<T> source, int chunkSize) {
        List<List<T>> result = new ArrayList<>();
        if (source == null || source.isEmpty()) return result;
        for (int i = 0; i < source.size(); i += chunkSize) {
            result.add(source.subList(i, Math.min(i + chunkSize, source.size())));
        }
        return result;
    }

    @ModelAttribute("brands")
    public List<ThuongHieuResponse> getAllBrands() {
        return thuongHieuClientService.getAllThuongHieu();
    }

    @GetMapping("/")
    public String home(Model model) {

        var nam = sanPhamClientService.getSanPhamDaiDienTheoLoai("Nam");
        var nu = sanPhamClientService.getSanPhamDaiDienTheoLoai("Nữ");
        var unisex = sanPhamClientService.getSanPhamDaiDienTheoLoai("Unisex");

        var sanPhamNamChunks = chunk(nam, 6);
        var sanPhamNuChunks = chunk(nu, 6);
        var sanPhamUnisexChunks = chunk(unisex, 6);

        model.addAttribute("sanPhamNamChunks", sanPhamNamChunks);
        model.addAttribute("sanPhamNuChunks", sanPhamNuChunks);
        model.addAttribute("sanPhamUnisexChunks", sanPhamUnisexChunks);

        model.addAttribute("hasMoreNam", sanPhamNamChunks.size() > 1);
        model.addAttribute("hasMoreNu", sanPhamNuChunks.size() > 1);
        model.addAttribute("hasMoreUnisex", sanPhamUnisexChunks.size() > 1);

        return "client/index";
    }




    @GetMapping("/thuong-hieu/{slug}")
    public String viewBrandProducts(@PathVariable("slug") String slug, Model model) {
        ThuongHieuResponse thuongHieu = thuongHieuClientService.getBySlug(slug);
        if (thuongHieu == null) {
            return "redirect:/";
        }
        List<SanPhamChiTietResponse> sanPhams = sanPhamClientService.getSanPhamByThuongHieu(slug);
        model.addAttribute("thuongHieu", thuongHieu);
        model.addAttribute("sanPhams", sanPhams);
        return "client/thuong-hieu";
    }

    @GetMapping("/thuong-hieu/all")
    public String viewAllBrands(Model model) {
        List<ThuongHieuResponse> allBrands = thuongHieuClientService.getAllThuongHieu();
        model.addAttribute("brands", allBrands);
        return "client/thuong-hieu-tat-ca";
    }

    @ModelAttribute("brandsHot")
    public List<ThuongHieuResponse> getHotBrands() {
        return thuongHieuClientService.getAllThuongHieu()
                .stream()
                .limit(6)
                .toList();
    }
}
