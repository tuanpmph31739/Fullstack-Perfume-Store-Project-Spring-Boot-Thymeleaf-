package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.SanPhamChiTietRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.service.admin.DungTichService;
import com.shop.fperfume.service.admin.NongDoService;
import com.shop.fperfume.service.admin.SanPhamChiTietService;
import com.shop.fperfume.service.admin.SanPhamService;
import com.shop.fperfume.util.MapperUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham-chi-tiet")
public class SanPhamChiTietController {

    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;
    @Autowired
    private SanPhamService sanPhamService;
    @Autowired
    private DungTichService dungTichService;
    @Autowired
    private NongDoService nongDoService;

    private final int PAGE_SIZE = 9;

    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "1") Integer pageNo,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "dungTichId", required = false) Integer dungTichId,
                        @RequestParam(value = "nongDoId", required = false) Integer nongDoId,
                        @RequestParam(value = "trangThai", required = false) String trangThai, // <--- đổi kiểu
                        @RequestParam(value = "sort", required = false) String sort) {

        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }

        PageableObject<SanPhamChiTietResponse> pageData =
                sanPhamChiTietService.getPage(
                        pageNo,
                        PAGE_SIZE,
                        keyword,
                        dungTichId,
                        nongDoId,
                        trangThai,   // <--- truyền String xuống service
                        sort
                );

        model.addAttribute("page", pageData);
        model.addAttribute("page.metaDataAvailable", true);
        model.addAttribute("page.size", PAGE_SIZE);

        // Giữ lại filter
        model.addAttribute("keyword", keyword);
        model.addAttribute("dungTichId", dungTichId);
        model.addAttribute("nongDoId", nongDoId);
        model.addAttribute("trangThai", trangThai);  // giờ là String
        model.addAttribute("sort", sort);

        loadDropdownData(model);
        model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");

        return "admin/san_pham_chi_tiet/index";
    }


    @GetMapping("/add")
    public String viewAdd(Model model,
                          HttpServletRequest request) {
        model.addAttribute("sanPhamChiTietRequest", new SanPhamChiTietRequest());
        loadDropdownData(model);

        String referer = request.getHeader("Referer");
        model.addAttribute("backUrl", referer);
        model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");
        return "admin/san_pham_chi_tiet/add";
    }

    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("sanPhamChiTietRequest") SanPhamChiTietRequest request,
                      BindingResult bindingResult,
                      @RequestParam(value = "backUrl", required = false) String backUrl,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            loadDropdownData(model);
            return "admin/san_pham_chi_tiet/add";
        }

        try {
            sanPhamChiTietService.addSanPhamChiTiet(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thành công!");
        } catch (RuntimeException e) {
            bindingResult.rejectValue("maSKU", "error.maSKU", e.getMessage());
            loadDropdownData(model);
            model.addAttribute("backUrl", backUrl);
            model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");
            return "admin/san_pham_chi_tiet/add";
        }
        if (backUrl != null && !backUrl.isBlank()) {
            return "redirect:" + backUrl;
        }

        return "redirect:/admin/san-pham-chi-tiet";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Integer id,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {
        try {
            SanPhamChiTietResponse responseDto = sanPhamChiTietService.getById(id);

            SanPhamChiTietRequest requestDto = MapperUtils.map(responseDto, SanPhamChiTietRequest.class);

            model.addAttribute("sanPhamChiTietRequest", requestDto);
            model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");
            model.addAttribute("hinhAnhHienTai", responseDto.getHinhAnh());

            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", referer);

            loadDropdownData(model);
            return "admin/san_pham_chi_tiet/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm!");
            return "redirect:/admin/san-pham-chi-tiet";
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id,
                         @Valid @ModelAttribute("sanPhamChiTietRequest") SanPhamChiTietRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "backUrl", required = false) String backUrl,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            loadDropdownData(model);
            try {
                model.addAttribute("hinhAnhHienTai", sanPhamChiTietService.getById(id).getHinhAnh());
            } catch (Exception ignored) {}
            model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");
            model.addAttribute("backUrl", backUrl);
            return "admin/san_pham_chi_tiet/edit";
        }

        try {
            sanPhamChiTietService.updateSanPhamChiTiet(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi tiết sản phẩm thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Mã SKU")) {
                bindingResult.rejectValue("maSKU", "error.maSKU", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            }

            loadDropdownData(model);
            try {
                model.addAttribute("hinhAnhHienTai", sanPhamChiTietService.getById(id).getHinhAnh());
            } catch (Exception ignored) {}
            model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");
            model.addAttribute("backUrl", backUrl);
            return "admin/san_pham_chi_tiet/edit";
        }

        if (backUrl != null && !backUrl.isBlank()) {
            return "redirect:" + backUrl;
        }
        return "redirect:/admin/san-pham-chi-tiet";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id,
                       Model model,
                       HttpServletRequest request) {
        model.addAttribute("sanPhamChiTietResponse", sanPhamChiTietService.getById(id));
        model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");

        String referer = request.getHeader("Referer");
        model.addAttribute("backUrl", referer);

        return "admin/san_pham_chi_tiet/view";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {

        try {
            sanPhamChiTietService.deleteSanPhamChiTiet(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/san-pham-chi-tiet");
    }

    private void loadDropdownData(Model model) {
        model.addAttribute("listSanPham", sanPhamService.getAllSanPham());
        model.addAttribute("listDungTich", dungTichService.getDungTich());
        model.addAttribute("listNongDo", nongDoService.getAllNongDo());
    }

}
