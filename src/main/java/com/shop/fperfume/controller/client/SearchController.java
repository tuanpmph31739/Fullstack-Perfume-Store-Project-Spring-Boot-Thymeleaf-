package com.shop.fperfume.controller.client;

import com.shop.fperfume.dto.SearchSuggestionDTO;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SearchController {

    private final SanPhamClientService sanPhamClientService;

    public SearchController(SanPhamClientService sanPhamClientService) {
        this.sanPhamClientService = sanPhamClientService;
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 Model model) {

        if (keyword == null || keyword.trim().isEmpty()) {
            // Không có keyword thì cho về trang sản phẩm tất cả hoặc trang chủ
            return "redirect:/san-pham/all";
        }

        int pageIndex = Math.max(page - 1, 0);
        int pageSize = 12; // 12 sp / trang, tùy bạn

        Page<SanPhamChiTietResponse> resultPage =
                sanPhamClientService.searchProducts(keyword, pageIndex, pageSize);

        model.addAttribute("keyword", keyword);
        model.addAttribute("pageData", resultPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());

        return "client/search/results";
    }

    @GetMapping("/api/search-suggest")
    @ResponseBody
    public List<SearchSuggestionDTO> searchSuggest(@RequestParam("keyword") String keyword) {
        List<SanPhamChiTietResponse> list = sanPhamClientService.searchSuggest(keyword, 6);

        return list.stream()
                .map(sp -> new SearchSuggestionDTO(
                        sp.getId(),
                        sp.getTenSanPham(),      // hoặc getTenNuocHoa tùy DTO của bạn
                        sp.getTenThuongHieu(),
                        sp.getHinhAnh(),
                        sp.getGiaBan()
                ))
                .toList();
    }

}
