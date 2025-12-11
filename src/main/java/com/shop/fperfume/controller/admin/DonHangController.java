package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.model.response.DonHangResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.HoaDonChiTietRepository;
import com.shop.fperfume.service.admin.DonHangService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/don-hang")
public class DonHangController {

    @Autowired
    private DonHangService donHangService;
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    private final int PAGE_SIZE = 12;

    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "kenhBan", required = false) String kenhBan,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "trangThai", required = false) String trangThai,
                        @RequestParam(name = "sortNgayTao", required = false, defaultValue = "DESC") String sortNgayTao) {

        if (kenhBan == null || kenhBan.isBlank()) {
            kenhBan = "WEB";
        }

        PageableObject<DonHangResponse> page =
                donHangService.pagingDonHang(pageNo, PAGE_SIZE, kenhBan, keyword, trangThai, sortNgayTao);

        model.addAttribute("page", page);
        model.addAttribute("page.metaDataAvailable", true);
        model.addAttribute("page.size", PAGE_SIZE);

        model.addAttribute("kenhBan", kenhBan);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sortNgayTao", sortNgayTao);

        model.addAttribute("currentPath", "/admin/don-hang");
        return "admin/don_hang/index";
    }

    @GetMapping("/view/{id}")
    public String viewDonHang(@PathVariable("id") Integer id,
                              Model model,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        try {
            DonHangResponse donHang = donHangService.getById(id);
            model.addAttribute("donHang", donHang);

            List<HoaDonChiTiet> chiTietList =
                    hoaDonChiTietRepository.findByHoaDon_Id_WithSanPham(id);
            model.addAttribute("chiTietList", chiTietList);

            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", referer != null ? referer : "/admin/don-hang");

            model.addAttribute("currentPath", "/admin/don-hang");
            return "admin/don_hang/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/admin/don-hang";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id,
                               Model model,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            // Thông tin đơn hàng (DTO)
            DonHangResponse donHang = donHangService.getById(id);
            model.addAttribute("donHang", donHang);

            // ✅ danh sách sản phẩm trong đơn
            List<HoaDonChiTiet> chiTietList =
                    hoaDonChiTietRepository.findByHoaDon_Id_WithSanPham(id);
            model.addAttribute("chiTietList", chiTietList);

            // ✅ danh sách trạng thái được phép chuyển tới
            var allowedTrangThais = donHangService.getAllowedNextTrangThais(donHang.getTrangThai());
            model.addAttribute("allowedTrangThais", allowedTrangThais);

            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", referer != null ? referer : "/admin/don-hang");
            model.addAttribute("currentPath", "/admin/don-hang");
            return "admin/don_hang/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/admin/don-hang";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateDonHang(@PathVariable("id") Integer id,
                                Model model,
                                @RequestParam("tenNguoiNhan") String tenNguoiNhan,
                                @RequestParam("sdt") String sdt,
                                @RequestParam("diaChi") String diaChi,
                                @RequestParam("trangThai") String trangThai,
                                RedirectAttributes redirectAttributes) {
        try {
            donHangService.updateDonHang(id, tenNguoiNhan, sdt, diaChi, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/don-hang";
    }

}
