package com.shop.fperfume.controller.admin;

import com.shop.fperfume.service.admin.ThongKeService;
import com.shop.fperfume.service.admin.ThongKeService.DoanhThuNgayDTO;
import com.shop.fperfume.service.admin.ThongKeService.TopSanPhamDTO;
import com.shop.fperfume.service.admin.ThongKeService.ThongKeTongQuanDTO;
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

        // Tổng quan
        ThongKeTongQuanDTO tongQuan = thongKeService.thongKeTongQuan(from, to);

        // Doanh thu theo ngày
        List<DoanhThuNgayDTO> doanhThuNgay = thongKeService.thongKeDoanhThuTheoNgay(from, to);

        // Top sản phẩm
        List<TopSanPhamDTO> topSanPham = thongKeService.topSanPhamBanChay(from, to, 5);

        // Chuẩn bị data cho Chart.js
        List<String> labelsNgay = doanhThuNgay.stream()
                .map(dto -> dto.ngay.toString())
                .collect(Collectors.toList());

        List<java.math.BigDecimal> dataDoanhThu = doanhThuNgay.stream()
                .map(dto -> dto.doanhThu)
                .collect(Collectors.toList());

        List<String> labelsTopSP = topSanPham.stream()
                .map(dto -> dto.tenSanPham)
                .collect(Collectors.toList());

        List<Long> dataTopSP = topSanPham.stream()
                .map(dto -> dto.tongSoLuong)
                .collect(Collectors.toList());

        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);

        model.addAttribute("tongQuan", tongQuan);

        model.addAttribute("labelsNgay", labelsNgay);
        model.addAttribute("dataDoanhThu", dataDoanhThu);

        model.addAttribute("labelsTopSP", labelsTopSP);
        model.addAttribute("dataTopSP", dataTopSP);
        model.addAttribute("listTopSP", topSanPham);
        model.addAttribute("currentPath", "/admin/thong-ke");
        return "admin/thong_ke/index";
    }
}
