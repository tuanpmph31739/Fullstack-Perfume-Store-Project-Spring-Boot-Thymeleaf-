package com.shop.fperfume.controller;

import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("listSanPham", sanPhamService.getAllSanPham());
        return "admin/san_pham/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("sanPhamRequest", new SanPhamRequest());
        // load dropdown (dung tích, loại, thương hiệu, xuất xứ)
        return "admin/san_pham/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("sanPhamRequest") SanPhamRequest request) {
        sanPhamService.addSanPham(request);
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable Long id, Model model) {
        // load data lên form sửa
        return "admin/san_pham/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("sanPhamRequest") SanPhamRequest request) {
        sanPhamService.updateSanPham(id, request);
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        sanPhamService.deleteSanPham(id);
        return "redirect:/admin/san-pham";
    }
}

