package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.GiamGiaRequest;
import com.shop.fperfume.model.response.GiamGiaResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.service.admin.GiamGiaService;
import com.shop.fperfume.service.admin.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/giam-gia")
public class GiamGiaController {

    @Autowired
    private GiamGiaService giamGiaService;

    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    // ================================
    //  Danh sách giảm giá
    // ================================
    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "10") Integer size,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "loaiGiam", required = false) String loaiGiam,
                        @RequestParam(name = "trangThai", required = false) Boolean trangThai) {

        if (page < 1) page = 1;

        PageableObject<GiamGiaResponse> pageableObject =
                giamGiaService.paging(page, size, keyword, loaiGiam, trangThai);

        model.addAttribute("page", pageableObject);
        model.addAttribute("currentPath", "/admin/giam-gia");

        // Để giữ lại giá trị trên form lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiGiam", loaiGiam);
        model.addAttribute("trangThai", trangThai);

        return "admin/giam_gia/index";
    }


    // ================================
    //  Trang thêm mới
    // ================================
    @GetMapping("/add")
    public String addForm(Model model) {

        model.addAttribute("giamGiaRequest", new GiamGiaRequest());
        model.addAttribute("sanPhamChiTietList", sanPhamChiTietService.getAllSanPhamChiTiet());

        return "admin/giam_gia/add";
    }

    // ================================
    //  Xử lý thêm mới
    // ================================
    @PostMapping("/add")
    public String addGiamGia(@ModelAttribute GiamGiaRequest request,
                             RedirectAttributes redirectAttributes) {

        try {
            giamGiaService.addGiamGia(request);
            redirectAttributes.addFlashAttribute("success", "Thêm giảm giá thành công!");
            return "redirect:/admin/giam-gia";

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/giam-gia/add";
        }
    }

    // ================================
    //  Trang sửa giảm giá
    // ================================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        try {
            GiamGiaResponse response = giamGiaService.getGiamGiaById(id);

            // Convert response → request để bind form
            GiamGiaRequest request = new GiamGiaRequest();
            request.setId(response.getId());
            request.setMa(response.getMa());
            request.setTen(response.getTen());
            request.setMoTa(response.getMoTa());
            request.setLoaiGiam(response.getLoaiGiam());
            request.setGiaTri(response.getGiaTri());
            request.setDonHangToiThieu(response.getDonHangToiThieu());
            request.setGiamToiDa(response.getGiamToiDa());
            request.setNgayBatDau(response.getNgayBatDau());
            request.setNgayKetThuc(response.getNgayKetThuc());
            request.setTrangThai(response.getTrangThai());
            request.setPhamViApDung(response.getPhamViApDung());
            request.setIdSanPhamChiTiet(response.getIdSanPhamChiTiet());
            request.setSoLuong(response.getSoLuong());

            model.addAttribute("giamGiaRequest", request);
            model.addAttribute("sanPhamChiTietList", sanPhamChiTietService.getAllSanPhamChiTiet());

            return "admin/giam_gia/edit";

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/giam-gia";
        }
    }

    // ================================
    //  Xử lý cập nhật
    // ================================
    @PostMapping("/edit/{id}")
    public String updateGiamGia(@PathVariable Integer id,
                                @ModelAttribute GiamGiaRequest request,
                                RedirectAttributes redirectAttributes) {

        try {
            giamGiaService.updateGiamGia(id, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật giảm giá thành công!");
            return "redirect:/admin/giam-gia";

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/giam-gia/edit/" + id;
        }
    }

    // ================================
    //  Xóa
    // ================================
    @GetMapping("/delete/{id}")
    public String deleteGiamGia(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {

        try {
            giamGiaService.deleteGiamGia(id);
            redirectAttributes.addFlashAttribute("success", "Xóa giảm giá thành công!");

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/giam-gia";
    }
}
