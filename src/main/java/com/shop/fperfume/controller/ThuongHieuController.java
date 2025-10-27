package com.shop.fperfume.controller;

import com.shop.fperfume.model.request.DungTichRequest;
import com.shop.fperfume.model.request.ThuongHieuRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.ThuongHieuService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/thuong-hieu")
public class ThuongHieuController {
    @Autowired
    private ThuongHieuService thuongHieuService;

    private final int PAGE_SIZE = 10;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<ThuongHieuResponse> page = thuongHieuService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/thuong-hieu");

        return "admin/thuong_hieu/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("thuongHieuRequest", new ThuongHieuRequest());
        model.addAttribute("currentPath", "/admin/thuong-hieu");
        return "admin/thuong_hieu/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("thuongHieuRequest") ThuongHieuRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/add";
        }

        try {
            thuongHieuService.addThuongHieu(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thương hiệu thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã thương hiệu")) {
                bindingResult.rejectValue("maThuongHieu", "error.maThuongHieu", e.getMessage());
            } else if (e.getMessage().contains("Tên thương hiệu")) {
                bindingResult.rejectValue("tenThuongHieu", "error.tenThuongHieu", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/add";
        }

        return "redirect:/admin/thuong-hieu";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ThuongHieuResponse responseDto = thuongHieuService.getThuongHieuById(id);
            ThuongHieuRequest requestDto = MapperUtils.map(responseDto, ThuongHieuRequest.class);

            model.addAttribute("thuongHieuRequest", requestDto);
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thương hiệu!");
            return "redirect:/admin/thuong-hieu";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("thuongHieuRequest") ThuongHieuRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        request.setId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/edit";
        }

        try {
            thuongHieuService.updateThuongHieu(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thương hiệu thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã thương hiệu")) {
                bindingResult.rejectValue("maThuongHieu", "error.maThuongHieu", e.getMessage());
            } else if (e.getMessage().contains("Tên thương hiệu")) {
                bindingResult.rejectValue("tenThuongHieu", "error.tenThuongHieu", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/edit";
        }

        return "redirect:/admin/thuong-hieu";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            thuongHieuService.deleteThuongHieu(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Thương hiệu này đang được sử dụng.");
        }
        return "redirect:/admin/thuong-hieu";
    }
}
