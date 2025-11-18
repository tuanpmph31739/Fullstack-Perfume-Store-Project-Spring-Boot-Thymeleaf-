package com.shop.fperfume.controller.BanHang;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.service.banHang.BanHangTaiQuayService;
import com.shop.fperfume.service.banHang.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các nghiệp vụ Bán hàng tại quầy (POS)
 * Dùng cho mô hình Spring MVC + Thymeleaf.
 */
@Controller
@RequestMapping("/ban-hang-tai-quay") // Đường dẫn cơ sở
@RequiredArgsConstructor
public class BanHangTaiQuayController {

    private final HoaDonService hoaDonService;
    private final BanHangTaiQuayService banHangService;

    /**
     * Hiển thị trang bán hàng chính.
     * Tải danh sách hóa đơn chờ, sản phẩm, và chi tiết hóa đơn đang chọn.
     */
    @GetMapping
    public String viewBanHang(Model model,
                              @RequestParam(name = "idHD", required = false) Integer idHD,
                              @RequestParam(name = "keyword", required = false) String keyword) {

        // 1. Tải Sản phẩm (Đã gộp nhóm)
        Map<SanPham, List<SanPhamChiTiet>> groupedProducts = banHangService.getGroupedSanPham();
        model.addAttribute("groupedProducts", groupedProducts);

        // 2. Tải danh sách hóa đơn chờ
        List<HoaDon> listHoaDon = hoaDonService.getHoaDonChoTaiQuay();
        model.addAttribute("listHoaDon", listHoaDon);

        // 3. Xử lý hóa đơn đang được chọn
        HoaDon hoaDonHienTai = null;
        List<HoaDonChiTiet> gioHang = null;
        if (idHD == null && !listHoaDon.isEmpty()) {
            idHD = listHoaDon.get(0).getId();
        }
        if (idHD != null) {
            hoaDonHienTai = hoaDonService.getById(idHD); // (Hàm này đã fix Lazy KhachHang)
            if (hoaDonHienTai != null && hoaDonHienTai.getTrangThai().equals("0")) {
                gioHang = banHangService.getChiTietCuaHoaDon(idHD); // (Hàm này đã fix Lazy SanPham)
            } else {
                idHD = null;
            }
        }
        model.addAttribute("idHD", idHD);
        model.addAttribute("hoaDon", hoaDonHienTai);
        model.addAttribute("gioHang", gioHang);

        // 4. Xử lý tải khách hàng
        List<NguoiDung> customerResults;
        if (keyword != null && !keyword.isEmpty()) {
            customerResults = banHangService.searchKhachHang(keyword);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("customerSearchTriggered", true);
        } else {
            customerResults = banHangService.getAllKhachHang(); // Lấy tất cả khách
        }
        model.addAttribute("customerResults", customerResults);

        return "banhang/index";
    }


    /**
     * Tạo một hóa đơn chờ mới.
     */
    @PostMapping("/tao-hoa-don")
    public String taoHoaDon() {
        HoaDon newHoaDon = hoaDonService.createNewHoaDon();
        return "redirect:/ban-hang-tai-quay?idHD=" + newHoaDon.getId();
    }

    /**
     * Hủy (xóa) một hóa đơn chờ.
     */
    @PostMapping("/xoa-hoa-don/{id}")
    public String xoaHoaDon(@PathVariable("id") Integer idHD) {
        hoaDonService.delete(idHD);
        return "redirect:/ban-hang-tai-quay";
    }

