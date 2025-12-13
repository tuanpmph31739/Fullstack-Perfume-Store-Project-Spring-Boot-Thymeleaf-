package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.model.response.DonHangResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.HoaDonChiTietRepository;
import com.shop.fperfume.service.admin.DonHangService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonAdminController {

    private final DonHangService donHangService;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    private static final int PAGE_SIZE = 12;

    /**
     * Trang danh sách HÓA ĐƠN
     * – Dùng chung dữ liệu HoaDon, nhưng UI chi tiết hơn về tiền / thanh toán
     */
    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "kenhBan", required = false) String kenhBan,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "trangThai", required = false) String trangThai,
                        @RequestParam(name = "sortNgayTao", required = false, defaultValue = "DATE_DESC") String sortNgayTao,
                        @RequestParam(name = "idThanhToan", required = false) Integer idThanhToan) {

        // Normalize kenhBan
        if (kenhBan != null) {
            kenhBan = kenhBan.trim();
            if (kenhBan.isEmpty()
                    || "null".equalsIgnoreCase(kenhBan)
                    || "ALL".equalsIgnoreCase(kenhBan)) {
                kenhBan = null;
            }
        }

        PageableObject<DonHangResponse> page =
                donHangService.pagingHoaDon(pageNo, PAGE_SIZE,
                        kenhBan, keyword, trangThai, sortNgayTao, idThanhToan);

        model.addAttribute("page", page);
        model.addAttribute("page.metaDataAvailable", true);
        model.addAttribute("page.size", PAGE_SIZE);

        model.addAttribute("kenhBan", kenhBan);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sortNgayTao", sortNgayTao);
        model.addAttribute("idThanhToan", idThanhToan); //  để Thymeleaf select đúng option

        model.addAttribute("currentPath", "/admin/hoa-don");
        return "admin/hoa_don/index";
    }



    /**
     * Xem chi tiết HÓA ĐƠN
     *  – Có thể tái sử dụng layout giống view Đơn hàng,
     *    hoặc bạn tạo riêng file admin/hoa_don/view.html
     */
    @GetMapping("/view/{id}")
    public String viewHoaDon(@PathVariable("id") Integer id,
                             Model model,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            DonHangResponse hoaDon = donHangService.getById(id);
            model.addAttribute("hoaDon", hoaDon);

            // List chi tiết hóa đơn
            List<HoaDonChiTiet> chiTietList =
                    hoaDonChiTietRepository.findByHoaDon_Id_WithSanPham(id);
            model.addAttribute("chiTietList", chiTietList);

            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", referer != null ? referer : "/admin/hoa-don");

            model.addAttribute("currentPath", "/admin/hoa-don");
            return "admin/hoa_don/view"; // lát nữa bạn có thể clone từ admin/don_hang/view
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn!");
            return "redirect:/admin/hoa-don";
        }
    }
}
