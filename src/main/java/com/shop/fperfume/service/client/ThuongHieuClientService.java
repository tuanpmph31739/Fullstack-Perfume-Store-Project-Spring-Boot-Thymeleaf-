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
     * Lấy danh sách tất cả thương hiệu (dùng cho navbar, dropdown,…)
     * ĐÃ SẮP XẾP THEO TÊN A → Z
     */
    public List<ThuongHieuResponse> getAllThuongHieu() {
        return thuongHieuRepository.findAllByOrderByTenThuongHieuAsc()
                .stream()
                .map(ThuongHieuResponse::new)
                .toList();
    }

    public ThuongHieuResponse getBySlug(String slug) {
        var entity = thuongHieuRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu: " + slug));
        return new ThuongHieuResponse(entity);
    }

}
