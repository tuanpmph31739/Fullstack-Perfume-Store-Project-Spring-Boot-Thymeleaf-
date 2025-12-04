package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.repository.HoaDonChiTietRepository;
import com.shop.fperfume.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonPrintController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;

    @GetMapping("/print/{id}")
    public String printInvoice(@PathVariable("id") Integer id,
                               @RequestParam(value = "autoPrint", required = false) Boolean autoPrint,
                               Model model) {

        HoaDon hoaDon = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepo.findByHoaDon_Id(id);

        model.addAttribute("donHang", hoaDon);
        model.addAttribute("chiTietList", chiTietList);

        // Đơn CHƯA hoàn thành => Phiếu tạm tính, ngược lại => Hóa đơn
        boolean isTamTinh = !"HOAN_THANH".equalsIgnoreCase(hoaDon.getTrangThai());
        model.addAttribute("isTamTinh", isTamTinh);

        // Auto print sau khi load
        model.addAttribute("autoPrint", autoPrint != null && autoPrint);

        return "admin/hoa_don/print-invoice";
    }
}
