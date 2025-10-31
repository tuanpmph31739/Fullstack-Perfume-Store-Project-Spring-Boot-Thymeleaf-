package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.NongDoRequest;
import com.shop.fperfume.model.response.NongDoResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.service.admin.NongDoService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nong-do")
public class NongDoController {

    @Autowired
    private NongDoService nongDoService;

    private final int PAGE_SIZE = 15;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<NongDoResponse> page = nongDoService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/nong-do");

        return "admin/nong_do/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("nongDoRequest", new NongDoRequest());
        model.addAttribute("currentPath", "/admin/nong-do");
        return "admin/nong_do/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("nongDoRequest") NongDoRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/nong-do");
            return "admin/nong_do/add";
        }

        try {
            nongDoService.addNongDo(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nồng độ thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã nồng độ")) {
                bindingResult.rejectValue("maNongDo", "error.maNongDo", e.getMessage());
            } else if (e.getMessage().contains("Tên nồng độ")) {
                bindingResult.rejectValue("tenNongDo", "error.tenNongDo", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/nong-do");
            return "admin/nong_do/add";
        }

        return "redirect:/admin/nong-do";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            NongDoResponse responseDto = nongDoService.getNongDoById(id);
            NongDoRequest requestDto = MapperUtils.map(responseDto, NongDoRequest.class);

            model.addAttribute("nongDoRequest", requestDto);
            model.addAttribute("currentPath", "/admin/nong-do");
            return "admin/nong_do/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nồng độ!");
            return "redirect:/admin/nong-do";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("nongDoRequest") NongDoRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/nong-do");
            return "admin/nong_do/edit";
        }

        try {
            nongDoService.updateNongDo(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nồng độ thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã nồng độ")) {
                bindingResult.rejectValue("maNongDo", "error.maNongDo", e.getMessage());
            } else if (e.getMessage().contains("Tên nồng độ")) {
                bindingResult.rejectValue("tenNongDo", "error.tenNongDo", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/nong-do");
            return "admin/nong_do/edit";
        }

        return "redirect:/admin/nong-do";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            nongDoService.deleteNongDo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nồng độ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Nồng độ này đang được sử dụng.");
        }
        return "redirect:/admin/nong-do";
    }
}
