package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import com.shop.fperfume.service.client.ThuongHieuClientService;
import jakarta.servlet.http.HttpServletRequest;  // <— thêm
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
public class SanPhamClientController {

    @Autowired
    private SanPhamClientService sanPhamClientService;

    @Autowired
    private ThuongHieuClientService thuongHieuClientService;

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Integer id, Model model) {
        SanPhamChiTietResponse spct = sanPhamClientService.getById(id);
        if (spct == null) return "redirect:/san-pham";

        var options = sanPhamClientService.getDungTichOptions(spct.getIdSanPham());
        Integer selectedDungTich = spct.getSoMl();

        List<SanPhamChiTietResponse> relatedProducts =
                sanPhamClientService.getRelatedProducts(
                        spct.getId(),
                        spct.getIdThuongHieu(),
                        spct.getIdNhomHuong(),
                        spct.getIdLoaiNuocHoa()
                );

        // Giới hạn tối đa 8 sản phẩm (cho chắc)
        if (relatedProducts.size() > 8) {
            relatedProducts = relatedProducts.subList(0, 8);
        }

        // Chia thành các nhóm, mỗi nhóm 4 sản phẩm
        List<List<SanPhamChiTietResponse>> relatedChunks = new ArrayList<>();
        int chunkSize = 4;
        for (int i = 0; i < relatedProducts.size(); i += chunkSize) {
            relatedChunks.add(
                    relatedProducts.subList(i, Math.min(i + chunkSize, relatedProducts.size()))
            );
        }

        model.addAttribute("sanPhamChiTiet", spct);
        model.addAttribute("options", options);
        model.addAttribute("selectedSpctId", spct.getId());
        model.addAttribute("selectedDungTich", selectedDungTich);
        model.addAttribute("idSanPham", spct.getIdSanPham());
        model.addAttribute("soLuongTon", spct.getSoLuongTon());
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("relatedChunks", relatedChunks);
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
                    "soLuongTon", ct.getSoLuongTon(),
                    "message", "OK"
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy biến thể theo dung tích"));
        }
    }

    @ModelAttribute("brands")
    public List<ThuongHieuResponse> loadBrands() {
        return thuongHieuClientService.getAllThuongHieu();
    }

    @GetMapping("/all")
    public String listAllProducts(
            Model model,
            HttpServletRequest request, // <— thêm
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize,
            @RequestParam(name = "brand", required = false) List<Integer> selectedBrands,
            @RequestParam(name = "gender", required = false) String selectedGender,  // nam|nu|unisex|null
            @RequestParam(name = "min", required = false) Integer minPrice,
            @RequestParam(name = "max", required = false) Integer maxPrice,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        // selfPath cho view
        model.addAttribute("selfPath", request.getRequestURI());

        int pageIndex = Math.max(pageNo - 1, 0);

        // Chuẩn hóa gender theo DB (Nam/Nữ/Unisex)
        String loaiDb = null;
        if (selectedGender != null && !selectedGender.isBlank()) {
            switch (selectedGender.toLowerCase()) {
                case "nam" -> loaiDb = "Nam";
                case "nu" -> loaiDb = "Nữ";
                case "unisex" -> loaiDb = "Unisex";
            }
        }

        if (minPrice != null && minPrice <= 0) minPrice = null;
        if (maxPrice != null && maxPrice <= 0) maxPrice = null;
        if (selectedBrands == null) selectedBrands = new ArrayList<>();

        var pageData = sanPhamClientService.filterProducts(
                selectedBrands, loaiDb, minPrice, maxPrice, sort, pageIndex, pageSize
        );

        model.addAttribute("sanPhams", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber() + 1);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", pageData.getTotalElements());

        if (pageData.getTotalPages() > 0) {
            List<Integer> pageNumbers = new ArrayList<>();
            for (int i = 1; i <= pageData.getTotalPages(); i++) pageNumbers.add(i);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Giữ trạng thái filter cho sidebar & toolbar
        model.addAttribute("selectedBrands", selectedBrands);
        model.addAttribute("selectedGender", selectedGender); // vẫn giữ 'nam/nu/unisex' cho radio
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "client/san_pham/product-list";
    }

    @GetMapping("/nuoc-hoa-nam")
    public String listNamProducts(
            Model model,
            HttpServletRequest request, // <— thêm
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize,
            @RequestParam(name = "brand", required = false) List<Integer> selectedBrands,
            @RequestParam(name = "min", required = false) Integer minPrice,
            @RequestParam(name = "max", required = false) Integer maxPrice,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return listByGender("Nam", "nam", "Nước hoa Nam",
                model, request, pageNo, pageSize, selectedBrands, minPrice, maxPrice, sort);
    }

    @GetMapping("/nuoc-hoa-nu")
    public String listNuProducts(
            Model model,
            HttpServletRequest request, // <— thêm
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize,
            @RequestParam(name = "brand", required = false) List<Integer> selectedBrands,
            @RequestParam(name = "min", required = false) Integer minPrice,
            @RequestParam(name = "max", required = false) Integer maxPrice,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return listByGender("Nữ", "nu", "Nước hoa Nữ",
                model, request, pageNo, pageSize, selectedBrands, minPrice, maxPrice, sort);
    }

    @GetMapping("/nuoc-hoa-unisex")
    public String listUnisexProducts(
            Model model,
            HttpServletRequest request, // <— thêm
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize,
            @RequestParam(name = "brand", required = false) List<Integer> selectedBrands,
            @RequestParam(name = "min", required = false) Integer minPrice,
            @RequestParam(name = "max", required = false) Integer maxPrice,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return listByGender("Unisex", "unisex", "Nước hoa Unisex",
                model, request, pageNo, pageSize, selectedBrands, minPrice, maxPrice, sort);
    }

    // ------------------ helper dùng chung ------------------
    private String listByGender(String loaiDb, String selectedGenderForUI, String title,
                                Model model,
                                HttpServletRequest request,     // <— thêm
                                Integer pageNo, Integer pageSize,
                                List<Integer> selectedBrands,
                                Integer minPrice, Integer maxPrice,
                                String sort) {

        // selfPath cho view gender
        model.addAttribute("selfPath", request.getRequestURI());

        int pageIndex = Math.max((pageNo == null ? 1 : pageNo) - 1, 0);
        if (minPrice != null && minPrice <= 0) minPrice = null;
        if (maxPrice != null && maxPrice <= 0) maxPrice = null;
        if (selectedBrands == null) selectedBrands = new ArrayList<>();

        var pageData = sanPhamClientService.filterProducts(
                selectedBrands, loaiDb, minPrice, maxPrice, sort, pageIndex, pageSize == null ? 15 : pageSize
        );

        model.addAttribute("sanPhams", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber() + 1);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("pageSize", pageData.getSize());
        model.addAttribute("totalElements", pageData.getTotalElements());

        if (pageData.getTotalPages() > 0) {
            List<Integer> pageNumbers = new ArrayList<>();
            for (int i = 1; i <= pageData.getTotalPages(); i++) pageNumbers.add(i);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Giữ trạng thái filter cho sidebar/toolbar
        model.addAttribute("selectedBrands", selectedBrands);
        model.addAttribute("selectedGender", selectedGenderForUI); // để radio trong sidebar tự check
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        // Tiêu đề trang
        model.addAttribute("title", title);

        // render view chung cho 3 trang theo giới
        return "client/san_pham/product-gender";
    }
}
