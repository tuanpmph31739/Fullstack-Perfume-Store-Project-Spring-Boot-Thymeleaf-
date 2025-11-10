package com.shop.fperfume.service.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.DungTichOptionResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class SanPhamClientService {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    // ✅ Lấy sản phẩm theo thương hiệu (hiển thị 1 biến thể đại diện mỗi sản phẩm)
    public List<SanPhamChiTietResponse> getSanPhamByThuongHieu(String slug) {
        return sanPhamChiTietRepository.findByThuongHieuSlug(slug)
                .stream()
                .map(SanPhamChiTietResponse::new)
                .toList();
    }

    // ✅ Chi tiết sản phẩm
    public SanPhamChiTietResponse getById(Integer id) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findByIdFetchingRelationships(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return new SanPhamChiTietResponse(sanPhamChiTiet);
    }

    // ✅ Lấy giá theo dung tích
    public Optional<SanPhamChiTiet> getBySanPhamAndSoMl(Integer idSanPham, Integer soMl) {
        return sanPhamChiTietRepository.findFirstBySanPhamIdAndDungTich_SoMl(idSanPham, soMl);
    }

    // ✅ Danh sách tùy chọn dung tích
    public List<DungTichOptionResponse> getDungTichOptions(Integer idSanPham) {
        return sanPhamChiTietRepository.findBySanPham_IdOrderByDungTich_SoMlAsc(idSanPham)
                .stream()
                .map(ct -> new DungTichOptionResponse(ct.getId(), ct.getDungTich().getSoMl(), ct.getGiaBan()))
                .toList();
    }

    // ✅ Lấy tất cả sản phẩm (1 biến thể đại diện / sản phẩm)
    public Page<SanPhamChiTietResponse> pageAll(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAllSanPhamChiTiet(pageable);
        return page.map(SanPhamChiTietResponse::new);
    }

    // ✅ Tạo đối tượng phân trang
    public PageableObject<SanPhamChiTietResponse> paging(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAllSanPhamChiTiet(pageable);
        Page<SanPhamChiTietResponse> responses = page.map(SanPhamChiTietResponse::new);
        return new PageableObject<>(responses);
    }

    // ✅ Lấy sản phẩm theo loại (Nam/Nữ/Unisex) - mỗi sp 1 biến thể rẻ nhất
    public List<SanPhamChiTietResponse> getSanPhamDaiDienTheoLoai(String tenLoai) {
        // offset = 0, limit = 12 (hiển thị 12 sản phẩm)
        var list = sanPhamChiTietRepository.findDaiDienByLoaiNuocHoa(tenLoai, 0, 12);
        return list.stream().map(SanPhamChiTietResponse::new).toList();
    }

    // ✅ Lọc sản phẩm có brand / loại / giá — chỉ 1 biến thể rẻ nhất / sản phẩm
    public Page<SanPhamChiTietResponse> filterProducts(
            List<Integer> brandIds,
            String loaiNuocHoa,  // "Nam" | "Nữ" | "Unisex" | null
            Integer minPrice,
            Integer maxPrice,
            String sort,          // price_asc|price_desc|newest|bestseller|null
            int pageIndex,
            int pageSize
    ) {
        BigDecimal minP = (minPrice == null || minPrice <= 0) ? null : new BigDecimal(minPrice);
        BigDecimal maxP = (maxPrice == null || maxPrice <= 0) ? null : new BigDecimal(maxPrice);
        int brandsSize = (brandIds == null || brandIds.isEmpty()) ? 0 : brandIds.size();

        Pageable pageable = switch (sort == null ? "" : sort) {
            case "price_asc"  -> PageRequest.of(pageIndex, pageSize, org.springframework.data.domain.Sort.by("GiaBan").ascending());
            case "price_desc" -> PageRequest.of(pageIndex, pageSize, org.springframework.data.domain.Sort.by("GiaBan").descending());
            case "newest"     -> PageRequest.of(pageIndex, pageSize, org.springframework.data.domain.Sort.by("Id").descending());
            default           -> PageRequest.of(pageIndex, pageSize);
        };

        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.searchAdvancedOneVariant(
                brandIds, brandsSize, loaiNuocHoa, minP, maxP, pageable
        );

        return page.map(SanPhamChiTietResponse::new);
    }
}
