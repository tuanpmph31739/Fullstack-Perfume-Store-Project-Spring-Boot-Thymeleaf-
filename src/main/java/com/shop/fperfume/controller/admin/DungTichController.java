package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.DungTichRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.service.admin.DungTichService;
import com.shop.fperfume.util.MapperUtils; // Dùng để map khi edit
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/dung-tich")
public class DungTichController {

    @Autowired
    private DungTichService dungTichService;

    private final int PAGE_SIZE = 15;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<DungTichResponse> page = dungTichService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/dung-tich");

        return "admin/dung_tich/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("dungTichRequest", new DungTichRequest());
        model.addAttribute("currentPath", "/admin/dung-tich");
        return "admin/dung_tich/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("dungTichRequest") DungTichRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/dung-tich");
            return "admin/dung_tich/add";
        }

        try {
            dungTichService.addDungTich(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm dung tích thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã dung tích")) {
                bindingResult.rejectValue("maDungTich", "error.maDungTich", e.getMessage());
            } else if (e.getMessage().contains("Số Ml")) {
                bindingResult.rejectValue("soMl", "error.soMl", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/dung-tich");
            return "admin/dung_tich/add";
        }

        return "redirect:/admin/dung-tich";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            DungTichResponse responseDto = dungTichService.getDungTichById(id);
            DungTichRequest requestDto = MapperUtils.map(responseDto, DungTichRequest.class);

            model.addAttribute("dungTichRequest", requestDto);
            model.addAttribute("currentPath", "/admin/dung-tich");
            return "admin/dung_tich/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dung tích!");
            return "redirect:/admin/dung-tich";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("dungTichRequest") DungTichRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/dung-tich");
            return "admin/dung_tich/edit";
        }

        try {
            dungTichService.updateDungTich(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật dung tích thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã dung tích")) {
                bindingResult.rejectValue("maDungTich", "error.maDungTich", e.getMessage());
            } else if (e.getMessage().contains("Số Ml")) {
                bindingResult.rejectValue("soMl", "error.soMl", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/dung-tich");
            return "admin/dung_tich/edit";
        }

        return "redirect:/admin/dung-tich";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            dungTichService.deleteDungTich(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa dung tích thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Dung tích này đang được sử dụng.");
        }
        return "redirect:/admin/dung-tich";
    }
}