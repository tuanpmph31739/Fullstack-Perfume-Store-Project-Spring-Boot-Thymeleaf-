package com.shop.fperfume.service.client;

import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import com.shop.fperfume.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanPhamClientService {
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    public List<SanPhamChiTietResponse> getSanPhamNoiBat() {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository
                .findDistinctBySanPham(PageRequest.of(0, 12));

        return list.stream().map(SanPhamChiTietResponse::new).toList();
    }

}
