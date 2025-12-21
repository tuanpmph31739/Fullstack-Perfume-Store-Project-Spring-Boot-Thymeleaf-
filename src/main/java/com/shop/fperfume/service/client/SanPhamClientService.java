package com.shop.fperfume.service.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.DungTichOptionResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class SanPhamClientService {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    // ✅ Brand slug: chỉ lấy spct HIỂN THỊ
    public List<SanPhamChiTietResponse> getSanPhamByThuongHieu(String slug) {
        return sanPhamChiTietRepository.findBySanPham_ThuongHieu_SlugAndHienThiTrue(slug)
                .stream()
                .map(SanPhamChiTietResponse::new)
                .toList();
    }

    // ✅ Chi tiết: chỉ lấy spct HIỂN THỊ
    public SanPhamChiTietResponse getById(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdFetchingRelationshipsVisible(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm (hoặc đã bị ẩn) với ID: " + id));
        return new SanPhamChiTietResponse(spct);
    }

    // ✅ Lấy giá theo dung tích: repo đã lọc hienThi=true
    public Optional<SanPhamChiTiet> getBySanPhamAndSoMl(Integer idSanPham, Integer soMl) {
        return sanPhamChiTietRepository.findFirstBySanPhamIdAndDungTich_SoMl(idSanPham, soMl);
    }

    // ✅ Options dung tích: chỉ HIỂN THỊ
    public List<DungTichOptionResponse> getDungTichOptions(Integer idSanPham) {
        return sanPhamChiTietRepository.findBySanPham_IdAndHienThiTrueOrderByDungTich_SoMlAsc(idSanPham)
                .stream()
                .map(ct -> new DungTichOptionResponse(ct.getId(), ct.getDungTich().getSoMl(), ct.getGiaBan()))
                .toList();
    }

    // ✅ Page all: repo đã lọc hienThi=true
    public Page<SanPhamChiTietResponse> pageAll(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAllSanPhamChiTiet(pageable);
        return page.map(SanPhamChiTietResponse::new);
    }

    public PageableObject<SanPhamChiTietResponse> paging(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAllSanPhamChiTiet(pageable);
        return new PageableObject<>(page.map(SanPhamChiTietResponse::new));
    }

    public List<SanPhamChiTietResponse> getSanPhamDaiDienTheoLoai(String tenLoai) {
        var list = sanPhamChiTietRepository.findDaiDienByLoaiNuocHoa(tenLoai, 0, 12);
        return list.stream().map(SanPhamChiTietResponse::new).toList();
    }

    public Page<SanPhamChiTietResponse> filterProducts(
            List<Integer> brandIds,
            String loaiNuocHoa,
            Integer minPrice,
            Integer maxPrice,
            String sort,
            int pageIndex,
            int pageSize
    ) {
        BigDecimal minP = (minPrice == null || minPrice <= 0) ? null : new BigDecimal(minPrice);
        BigDecimal maxP = (maxPrice == null || maxPrice <= 0) ? null : new BigDecimal(maxPrice);
        int brandsSize = (brandIds == null || brandIds.isEmpty()) ? 0 : brandIds.size();

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        String sortKey;
        if ("price_asc".equalsIgnoreCase(sort)) sortKey = "price_asc";
        else if ("price_desc".equalsIgnoreCase(sort)) sortKey = "price_desc";
        else if ("newest".equalsIgnoreCase(sort)) sortKey = "newest";
        else sortKey = "";

        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.searchAdvancedOneVariant(
                brandIds, brandsSize, loaiNuocHoa, minP, maxP, sortKey, pageable
        );

        return page.map(SanPhamChiTietResponse::new);
    }

    public List<SanPhamChiTietResponse> getRelatedProducts(Integer idSpct, Long idThuongHieu, Long idNhomHuong, Long idLoaiNuocHoa) {
        if (idThuongHieu == null && (idNhomHuong == null || idLoaiNuocHoa == null)) return List.of();

        Pageable pageable = PageRequest.of(0, 8);
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findRelatedProducts(idSpct, idThuongHieu, idNhomHuong, idNhomHuong, pageable);

        return list.stream().map(SanPhamChiTietResponse::new).toList();
    }

    public List<SanPhamChiTietResponse> searchSuggest(String keyword, int limit) {
        if (keyword == null || keyword.trim().length() < 2) return List.of();
        Pageable pageable = PageRequest.of(0, limit);
        return sanPhamChiTietRepository.searchSuggest(keyword.trim(), pageable)
                .stream().map(SanPhamChiTietResponse::new).toList();
    }

    public Page<SanPhamChiTietResponse> searchProducts(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) return Page.empty();

        String kw = keyword.trim();
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);

        Page<SanPhamChiTiet> entityPage = sanPhamChiTietRepository.searchByKeyword(kw, pageable);

        List<SanPhamChiTietResponse> content = entityPage.getContent()
                .stream().map(SanPhamChiTietResponse::new).toList();

        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    public long getMaxPriceBound(String loaiDb) {
        BigDecimal maxGia = sanPhamChiTietRepository.findMaxGiaByLoai(loaiDb);
        long maxBound = (maxGia != null ? maxGia.longValue() : 0L);

        long STEP = 500_000L;
        if (maxBound < STEP) maxBound = STEP;
        else maxBound = ((maxBound + STEP - 1) / STEP) * STEP;

        return maxBound;
    }

    public long getMaxPriceBoundByBrandSlug(String brandSlug, String loaiDb) {
        BigDecimal maxGia = sanPhamChiTietRepository.findMaxGiaByThuongHieuSlug(brandSlug, loaiDb);
        long maxBound = (maxGia != null ? maxGia.longValue() : 0L);

        long STEP = 500_000L;

        if (maxBound < STEP) {
            maxBound = STEP;
        } else {
            maxBound = ((maxBound + STEP - 1) / STEP) * STEP; // làm tròn lên bội STEP
        }

        return maxBound;
    }

}
