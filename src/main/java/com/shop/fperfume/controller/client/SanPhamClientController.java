package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import com.shop.fperfume.service.client.ThuongHieuClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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

    // ✅ Load brands cho sidebar
    @ModelAttribute("brands")
    public List<ThuongHieuResponse> loadBrands() {
        return thuongHieuClientService.getAllThuongHieu();
    }

    // ✅ Chi tiết sản phẩm (client chỉ xem được nếu hienThi = true)
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Integer id, Model model) {

        SanPhamChiTietResponse spct;
        try {
            spct = sanPhamClientService.getById(id); // service của bạn nên dùng query visible (hienThi=true)
        } catch (Exception e) {
            return "redirect:/san-pham/all";
        }

        // ✅ Nếu sản phẩm bị ẩn khỏi client
        if (spct == null || Boolean.FALSE.equals(spct.getHienThi())) {
            return "redirect:/san-pham/all";
        }

        var options = sanPhamClientService.getDungTichOptions(spct.getIdSanPham());
        Integer selectedDungTich = spct.getSoMl();

        // ✅ related products (repo đã filter hienThi=true)
        List<SanPhamChiTietResponse> relatedProducts =
                sanPhamClientService.getRelatedProducts(
                        spct.getId(),
                        spct.getIdThuongHieu(),
                        spct.getIdNhomHuong(),
                        spct.getIdLoaiNuocHoa()
                );

        if (relatedProducts.size() > 8) {
            relatedProducts = relatedProducts.subList(0, 8);
        }

        List<List<SanPhamChiTietResponse>> relatedChunks = new ArrayList<>();
        int chunkSize = 4;
        for (int i = 0; i < relatedProducts.size(); i += chunkSize) {
            relatedChunks.add(
                    relatedProducts.subList(i, Math.min(i + chunkSize, relatedProducts.size()))
            );
        }

        // ✅ Flag cho UI: ngừng KD vẫn hiển thị nhưng disable mua
        boolean isNgungKinhDoanh = Boolean.FALSE.equals(spct.getTrangThai());
        boolean canBuy = Boolean.TRUE.equals(spct.getTrangThai()) && spct.getSoLuongTon() != null && spct.getSoLuongTon() > 0;

        model.addAttribute("sanPhamChiTiet", spct);
        model.addAttribute("options", options);
        model.addAttribute("selectedSpctId", spct.getId());
        model.addAttribute("selectedDungTich", selectedDungTich);
        model.addAttribute("idSanPham", spct.getIdSanPham());
        model.addAttribute("soLuongTon", spct.getSoLuongTon());

        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("relatedChunks", relatedChunks);

        // ✅ thêm 2 biến để thymeleaf dùng (disable button, show badge)
        model.addAttribute("isNgungKinhDoanh", isNgungKinhDoanh);
        model.addAttribute("canBuy", canBuy);

        return "client/san_pham/product-detail";
    }

    // ✅ API lấy giá theo dung tích (chỉ trả nếu biến thể hienThi = true)
    @GetMapping("/{idSanPham}/gia")
    @ResponseBody
    public ResponseEntity<?> getProductPrice(@PathVariable Integer idSanPham,
                                             @RequestParam Integer soMl) {

        Optional<SanPhamChiTiet> opt = sanPhamClientService.getBySanPhamAndSoMl(idSanPham, soMl);

        if (opt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Không tìm thấy biến thể theo dung tích"));
        }

        SanPhamChiTiet ct = opt.get();

        // ✅ Chặn nếu bị ẩn (phòng trường hợp service/repo chưa filter)
        if (ct.getHienThi() != null && !ct.getHienThi()) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Sản phẩm đang bị ẩn"));
        }

        String tenNongDo = (ct.getNongDo() != null) ? ct.getNongDo().getTenNongDo() : null;

        Map<String, Object> res = new HashMap<>();
        res.put("giaBan", ct.getGiaBan() != null ? ct.getGiaBan().toPlainString() : null);
        res.put("idSpct", ct.getId());
        res.put("soLuongTon", ct.getSoLuongTon());

        // ✅ quan trọng: trả trạng thái kinh doanh để frontend disable mua khi false
        res.put("trangThai", ct.getTrangThai());

        res.put("hinhAnh", ct.getHinhAnh());
        res.put("tenNongDo", tenNongDo);
        res.put("maSKU", ct.getMaSKU());
        res.put("message", "OK");

        return ResponseEntity.ok(res);
    }

    @GetMapping("/all")
    public String listAllProducts(
            Model model,
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize,
            @RequestParam(name = "brand", required = false) List<Integer> selectedBrands,
            @RequestParam(name = "gender", required = false) String selectedGender, // nam|nu|unisex|null
            @RequestParam(name = "min", required = false) Integer minPrice,
            @RequestParam(name = "max", required = false) Integer maxPrice,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        model.addAttribute("selfPath", request.getRequestURI());

        int pageIndex = Math.max(pageNo - 1, 0);

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

        long maxPriceBound = sanPhamClientService.getMaxPriceBound(loaiDb);
        model.addAttribute("maxPriceBound", maxPriceBound);

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

        model.addAttribute("selectedBrands", selectedBrands);
        model.addAttribute("selectedGender", selectedGender);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "client/san_pham/product-list";
    }

    @GetMapping("/nuoc-hoa-nam")
    public String listNamProducts(
            Model model,
            HttpServletRequest request,
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
            HttpServletRequest request,
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
            HttpServletRequest request,
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
                                HttpServletRequest request,
                                Integer pageNo, Integer pageSize,
                                List<Integer> selectedBrands,
                                Integer minPrice, Integer maxPrice,
                                String sort) {

        model.addAttribute("selfPath", request.getRequestURI());

        int pageIndex = Math.max((pageNo == null ? 1 : pageNo) - 1, 0);
        if (minPrice != null && minPrice <= 0) minPrice = null;
        if (maxPrice != null && maxPrice <= 0) maxPrice = null;
        if (selectedBrands == null) selectedBrands = new ArrayList<>();

        long maxPriceBound = sanPhamClientService.getMaxPriceBound(loaiDb);
        model.addAttribute("maxPriceBound", maxPriceBound);

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

        model.addAttribute("selectedBrands", selectedBrands);
        model.addAttribute("selectedGender", selectedGenderForUI);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        model.addAttribute("title", title);

        return "client/san_pham/product-gender";
    }
}
