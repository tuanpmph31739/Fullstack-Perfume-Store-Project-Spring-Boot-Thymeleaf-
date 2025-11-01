package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.SanPham;
import com.shop.fperfume.model.request.GiamGiaRequest;
import com.shop.fperfume.model.response.GiamGiaResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.GiamGiaRepository;
import com.shop.fperfume.repository.SanPhamRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GiamGiaService {

    @Autowired
    private GiamGiaRepository giamGiaRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Lấy tất cả giảm giá
    @Transactional
    public List<GiamGiaResponse> getAllGiamGia() {
        return giamGiaRepository.findAll()
                .stream()
                .map(GiamGiaResponse::new)
                .toList();
    }

    // Thêm mới giảm giá
    @Transactional
    public void addGiamGia(GiamGiaRequest giamGiaRequest) {
        String maMoi = giamGiaRequest.getMa().trim();

        if (giamGiaRepository.existsByMa(maMoi)) {
            throw new RuntimeException("Mã giảm giá '" + maMoi + "' đã tồn tại!");
        }

        GiamGia giamGia = MapperUtils.map(giamGiaRequest, GiamGia.class);

        // Gán sản phẩm nếu có
        if (giamGiaRequest.getIdSanPham() != null) {
            SanPham sanPham = sanPhamRepository.findById(giamGiaRequest.getIdSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + giamGiaRequest.getIdSanPham()));
            giamGia.setSanPham(sanPham);
        } else {
            giamGia.setSanPham(null);
        }

        giamGiaRepository.save(giamGia);
    }

    // Cập nhật giảm giá
    @Transactional
    public void updateGiamGia(Long id, GiamGiaRequest giamGiaRequest) {
        GiamGia giamGia = giamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá với ID: " + id));

        String maMoi = giamGiaRequest.getMa().trim();
        if (!giamGia.getMa().equals(maMoi) && giamGiaRepository.existsByMa(maMoi)) {
            throw new RuntimeException("Mã giảm giá '" + maMoi + "' đã tồn tại!");
        }

        // Map các trường từ request sang entity
        MapperUtils.mapToExisting(giamGiaRequest, giamGia);

        // Gán sản phẩm nếu có
        if (giamGiaRequest.getIdSanPham() != null) {
            SanPham sanPham = sanPhamRepository.findById(giamGiaRequest.getIdSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + giamGiaRequest.getIdSanPham()));
            giamGia.setSanPham(sanPham);
        } else {
            giamGia.setSanPham(null);
        }

        giamGiaRepository.save(giamGia);
    }

    // Xóa giảm giá
    @Transactional
    public void deleteGiamGia(Long id) {
        giamGiaRepository.deleteById(id);
    }

    // Lấy giảm giá theo ID
    @Transactional
    public GiamGiaResponse getGiamGiaById(Long id) {
        GiamGia giamGia = giamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá với ID: " + id));
        return new GiamGiaResponse(giamGia);
    }

    // Phân trang giảm giá
    @Transactional
    public PageableObject<GiamGiaResponse> paging(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<GiamGia> page = giamGiaRepository.findAll(pageable);
        Page<GiamGiaResponse> responses = page.map(GiamGiaResponse::new);
        return new PageableObject<>(responses);
    }


}
