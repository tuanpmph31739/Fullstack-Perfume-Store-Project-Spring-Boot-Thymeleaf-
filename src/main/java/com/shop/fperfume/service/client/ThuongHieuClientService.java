package com.shop.fperfume.service.client;

import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThuongHieuClientService {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    /**
     * Lấy danh sách tất cả thương hiệu (dùng cho navbar, dropdown,...)
     */
    public List<ThuongHieuResponse> getAllThuongHieu() {
        return thuongHieuRepository.findAll()
                .stream()
                .map(ThuongHieuResponse::new)
                .toList();
    }

    /**
     * Lấy thương hiệu theo slug (dùng khi xem trang chi tiết hoặc danh sách sản phẩm theo thương hiệu)
     */
    public ThuongHieuResponse getBySlug(String slug) {
        return thuongHieuRepository.findBySlug(slug)
                .map(ThuongHieuResponse::new)
                .orElse(null);
    }
}
