package com.shop.fperfume.controller.client;

import com.shop.fperfume.dto.SearchSuggestionDTO;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.client.SanPhamClientService;
import com.shop.fperfume.service.client.ThuongHieuClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private SanPhamClientService sanPhamClientService;

    @Autowired
    private ThuongHieuClientService thuongHieuClientService;

    private static final int CHUNK_SIZE = 6;

    private static <T> List<List<T>> chunk(List<T> source, int chunkSize) {
        List<List<T>> result = new ArrayList<>();
        if (source == null || source.isEmpty()) return result;
        for (int i = 0; i < source.size(); i += chunkSize) {
            result.add(source.subList(i, Math.min(i + chunkSize, source.size())));
        }
        return result;
    }

    @GetMapping("/")
    public String home(Model model) {

        var nam = sanPhamClientService.getSanPhamDaiDienTheoLoai("Nam");
        var nu = sanPhamClientService.getSanPhamDaiDienTheoLoai("Nữ");
        var unisex = sanPhamClientService.getSanPhamDaiDienTheoLoai("Unisex");

        var sanPhamNamChunks = chunk(nam, 6);
        var sanPhamNuChunks = chunk(nu, 6);
        var sanPhamUnisexChunks = chunk(unisex, 6);

        model.addAttribute("sanPhamNamChunks", sanPhamNamChunks);
        model.addAttribute("sanPhamNuChunks", sanPhamNuChunks);
        model.addAttribute("sanPhamUnisexChunks", sanPhamUnisexChunks);

        model.addAttribute("hasMoreNam", sanPhamNamChunks.size() > 1);
        model.addAttribute("hasMoreNu", sanPhamNuChunks.size() > 1);
        model.addAttribute("hasMoreUnisex", sanPhamUnisexChunks.size() > 1);

        return "client/index";
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
