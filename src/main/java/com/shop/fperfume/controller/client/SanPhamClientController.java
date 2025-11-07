package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
public class SanPhamClientController {

    @Autowired
    private SanPhamClientService sanPhamClientService;

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Integer id, Model model) {
        SanPhamChiTietResponse spct = sanPhamClientService.getById(id);
        if (spct == null) return "redirect:/san-pham";

        var options = sanPhamClientService.getDungTichOptions(spct.getIdSanPham());

        // Sửa: Lấy dung tích của sản phẩm chi tiết hiện tại
        Integer selectedDungTich = spct.getSoMl();

        model.addAttribute("sanPhamChiTiet", spct);
        model.addAttribute("options", options);
        model.addAttribute("selectedSpctId", spct.getId());
        model.addAttribute("selectedDungTich", selectedDungTich);
        model.addAttribute("idSanPham", spct.getIdSanPham());
        model.addAttribute("soLuongTon", spct.getSoLuongTon()); // Dòng này của bạn đã đúng
        return "client/san_pham/product-detail";
    }


    @GetMapping("/{idSanPham}/gia")
    @ResponseBody
    public ResponseEntity<?> getProductPrice(@PathVariable Integer idSanPham, @RequestParam Integer soMl) {
        Optional<SanPhamChiTiet> sanPhamChiTiet = sanPhamClientService.getBySanPhamAndSoMl(idSanPham, soMl);
        if (sanPhamChiTiet.isPresent()) {
            SanPhamChiTiet ct = sanPhamChiTiet.get();

            // SỬA: Thêm "soLuongTon" vào response
            return ResponseEntity.ok(Map.of(
                    "giaBan", ct.getGiaBan().toPlainString(),
                    "idSpct", ct.getId().toString(),
                    "soLuongTon", ct.getSoLuongTon(), // <-- DÒNG MỚI
                    "message", "OK"
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy biến thể theo dung tích"));
        }
    }

    @GetMapping("/all")
    public String listAllProducts(
            Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize
    ) {
        int pageIndex = Math.max(pageNo - 1, 0);

        var pageData = sanPhamClientService.pageAll(pageIndex, pageSize);

        model.addAttribute("sanPhams", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber() + 1);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalItems", pageData.getTotalElements());

        if (pageData.getTotalPages() > 0) {
            List<Integer> pageNumbers = new java.util.ArrayList<>();
            for (int i = 1; i <= pageData.getTotalPages(); i++) pageNumbers.add(i);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "client/san_pham/product-list";
    }
}