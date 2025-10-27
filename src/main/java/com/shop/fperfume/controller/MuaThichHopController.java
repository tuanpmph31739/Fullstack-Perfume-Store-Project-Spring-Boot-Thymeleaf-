package com.shop.fperfume.controller;

import com.shop.fperfume.model.request.DungTichRequest;
import com.shop.fperfume.model.request.MuaThichHopRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.MuaThichHopResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.service.MuaThichHopService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/mua-thich-hop")
public class MuaThichHopController {

    @Autowired
    private MuaThichHopService muaThichHopService;

    private final int PAGE_SIZE = 10;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<MuaThichHopResponse> page = muaThichHopService.paging(pageNo, PAGE_SIZE);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/mua-thich-hop");

        return "admin/mua_thich_hop/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("muaThichHopRequest", new MuaThichHopRequest());
        model.addAttribute("currentPath", "/admin/mua-thich-hop");
        return "admin/mua_thich_hop/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("muaThichHopRequest") MuaThichHopRequest request,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/mua-thich-hop");
            return "admin/mua_thich_hop/add";
        }

        try {
            muaThichHopService.addMuaThichHop(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mùa thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã Mùa")) {
                bindingResult.rejectValue("maMua", "error.maMua", e.getMessage());
            } else if (e.getMessage().contains("Tên Mùa")) {
                bindingResult.rejectValue("tenMua", "error.tenMua", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/mua-thich-hop");
            return "admin/mua_thich_hop/add";
        }

        return "redirect:/admin/mua-thich-hop";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MuaThichHopResponse responseDto = muaThichHopService.getMuaThichHopById(id);
            MuaThichHopRequest requestDto = MapperUtils.map(responseDto, MuaThichHopRequest.class);

            model.addAttribute("muaThichHopRequest", requestDto);
            model.addAttribute("currentPath", "/admin/mua-thich-hop");
            return "admin/mua_thich_hop/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mùa!");
            return "redirect:/admin/mua-thich-hop";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("muaThichHopRequest") MuaThichHopRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/admin/mua-thich-hop");
            return "admin/mua_thich_hop/edit";
        }

        try {
            muaThichHopService.updateMuaThichHop(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mùa thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Mã mùa")) {
                bindingResult.rejectValue("maMua", "error.maMua", e.getMessage());
            } else if (e.getMessage().contains("Tên mùa")) {
                bindingResult.rejectValue("tenMua", "error.tenMua", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            }
            model.addAttribute("currentPath", "/admin/mua-thich-hop");
            return "admin/mua_thich_hop/edit";
        }

        return "redirect:/admin/mua-thich-hop";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            muaThichHopService.deleteMuaThichHop(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mùa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: Mùa này đang được sử dụng.");
        }
        return "redirect:/admin/mua-thich-hop";
    }
}
