package com.shop.fperfume.service.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.DungTichOptionResponse;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamClientService {
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    public List<SanPhamChiTietResponse> getSanPhamNoiBat() {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository
                .findDistinctBySanPham(PageRequest.of(0, 12));

        return list.stream().map(SanPhamChiTietResponse::new).toList();
    }

    public List<SanPhamChiTietResponse> getSanPhamByThuongHieu(String slug) {
        return sanPhamChiTietRepository.findByThuongHieuSlug(slug)
                .stream()
                .map(SanPhamChiTietResponse::new)
                .toList();
    }

    public SanPhamChiTietResponse getById(Integer id) {
        // Lấy thông tin sản phẩm chi tiết và các mối quan hệ liên quan
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findByIdFetchingRelationships(id)
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + id));

        return new SanPhamChiTietResponse(sanPhamChiTiet);
    }

    // Lấy giá của sản phẩm theo dung tích
    public Optional<SanPhamChiTiet> getBySanPhamAndSoMl(Integer idSanPham, Integer soMl) {
        return sanPhamChiTietRepository.findFirstBySanPhamIdAndDungTich_SoMl(idSanPham, soMl);
    }

    public List<DungTichOptionResponse> getDungTichOptions(Integer idSanPham) {
        return sanPhamChiTietRepository.findBySanPham_IdOrderByDungTich_SoMlAsc(idSanPham)
                .stream()
                .map(ct -> new DungTichOptionResponse(ct.getId(), ct.getDungTich().getSoMl(), ct.getGiaBan()))
                .toList();
    }

}