    /**
     * Thêm sản phẩm vào hóa đơn (giỏ hàng).
     */
    @PostMapping("/add-san-pham")
    public String addSanPham(
            @RequestParam("idHD") Integer idHD,
            @RequestParam("idSanPhamChiTiet") Integer idSPCT,
            @RequestParam("soLuong") Integer soLuong,
            RedirectAttributes redirectAttributes) {

        try {
            banHangService.addSanPhamVaoHoaDon(idHD, idSPCT, soLuong);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Tăng số lượng của 1 sản phẩm trong giỏ hàng.
     */
    @PostMapping("/tang-so-luong")
    public String tangSoLuong(@RequestParam("idHDCT") Integer idHDCT,
                              @RequestParam("idHD") Integer idHD,
                              RedirectAttributes redirectAttributes) {
        try {
            banHangService.tangSoLuong(idHDCT);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Giảm số lượng của 1 sản phẩm trong giỏ hàng.
     */
    @PostMapping("/giam-so-luong")
    public String giamSoLuong(@RequestParam("idHDCT") Integer idHDCT,
                              @RequestParam("idHD") Integer idHD,
                              RedirectAttributes redirectAttributes) {
        try {
            banHangService.giamSoLuong(idHDCT);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Xóa hoàn toàn 1 sản phẩm khỏi giỏ hàng.
     */
    @PostMapping("/remove-san-pham")
    public String removeSanPham(@RequestParam("idHDCT") Integer idHDCT,
                                @RequestParam("idHD") Integer idHD,
                                RedirectAttributes redirectAttributes) {
        try {
            banHangService.xoaSanPhamVinhVien(idHDCT);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Gán khách hàng có sẵn vào hóa đơn.
     */
    @PostMapping("/gan-khach-hang")
    public String ganKhachHang(@RequestParam("idHD") Integer idHD,
                               @RequestParam("idKH") Integer idKH,
                               RedirectAttributes redirectAttributes) {
        try {
            banHangService.ganKhachHang(idHD, idKH);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gán khách hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Áp dụng voucher.
     */
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam("idHD") Integer idHD,
                               @RequestParam("maGiamGia") String maGiamGia,
                               RedirectAttributes redirectAttributes) {

        if (maGiamGia == null || maGiamGia.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mã giảm giá");
            return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
        }

        try {
            banHangService.applyVoucher(idHD, maGiamGia);
            redirectAttributes.addFlashAttribute("successMessage", "Áp dụng mã thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Gỡ bỏ voucher.
     */
    @PostMapping("/remove-voucher")
    public String removeVoucher(@RequestParam("idHD") Integer idHD,
                                RedirectAttributes redirectAttributes) {
        try {
            banHangService.removeVoucher(idHD);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ mã giảm giá");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Xử lý thanh toán, chốt đơn.
     */
    @PostMapping("/thanh-toan")
    public String thanhToan(
            @RequestParam("idHD") Integer idHD,
            // Thêm các trường Giao Hàng (không bắt buộc)
            @RequestParam(name = "tenNguoiNhan", required = false) String tenNguoiNhan,
            @RequestParam(name = "sdtGiaoHang", required = false) String sdtGiaoHang,
            @RequestParam(name = "tinhThanhPho", required = false) String tinhThanhPho,
            @RequestParam(name = "quanHuyen", required = false) String quanHuyen,
            @RequestParam(name = "phuongXa", required = false) String phuongXa,
            @RequestParam(name = "diaChiChiTiet", required = false) String diaChiChiTiet,
            @RequestParam(name = "phiShip", required = false) BigDecimal phiShip,
            RedirectAttributes redirectAttributes) {

        try {
            // Ghép địa chỉ lại (nếu có)
            String diaChiGiaoHang = null;
            if (diaChiChiTiet != null && !diaChiChiTiet.isEmpty()) {
                diaChiGiaoHang = String.join(", ",
                        diaChiChiTiet, phuongXa, quanHuyen, tinhThanhPho);
            }

            // Gọi service với thông tin đầy đủ
            banHangService.thanhToanHoaDonTaiQuay(
                    idHD, tenNguoiNhan, sdtGiaoHang, diaChiGiaoHang, phiShip
            );

            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công!");
            return "redirect:/ban-hang-tai-quay";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/ban-hang-tai-quay?idHD=" + idHD;
        }
    }
}