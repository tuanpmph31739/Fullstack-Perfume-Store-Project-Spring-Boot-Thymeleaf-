package com.shop.fperfume.controller.pos;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.service.pos.BanHangTaiQuayService;
import com.shop.fperfume.service.pos.HoaDonService;
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
@RequestMapping("/admin/ban-hang-tai-quay") // Đường dẫn cơ sở trong admin
@RequiredArgsConstructor
public class BanHangTaiQuayController {

    private final HoaDonService hoaDonService;
    private final BanHangTaiQuayService banHangService;

    /**
     * Hiển thị trang bán hàng chính.
     */
    @GetMapping
    public String viewBanHang(Model model,
                              @RequestParam(name = "idHD", required = false) Integer idHD,
                              @RequestParam(name = "keyword", required = false) String keyword) {

        // 1. Tải sản phẩm (đã gộp nhóm)
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
            hoaDonHienTai = hoaDonService.getById(idHD);
            if (hoaDonHienTai != null
                    && "DANG_CHO_THANH_TOAN".equals(hoaDonHienTai.getTrangThai())) {
                gioHang = banHangService.getChiTietCuaHoaDon(idHD);
            } else {
                idHD = null;
            }
        }
        model.addAttribute("idHD", idHD);
        model.addAttribute("hoaDon", hoaDonHienTai);
        model.addAttribute("gioHang", gioHang);
        List<GiamGia> listVoucherPhuHop = banHangService.findVoucherPhuHopChoHoaDon(hoaDonHienTai, gioHang);
        model.addAttribute("listVoucherPhuHop", listVoucherPhuHop);

        // 4. Xử lý tải khách hàng cho modal
        List<NguoiDung> customerResults;
        if (keyword != null && !keyword.isEmpty()) {
            customerResults = banHangService.searchKhachHang(keyword);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("customerSearchTriggered", true);
        } else {
            customerResults = banHangService.getAllKhachHang();
        }
        model.addAttribute("customerResults", customerResults);
        model.addAttribute("currentPath", "/admin/ban-hang-tai-quay");

