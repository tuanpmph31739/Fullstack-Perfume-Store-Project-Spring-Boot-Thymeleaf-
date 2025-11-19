package com.shop.fperfume.controller.admin;

import com.shop.fperfume.service.admin.ThongKeService;
import com.shop.fperfume.service.admin.ThongKeService.DoanhThuNgayDTO;
import com.shop.fperfume.service.admin.ThongKeService.TopSanPhamDTO;
import com.shop.fperfume.service.admin.ThongKeService.ThongKeTongQuanDTO;
import com.shop.fperfume.service.admin.ThongKeService.ThongKeTrangThaiDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping
    public String viewThongKe(
            Model model,
            // tab: tong | web | pos
            @RequestParam(name = "tab", required = false, defaultValue = "tong") String tab,

            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Mặc định: 7 ngày gần nhất
        if (to == null) {
            to = LocalDate.now();
        }
        if (from == null) {
            from = to.minusDays(6);
        }

        // ---------------------------
        // Chọn dữ liệu theo từng tab
        // ---------------------------
        ThongKeTongQuanDTO tongQuan;
        ThongKeTrangThaiDTO tkTrangThai;
        List<DoanhThuNgayDTO> doanhThuNgay;
        List<TopSanPhamDTO> topSanPham;

        switch (tab) {
            case "web" -> {
                // Kênh bán trên web
                tongQuan = thongKeService.thongKeTongQuanTheoKenh(from, to, "WEB");
                tkTrangThai = thongKeService.thongKeDonTheoTrangThaiTheoKenh(from, to, "WEB");
                doanhThuNgay = thongKeService.thongKeDoanhThuTheoNgayTheoKenh(from, to, "WEB");
                topSanPham = thongKeService.topSanPhamBanChayTheoKenh(from, to, 5, "WEB");
            }
            case "pos" -> {
                // Kênh bán tại quầy
                tongQuan = thongKeService.thongKeTongQuanTheoKenh(from, to, "TAI_QUAY");
                tkTrangThai = thongKeService.thongKeDonTheoTrangThaiTheoKenh(from, to, "TAI_QUAY");
                doanhThuNgay = thongKeService.thongKeDoanhThuTheoNgayTheoKenh(from, to, "TAI_QUAY");
                topSanPham = thongKeService.topSanPhamBanChayTheoKenh(from, to, 5, "TAI_QUAY");
            }
            default -> {
                // Tab "tổng" – tất cả kênh, chỉ đơn HOAN_THANH
                tongQuan = thongKeService.thongKeTongQuan(from, to);
                tkTrangThai = thongKeService.thongKeDonTheoTrangThai(from, to);
                doanhThuNgay = thongKeService.thongKeDoanhThuTheoNgay(from, to);
                topSanPham = thongKeService.topSanPhamBanChay(from, to, 5);
            }
        }

        // ----------------------------------
        // Chuẩn bị data cho Chart.js (theo ngày)
        // ----------------------------------
        List<String> labelsNgay = doanhThuNgay.stream()
                .map(dto -> dto.ngay.toString())
                .collect(Collectors.toList());

        List<java.math.BigDecimal> dataDoanhThu = doanhThuNgay.stream()
                .map(dto -> dto.doanhThu)
                .collect(Collectors.toList());

        // ----------------------------------
        // Chuẩn bị data cho chart Top Sản phẩm
        // ----------------------------------
        List<String> labelsTopSP = topSanPham.stream()
                .map(dto -> dto.tenSanPham)
                .collect(Collectors.toList());

        List<Long> dataTopSP = topSanPham.stream()
                .map(dto -> dto.tongSoLuong)
                .collect(Collectors.toList());

        // ----------------------------------
        // Đẩy lên model cho Thymeleaf
        // ----------------------------------
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);

        // tab hiện tại (để active nav-pills)
        model.addAttribute("tab", tab);

        // Tổng quan doanh thu + tổng đơn hoàn thành
        model.addAttribute("tongQuan", tongQuan);

        // Thống kê số đơn theo trạng thái (chờ xác nhận, đang chờ thanh toán, đang giao, đã huỷ, hoàn thành)
        model.addAttribute("tkTrangThai", tkTrangThai);

        // Chart doanh thu theo ngày
        model.addAttribute("labelsNgay", labelsNgay);
        model.addAttribute("dataDoanhThu", dataDoanhThu);

        // Top sản phẩm
        model.addAttribute("labelsTopSP", labelsTopSP);
        model.addAttribute("dataTopSP", dataTopSP);
        model.addAttribute("listTopSP", topSanPham);

        // Sidebar active
        model.addAttribute("currentPath", "/admin/thong-ke");

        return "admin/thong_ke/index";
    }
}
