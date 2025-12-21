package com.shop.fperfume.controller.client;

import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import com.shop.fperfume.service.client.ThuongHieuClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ThuongHieuClientController {

    @Autowired
    private ThuongHieuClientService thuongHieuClientService;

    @Autowired
    private SanPhamClientService sanPhamClientService;

    // ✅ Sidebar cần list brand để tick checkbox
    @ModelAttribute("brands")
    public List<ThuongHieuResponse> loadBrands() {
        return thuongHieuClientService.getAllThuongHieu();
    }

    @GetMapping("/thuong-hieu/all")
    public String viewAllBrands(Model model) {

        // 1) Lấy toàn bộ thương hiệu (đã sort A–Z từ service)
        List<ThuongHieuResponse> allBrands = thuongHieuClientService.getAllThuongHieu();

        // 2) Group theo chữ cái đầu: KEY LÀ STRING "A", "B", "C", ...
        Map<String, List<ThuongHieuResponse>> brandsByLetter = allBrands.stream()
                .filter(th -> th.getTenThuongHieu() != null && !th.getTenThuongHieu().isBlank())
                .collect(Collectors.groupingBy(
                        th -> th.getTenThuongHieu()
                                .trim()
                                .substring(0, 1)
                                .toUpperCase(Locale.ROOT),
                        TreeMap::new,
                        Collectors.toList()
                ));

        // 3) Đưa ra view
        model.addAttribute("brands", allBrands);                 // dùng cho khối "All"
        model.addAttribute("brandsByLetter", brandsByLetter);    // dùng cho từng chữ cái

        return "client/thuong_hieu/thuong-hieu-tat-ca";
    }

    @GetMapping("/thuong-hieu/{slug}")
    public String listProductsByBrand(
            @PathVariable String slug,
            Model model,
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "size", defaultValue = "15") Integer pageSize,
            @RequestParam(name = "brand", required = false) List<Integer> selectedBrands,
            @RequestParam(name = "gender", required = false) String selectedGender,  // nam|nu|unisex|null
            @RequestParam(name = "min", required = false) Integer minPrice,
            @RequestParam(name = "max", required = false) Integer maxPrice,
            @RequestParam(name = "sort", required = false) String sort
    ) {

        // 1) Lấy brand theo slug (getBySlug đang orElseThrow)
        ThuongHieuResponse brand;
        try {
            brand = thuongHieuClientService.getBySlug(slug);
        } catch (RuntimeException e) {
            return "redirect:/thuong-hieu/all";
        }

        // selfPath cho view (dùng trong form sort & pagination)
        model.addAttribute("selfPath", request.getRequestURI());

        // 2) Chuẩn hóa phân trang
        int pageIndex = Math.max((pageNo == null ? 1 : pageNo) - 1, 0);

        // 3) Chuẩn hóa gender (UI: nam/nu/unisex -> DB: Nam/Nữ/Unisex)
        String loaiDb = null;
        if (selectedGender != null && !selectedGender.isBlank()) {
            switch (selectedGender.toLowerCase()) {
                case "nam" -> loaiDb = "Nam";
                case "nu" -> loaiDb = "Nữ";
                case "unisex" -> loaiDb = "Unisex";
            }
        }

        // 4) Chuẩn hóa giá
        if (minPrice != null && minPrice <= 0) minPrice = null;
        if (maxPrice != null && maxPrice <= 0) maxPrice = null;

        // ✅ Max slider theo đúng thương hiệu đang xem (và theo giới tính nếu có)
        long maxPriceBound = sanPhamClientService.getMaxPriceBoundByBrandSlug(slug, loaiDb);
        model.addAttribute("maxPriceBound", maxPriceBound);

        // 5) Xử lý brand filter:
        //    - Nếu người dùng chưa chọn brand trong sidebar => mặc định brand hiện tại
        if (selectedBrands == null || selectedBrands.isEmpty()) {
            selectedBrands = new ArrayList<>();
            selectedBrands.add(brand.getId().intValue());
        }

        // 6) Gọi service filterProducts (giống listAllProducts)
        var pageData = sanPhamClientService.filterProducts(
                selectedBrands,      // luôn chứa ít nhất brand hiện tại
                loaiDb,
                minPrice,
                maxPrice,
                sort,
                pageIndex,
                pageSize == null ? 15 : pageSize
        );

        // 7) Đổ dữ liệu cho view
        List<SanPhamChiTietResponse> sanPhams = pageData.getContent();

        model.addAttribute("brand", brand);
        model.addAttribute("sanPhams", sanPhams);
        model.addAttribute("currentPage", pageData.getNumber() + 1);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", pageData.getTotalElements());

        // pageNumbers (1..N)
        if (pageData.getTotalPages() > 0) {
            List<Integer> pageNumbers = IntStream
                    .rangeClosed(1, pageData.getTotalPages())
                    .boxed()
                    .toList();
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Giữ trạng thái filter cho sidebar & toolbar
        model.addAttribute("selectedBrands", selectedBrands);
        model.addAttribute("selectedGender", selectedGender); // nam/nu/unisex để radio tự check
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        // View giao diện brand-detail dùng sidebar & grid giống product-list
        return "client/thuong_hieu/thuong-hieu-chi-tiet";
    }
}
