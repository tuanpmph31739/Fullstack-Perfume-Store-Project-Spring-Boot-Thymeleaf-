package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.repository.HoaDonChiTietRepository;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.service.common.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonPrintController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final QrCodeService qrCodeService;   // ✅ thêm service QR

    @GetMapping("/print/{id}")
    public String printInvoice(@PathVariable("id") Integer id,
                               @RequestParam(value = "autoPrint", required = false) Boolean autoPrint,
                               Model model) {

        HoaDon hoaDon = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepo.findByHoaDon_Id(id);

        model.addAttribute("donHang", hoaDon);
        model.addAttribute("chiTietList", chiTietList);

        boolean isTamTinh = !"HOAN_THANH".equalsIgnoreCase(hoaDon.getTrangThai());
        model.addAttribute("isTamTinh", isTamTinh);

        model.addAttribute("autoPrint", autoPrint != null && autoPrint);

        // ✅ Sinh QR theo số tiền + mã đơn
        String qrBase64 = qrCodeService.generatePaymentQrBase64(
                hoaDon.getMa(),
                hoaDon.getTongThanhToan()
        );
        model.addAttribute("qrCodeBase64", qrBase64);

        return "admin/hoa_don/print-invoice";
    }
}
