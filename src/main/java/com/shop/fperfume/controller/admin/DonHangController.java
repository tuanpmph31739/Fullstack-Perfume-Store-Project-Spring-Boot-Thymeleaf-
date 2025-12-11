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
            // Lấy thông tin đơn hàng (dto)
            DonHangResponse donHang = donHangService.getById(id);
            model.addAttribute("donHang", donHang);

            // Lấy danh sách chi tiết đơn hàng (sản phẩm, số lượng, đơn giá...)
            List<HoaDonChiTiet> chiTietList =
                    hoaDonChiTietRepository.findByHoaDon_Id_WithSanPham(id);
            model.addAttribute("chiTietList", chiTietList);

            // URL quay lại trang trước
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
            // Lấy thông tin đơn hàng (dto hoặc entity)
            DonHangResponse donHang = donHangService.getById(id);  // Đây là dto chứa thông tin đơn hàng
            model.addAttribute("donHang", donHang);  // Đảm bảo add vào model

            // Để bấm "Quay lại" vẫn về đúng trang trước
            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", referer != null ? referer : "/admin/don-hang");
            model.addAttribute("currentPath", "/admin/don-hang");
            return "admin/don_hang/edit";  // Trỏ tới file edit.html
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