        return "ban_hang/index";
    }
    private void keepCustomerFormToFlash(RedirectAttributes ra,
                                         String hoTen,
                                         String sdt,
                                         String email,
                                         String diaChi) {
        ra.addFlashAttribute("hoTenTmp", hoTen);
        ra.addFlashAttribute("sdtTmp", sdt);
        ra.addFlashAttribute("emailTmp", email);
        ra.addFlashAttribute("diaChiTmp", diaChi);
    }

    /**
     * Tạo một hóa đơn chờ mới.
     */
    @PostMapping("/tao-hoa-don")
    public String taoHoaDon() {
        HoaDon newHoaDon = hoaDonService.createNewHoaDon();
        return "redirect:/admin/ban-hang-tai-quay?idHD=" + newHoaDon.getId();
    }

    /**
     * Hủy (xóa) một hóa đơn chờ.
     */
    @PostMapping("/xoa-hoa-don/{id}")
    public String xoaHoaDon(@PathVariable("id") Integer idHD,
                            RedirectAttributes redirectAttributes) {
        try {
            hoaDonService.delete(idHD);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy hóa đơn.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ban-hang-tai-quay";
    }

    /**
     * Thêm sản phẩm vào hóa đơn (giỏ hàng).
     */
    @PostMapping("/add-san-pham")
    public String addSanPham(
            @RequestParam("idHD") Integer idHD,
            @RequestParam("idSanPhamChiTiet") Integer idSPCT,
            @RequestParam("soLuong") Integer soLuong,

            @RequestParam(name = "hoTen", required = false) String hoTen,
            @RequestParam(name = "sdt", required = false) String sdt,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "diaChi", required = false) String diaChi,

            RedirectAttributes redirectAttributes) {

        try {
            capNhatThongTinKhachIfNeeded(idHD, hoTen, sdt, email, diaChi);
            banHangService.addSanPhamVaoHoaDon(idHD, idSPCT, soLuong);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // GIỮ LẠI DỮ LIỆU FORM
        keepCustomerFormToFlash(redirectAttributes, hoTen, sdt, email, diaChi);

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
    }



    /**
     * Tăng số lượng của 1 sản phẩm trong giỏ hàng.
     */
    @PostMapping("/tang-so-luong")
    public String tangSoLuong(@RequestParam("idHDCT") Integer idHDCT,
                              @RequestParam("idHD") Integer idHD,

                              @RequestParam(name = "hoTen", required = false) String hoTen,
                              @RequestParam(name = "sdt", required = false) String sdt,
                              @RequestParam(name = "email", required = false) String email,
                              @RequestParam(name = "diaChi", required = false) String diaChi,

                              RedirectAttributes redirectAttributes) {
        try {
            capNhatThongTinKhachIfNeeded(idHD, hoTen, sdt, email, diaChi);
            banHangService.tangSoLuong(idHDCT);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        keepCustomerFormToFlash(redirectAttributes, hoTen, sdt, email, diaChi);

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
    }


    /**
     * Giảm số lượng của 1 sản phẩm trong giỏ hàng.
     */
    @PostMapping("/giam-so-luong")
    public String giamSoLuong(@RequestParam("idHDCT") Integer idHDCT,
                              @RequestParam("idHD") Integer idHD,

                              @RequestParam(name = "hoTen", required = false) String hoTen,
                              @RequestParam(name = "sdt", required = false) String sdt,
                              @RequestParam(name = "email", required = false) String email,
                              @RequestParam(name = "diaChi", required = false) String diaChi,

                              RedirectAttributes redirectAttributes) {
        try {
            capNhatThongTinKhachIfNeeded(idHD, hoTen, sdt, email, diaChi);
            banHangService.giamSoLuong(idHDCT);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        keepCustomerFormToFlash(redirectAttributes, hoTen, sdt, email, diaChi);

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
    }


    /**
     * Xóa hoàn toàn 1 sản phẩm khỏi giỏ hàng.
     */
    @PostMapping("/remove-san-pham")
    public String removeSanPham(@RequestParam("idHDCT") Integer idHDCT,
                                @RequestParam("idHD") Integer idHD,

                                @RequestParam(name = "hoTen", required = false) String hoTen,
                                @RequestParam(name = "sdt", required = false) String sdt,
                                @RequestParam(name = "email", required = false) String email,
                                @RequestParam(name = "diaChi", required = false) String diaChi,

                                RedirectAttributes redirectAttributes) {
        try {
            capNhatThongTinKhachIfNeeded(idHD, hoTen, sdt, email, diaChi);
            banHangService.xoaSanPhamVinhVien(idHDCT);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        keepCustomerFormToFlash(redirectAttributes, hoTen, sdt, email, diaChi);

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
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
        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * CẬP NHẬT THÔNG TIN KHÁCH HÀNG NHẬP TAY (TÊN, SĐT, EMAIL, ĐỊA CHỈ).
     * (Nếu bạn vẫn muốn có nút "Lưu thông tin khách" riêng).
     */
    @PostMapping("/cap-nhat-thong-tin-khach")
    public String capNhatThongTinKhach(@RequestParam("idHD") Integer idHD,
                                       @RequestParam(name = "hoTen", required = false) String hoTen,
                                       @RequestParam(name = "sdt", required = false) String sdt,
                                       @RequestParam(name = "email", required = false) String email,
                                       @RequestParam(name = "diaChi", required = false) String diaChi,
                                       RedirectAttributes redirectAttributes) {
        try {
            banHangService.capNhatThongTinKhach(idHD, hoTen, sdt, email, diaChi);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin khách hàng.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
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
            return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
        }

        try {
            banHangService.applyVoucher(idHD, maGiamGia);
            redirectAttributes.addFlashAttribute("successMessage", "Áp dụng mã thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
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

        return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
    }

    /**
     * Xử lý thanh toán, chốt đơn.
     * Nhận luôn thông tin khách hàng từ form bên trái.
     */
    @PostMapping("/thanh-toan")
    public String thanhToan(
            @RequestParam("idHD") Integer idHD,

            @RequestParam(name = "hoTen", required = false) String hoTen,
            @RequestParam(name = "sdt", required = false) String sdt,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "diaChi", required = false) String diaChi,

            @RequestParam(name = "phiShip", required = false) BigDecimal phiShip,
            @RequestParam(name = "soTienKhachDua", required = false) BigDecimal soTienKhachDua,

            RedirectAttributes redirectAttributes) {

        try {
            // 1. Cập nhật thông tin khách
            banHangService.capNhatThongTinKhach(idHD, hoTen, sdt, email, diaChi);

            // 2. Thanh toán hóa đơn tại quầy
            // ⚠️ Đảm bảo method này trả về HoaDon. Nếu hiện đang là void thì sửa lại cho nó return HoaDon.
            HoaDon hoaDon = banHangService.thanhToanHoaDonTaiQuay(
                    idHD,
                    hoTen,         // tên người nhận
                    sdt,           // sđt giao hàng
                    diaChi,        // địa chỉ giao hàng
                    phiShip,
                    soTienKhachDua
            );

            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công!");

            // ⭐ Đưa id hóa đơn vào flash attribute để trang POS xử lý mở tab in
            redirectAttributes.addFlashAttribute("printId", hoaDon.getId());

            // Vẫn quay về trang bán hàng như bình thường
            return "redirect:/admin/ban-hang-tai-quay";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/ban-hang-tai-quay?idHD=" + idHD;
        }
    }


    private void capNhatThongTinKhachIfNeeded(Integer idHD,
                                              String hoTen,
                                              String sdt,
                                              String email,
                                              String diaChi) {
        // Nếu người dùng gõ ít nhất Tên hoặc SĐT hoặc Email / Địa chỉ thì mới gọi service
        if ((hoTen != null && !hoTen.isBlank()) ||
                (sdt != null && !sdt.isBlank()) ||
                (email != null && !email.isBlank()) ||
                (diaChi != null && !diaChi.isBlank())) {
            banHangService.capNhatThongTinKhach(idHD, hoTen, sdt, email, diaChi);
        }
    }
}
