package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.ThuongHieuRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.admin.ThuongHieuService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

    private final int PAGE_SIZE = 12;

    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "keyword", required = false) String keyword) {

        if (pageNo == null || pageNo < 1) pageNo = 1;

        PageableObject<ThuongHieuResponse> page =
                thuongHieuService.paging(pageNo, PAGE_SIZE, keyword);

        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("keyword", keyword);
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
            return "redirect:/admin/thuong-hieu";

        } catch (DataIntegrityViolationException e) {
            // ✅ DB unique constraint (thường là trùng slug/ma/ten)
            bindingResult.rejectValue("tenThuongHieu", "error.tenThuongHieu",
                    "Tên thương hiệu sau khi chuẩn hoá (slug) bị trùng. Vui lòng đổi tên khác!");
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/add";

        } catch (RuntimeException e) {
            // ✅ lỗi business bạn throw trong service
            mapBusinessErrorToField(e, bindingResult, model);
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            ThuongHieuResponse responseDto = thuongHieuService.getThuongHieuById(id);
            ThuongHieuRequest requestDto = MapperUtils.map(responseDto, ThuongHieuRequest.class);

            model.addAttribute("thuongHieuRequest", requestDto);
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            model.addAttribute("hinhAnhHienTai", responseDto.getHinhAnh());

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
            try {
                model.addAttribute("hinhAnhHienTai", thuongHieuService.getThuongHieuById(id).getHinhAnh());
            } catch (Exception ignored) {}
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/edit";
        }

        try {
            thuongHieuService.updateThuongHieu(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thương hiệu thành công!");
            return "redirect:/admin/thuong-hieu";

        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("tenThuongHieu", "error.tenThuongHieu",
                    "Tên thương hiệu sau khi chuẩn hoá (slug) bị trùng. Vui lòng đổi tên khác!");
            try {
                model.addAttribute("hinhAnhHienTai", thuongHieuService.getThuongHieuById(id).getHinhAnh());
            } catch (Exception ignored) {}
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/edit";

        } catch (RuntimeException e) {
            mapBusinessErrorToField(e, bindingResult, model);
            try {
                model.addAttribute("hinhAnhHienTai", thuongHieuService.getThuongHieuById(id).getHinhAnh());
            } catch (Exception ignored) {}
            model.addAttribute("currentPath", "/admin/thuong-hieu");
            return "admin/thuong_hieu/edit";
        }
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

    private void mapBusinessErrorToField(RuntimeException e,
                                         BindingResult bindingResult,
                                         Model model) {
        String msg = (e.getMessage() == null) ? "Có lỗi xảy ra!" : e.getMessage();

        if (msg.contains("Mã thương hiệu")) {
            bindingResult.rejectValue("maThuongHieu", "error.maThuongHieu", msg);
        } else if (msg.contains("Tên thương hiệu") || msg.toLowerCase().contains("trùng")) {
            bindingResult.rejectValue("tenThuongHieu", "error.tenThuongHieu", msg);
        } else {
            model.addAttribute("errorMessage", "Lỗi: " + msg);
        }
    }
}
