package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.SanPhamChiTietRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.service.admin.DungTichService;
import com.shop.fperfume.service.admin.NongDoService;
import com.shop.fperfume.service.admin.SanPhamChiTietService;
import com.shop.fperfume.service.admin.SanPhamService;
import com.shop.fperfume.util.MapperUtils; // <-- Đảm bảo bạn đã import MapperUtils
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

    private final int PAGE_SIZE = 10;

    // (Hàm index, viewAdd, add/save giữ nguyên như trước)
    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        if (pageNo < 1) pageNo = 1;

        PageableObject<SanPhamChiTietResponse> page = sanPhamChiTietService.paging(pageNo, PAGE_SIZE);
        model.addAttribute("page", page);
        model.addAttribute("pageMetaDataAvailable", page != null);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/san-pham-chi-tiet");

        return "admin/san_pham_chi_tiet/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("sanPhamChiTietRequest", new SanPhamChiTietRequest());
        loadDropdownData(model);
        return "admin/san_pham_chi_tiet/add";
    }

    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("sanPhamChiTietRequest") SanPhamChiTietRequest request,
                      BindingResult bindingResult, // Phải có BindingResult
                      RedirectAttributes redirectAttributes,
                      Model model) {

        // 1. Kiểm tra lỗi validation (ví dụ: để trống)
        if (bindingResult.hasErrors()) {
            loadDropdownData(model);
            return "admin/san_pham_chi_tiet/add"; // Trả về form
        }

        // 2. Bắt lỗi nghiệp vụ (ví dụ: trùng SKU)
        try {
            sanPhamChiTietService.addSanPhamChiTiet(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thành công!");

        } catch (RuntimeException e) {

            // === SỬA Ở ĐÂY ===
            // Gán lỗi Exception vào đúng trường "maSKU"
            bindingResult.rejectValue("maSKU", "error.maSKU", e.getMessage());
            // =================

            // Load lại dropdown và trả về form
            loadDropdownData(model);
            return "admin/san_pham_chi_tiet/add";
        }

        return "redirect:/admin/san-pham-chi-tiet";
    }


    /**
     * === SỬA LẠI HÀM NÀY ĐỂ DÙNG MAPPERUTILS ===
     */
    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy Response DTO
            SanPhamChiTietResponse responseDto = sanPhamChiTietService.getById(id);

            // 2. Dùng MapperUtils để map từ Response sang Request DTO
            SanPhamChiTietRequest requestDto = MapperUtils.map(responseDto, SanPhamChiTietRequest.class);

            // 3. Add Request DTO vào model
            model.addAttribute("sanPhamChiTietRequest", requestDto);

            // 4. Gửi tên ảnh cũ sang view để hiển thị
            model.addAttribute("hinhAnhHienTai", responseDto.getHinhAnh());

            loadDropdownData(model);
            return "admin/san_pham_chi_tiet/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm!");
            return "redirect:/admin/san-pham-chi-tiet";
        }
    }

    /**
     * (Hàm update giữ nguyên như trước, đã bao gồm Validation)
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id,
                         @Valid @ModelAttribute("sanPhamChiTietRequest") SanPhamChiTietRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            loadDropdownData(model);
            try {
                model.addAttribute("hinhAnhHienTai", sanPhamChiTietService.getById(id).getHinhAnh());
            } catch (Exception e) {}
            return "admin/san_pham_chi_tiet/edit";
        }

        // 2. Bắt lỗi nghiệp vụ (ví dụ: trùng SKU khi update)
        try {
            sanPhamChiTietService.updateSanPhamChiTiet(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi tiết sản phẩm thành công!");
        } catch (RuntimeException e) { // Bắt lỗi từ Service

            // Kiểm tra xem có phải lỗi trùng SKU không
            if (e.getMessage() != null && e.getMessage().contains("Mã SKU")) {
                // Gán lỗi này vào BindingResult, gắn nó vào trường "maSKU"
                bindingResult.rejectValue("maSKU", "error.maSKU", e.getMessage());
            } else {
                // Nếu là lỗi khác, hiển thị lỗi chung (cần div th:if="${errorMessage}" trong HTML)
                model.addAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            }

            // Load lại dropdown và trả về form edit
            loadDropdownData(model);
            // Gửi lại ảnh cũ khi có lỗi nghiệp vụ
            try {
                model.addAttribute("hinhAnhHienTai", sanPhamChiTietService.getById(id).getHinhAnh());
            } catch (Exception e2) { /* Bỏ qua */ }
            return "admin/san_pham_chi_tiet/edit";
        }

        return "redirect:/admin/san-pham-chi-tiet";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        model.addAttribute("sanPhamChiTietResponse", sanPhamChiTietService.getById(id));
        return "admin/san_pham_chi_tiet/view";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamChiTietService.deleteSanPhamChiTiet(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/san-pham-chi-tiet";
    }

    private void loadDropdownData(Model model) {
        model.addAttribute("listSanPham", sanPhamService.getAllSanPham());
        model.addAttribute("listDungTich", dungTichService.getDungTich());
        model.addAttribute("listNongDo", nongDoService.getAllNongDo());
    }

}