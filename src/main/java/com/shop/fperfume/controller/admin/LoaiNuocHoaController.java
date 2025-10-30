package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.LoaiNuocHoaRequest;
import com.shop.fperfume.model.response.LoaiNuocHoaResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.service.admin.LoaiNuocHoaService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/loai-nuoc-hoa")
public class LoaiNuocHoaController {
    @Autowired
    private LoaiNuocHoaService  loaiNuocHoaService;

    private final int PAGE_SIZE = 10;

    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<LoaiNuocHoaResponse> page = loaiNuocHoaService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");

        return "admin/loai_nuoc_hoa/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("loaiNuocHoaRequest", new LoaiNuocHoaRequest());
        model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");
        return "admin/loai_nuoc_hoa/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("loaiNuocHoaRequest") LoaiNuocHoaRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");
            return "admin/loai_nuoc_hoa/add";
        }

        try {
            loaiNuocHoaService.addLoaiNuocHoa(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm loại nước hoa thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Tên loại nước hoa")) {
                bindingResult.rejectValue("tenLoai", "error.tenLoai", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");
            return "admin/loai_nuoc_hoa/add";
        }

        return "redirect:/admin/loai-nuoc-hoa";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            LoaiNuocHoaResponse responseDto = loaiNuocHoaService.getLoaiNuocHoaById(id);
            LoaiNuocHoaRequest requestDto = MapperUtils.map(responseDto, LoaiNuocHoaRequest.class);

            model.addAttribute("loaiNuocHoaRequest", requestDto);
            model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");
            return "admin/loai_nuoc_hoa/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại nước hoa!");
            return "redirect:/admin/loai-nuoc-hoa";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("loaiNuocHoaRequest") LoaiNuocHoaRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");
            return "admin/loai_nuoc_hoa/edit";
        }

        try {
            loaiNuocHoaService.updateLoaiNuocHoa(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật loại nước hoa thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Tên loại nước hoa")) {
                bindingResult.rejectValue("tenLoai", "error.tenLoai", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/loai-nuoc-hoa");
            return "admin/loai_nuoc_hoa/edit";
        }

        return "redirect:/admin/loai-nuoc-hoa";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            loaiNuocHoaService.deleteLoaiNuocHoa(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại nước hoa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Loại nước hoa này đang được sử dụng.");
        }
        return "redirect:/admin/loai-nuoc-hoa";
    }
}
