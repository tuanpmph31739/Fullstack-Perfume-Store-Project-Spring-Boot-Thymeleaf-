package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.NhomHuongRequest;
import com.shop.fperfume.model.response.NhomHuongResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.service.admin.NhomHuongService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nhom-huong")
public class NhomHuongController {

    @Autowired
    private NhomHuongService nhomHuongService;

    private final int PAGE_SIZE = 15;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<NhomHuongResponse> page = nhomHuongService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/nhom-huong");

        return "admin/nhom_huong/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("nhomHuongRequest", new NhomHuongRequest());
        model.addAttribute("currentPath", "/admin/nhom-huong");
        return "admin/nhom_huong/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("nhomHuongRequest") NhomHuongRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/nhom-huong");
            return "admin/nhom_huong/add";
        }

        try {
            nhomHuongService.addNhomHuong(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhóm hương thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã nhóm hương")) {
                bindingResult.rejectValue("maNhomHuong", "error.maNhomHuong", e.getMessage());
            } else if (e.getMessage().contains("Tên nhóm hương")) {
                bindingResult.rejectValue("tenNhomHuong", "error.tenNhomHuong", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/nhom-huong");
            return "admin/nhom_huong/add";
        }

        return "redirect:/admin/nhom-huong";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            NhomHuongResponse responseDto = nhomHuongService.getNhomHuongById(id);
            NhomHuongRequest requestDto = MapperUtils.map(responseDto, NhomHuongRequest.class);

            model.addAttribute("nhomHuongRequest", requestDto);
            model.addAttribute("currentPath", "/admin/nhom-huong");
            return "admin/nhom_huong/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhóm hương!");
            return "redirect:/admin/nhom-huong";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("nhomHuongRequest") NhomHuongRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/nhom-huong");
            return "admin/nhom_huong/edit";
        }

        try {
            nhomHuongService.updateNhomHuong(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhóm hương thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã nhóm hương")) {
                bindingResult.rejectValue("maNhomHuong", "error.maNhomHuong", e.getMessage());
            } else if (e.getMessage().contains("Tên nhóm hương")) {
                bindingResult.rejectValue("tenNhomHuong", "error.tenNhomHuong", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/nhom-huong");
            return "admin/nhom_huong/edit";
        }

        return "redirect:/admin/nhom-huong";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            nhomHuongService.deleteNhomHuong(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhóm hương thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Nhóm hương này đang được sử dụng.");
        }
        return "redirect:/admin/nhom-huong";
    }
}
