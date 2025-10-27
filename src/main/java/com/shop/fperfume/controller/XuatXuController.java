package com.shop.fperfume.controller;

import com.shop.fperfume.model.request.DungTichRequest;
import com.shop.fperfume.model.request.XuatXuRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.XuatXuResponse;
import com.shop.fperfume.service.XuatXuService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/xuat-xu")
public class XuatXuController {
    @Autowired
    private XuatXuService xuatXuService;

    private final int PAGE_SIZE = 10;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<XuatXuResponse> page = xuatXuService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/xuat-xu");

        return "admin/xuat_xu/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("xuatXuRequest", new XuatXuRequest());
        model.addAttribute("currentPath", "/admin/xuat-xu");
        return "admin/xuat_xu/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("xuatXuRequest") XuatXuRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/xuat-xu");
            return "admin/xuat_xu/add";
        }

        try {
            xuatXuService.addXuatXu(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nơi xuất xứ thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã xuất xứ")) {
                bindingResult.rejectValue("maXuatXu", "error.maXuatXu", e.getMessage());
            } else if (e.getMessage().contains("Nơi xuất xứ")) {
                bindingResult.rejectValue("tenXuatXu", "error.tenXuatXu", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/xuat-xu");
            return "admin/xuat_xu/add";
        }

        return "redirect:/admin/xuat-xu";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            XuatXuResponse responseDto = xuatXuService.getXuatXuById(id);
            XuatXuRequest requestDto = MapperUtils.map(responseDto, XuatXuRequest.class);

            model.addAttribute("xuatXuRequest", requestDto);
            model.addAttribute("currentPath", "/admin/xuat-xu");
            return "admin/xuat_xu/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nơi xuất xứ!");
            return "redirect:/admin/xuat-xu";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("xuatXuRequest") XuatXuRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/xuat-xu");
            return "admin/xuat_xu/edit";
        }

        try {
            xuatXuService.updateXuatXu(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nơi xuất xứ thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã xuất xứ")) {
                bindingResult.rejectValue("maXuatXu", "error.maXuatXu", e.getMessage());
            } else if (e.getMessage().contains("Nơi xuất xứ")) {
                bindingResult.rejectValue("tenXuatXu", "error.tenXuatXu", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/xuat-xu");
            return "admin/xuat_xu/edit";
        }

        return "redirect:/admin/xuat-xu";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            xuatXuService.deleteXuatXu(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nơi xuất xứ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Nơi xuất xứ này đang được sử dụng.");
        }
        return "redirect:/admin/xuat-xu";
    }
}
