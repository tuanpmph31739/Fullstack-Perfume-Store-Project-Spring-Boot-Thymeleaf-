package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

        // Lấy tất cả dung tích của sản phẩm gốc
        var options = sanPhamClientService.getDungTichOptions(spct.getIdSanPham());

        Integer selectedDungTich = options.isEmpty() ? null : options.get(0).getSoMl();

        model.addAttribute("sanPhamChiTiet", spct);      // biến thể hiện tại
        model.addAttribute("options", options);          // các lựa chọn dung tích
        model.addAttribute("selectedSpctId", spct.getId());
        model.addAttribute("selectedDungTich", selectedDungTich);
        model.addAttribute("idSanPham", spct.getIdSanPham());
        return "client/san_pham/product-detail";
    }


    @GetMapping("/{idSanPham}/gia")
    @ResponseBody
    public ResponseEntity<?> getProductPrice(@PathVariable Integer idSanPham, @RequestParam Integer soMl) {
        Optional<SanPhamChiTiet> sanPhamChiTiet = sanPhamClientService.getBySanPhamAndSoMl(idSanPham, soMl);
        if (sanPhamChiTiet.isPresent()) {
            SanPhamChiTiet ct = sanPhamChiTiet.get();
            return ResponseEntity.ok(Map.of(
                    "giaBan", ct.getGiaBan().toPlainString(),
                    "idSpct", ct.getId().toString(),
                    "message", "OK"
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy biến thể theo dung tích"));
        }
    }

}




