package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.GiamGiaRequest;
import com.shop.fperfume.model.response.GiamGiaResponse;
import com.shop.fperfume.service.admin.GiamGiaService;
import com.shop.fperfume.service.admin.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/giam-gia")
public class GiamGiaController {

    @Autowired
    private GiamGiaService giamGiaService;

    @Autowired
    private SanPhamService sanPhamService; // Để hiển thị danh sách sản phẩm khi chọn

    // Hiển thị danh sách giảm giá
    @GetMapping
    public String index(Model model) {
        model.addAttribute("giamGias", giamGiaService.getAllGiamGia());
        return "admin/giam_gia/index";
    }

    // Trang thêm mới
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("giamGiaRequest", new GiamGiaRequest());
        model.addAttribute("sanPhams", sanPhamService.getAllSanPham());
        return "admin/giam_gia/add";
    }

    // Xử lý thêm mới
    @PostMapping("/add")
    public String addGiamGia(@ModelAttribute GiamGiaRequest giamGiaRequest,
                             RedirectAttributes redirectAttributes) {
        try {
            giamGiaService.addGiamGia(giamGiaRequest);
            redirectAttributes.addFlashAttribute("success", "Thêm giảm giá thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/giam-gia/add";
        }
        return "redirect:/admin/giam-gia";
    }

    // Trang chỉnh sửa
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            GiamGiaResponse giamGiaResponse = giamGiaService.getGiamGiaById(id);
            model.addAttribute("giamGiaRequest", giamGiaResponse);
            model.addAttribute("sanPhams", sanPhamService.getAllSanPham());
            return "admin/giam_gia/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/giam-gia";
        }
    }

    // Xử lý cập nhật
    @PostMapping("/edit/{id}")
    public String updateGiamGia(@PathVariable Long id,
                                @ModelAttribute GiamGiaRequest giamGiaRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            giamGiaService.updateGiamGia(id, giamGiaRequest);
            redirectAttributes.addFlashAttribute("success", "Cập nhật giảm giá thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/giam-gia/edit/" + id;
        }
        return "redirect:/admin/giam-gia";
    }

    // Xóa giảm giá
    @GetMapping("/delete/{id}")
    public String deleteGiamGia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            giamGiaService.deleteGiamGia(id);
            redirectAttributes.addFlashAttribute("success", "Xóa giảm giá thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/giam-gia";
    }
}
