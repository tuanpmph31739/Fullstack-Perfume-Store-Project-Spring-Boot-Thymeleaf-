package com.shop.fperfume.service;

import com.shop.fperfume.entity.ThuongHieu;
import com.shop.fperfume.model.request.ThuongHieuRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.repository.ThuongHieuRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThuongHieuService {
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    public List<ThuongHieuResponse> getThuongHieu() {
        return thuongHieuRepository.findAll()
                .stream()
                .map(ThuongHieuResponse::new)
                .toList();
    }

    public void addThuongHieu(ThuongHieuRequest thuongHieuRequest) {
        ThuongHieu thuongHieu = MapperUtils.map(thuongHieuRequest, ThuongHieu.class);
        thuongHieuRepository.save(thuongHieu);
    }

    @Transactional
    public void updateThuongHieu(Long id, ThuongHieuRequest thuongHieuRequest) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        thuongHieuRepository.save(thuongHieu);
    }

    public void deleteThuongHieu(Long id) {
        thuongHieuRepository.deleteById(id);
    }

    public ThuongHieuResponse getThuongHieuById(Long id) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        return new ThuongHieuResponse(thuongHieu);
    }

    public PageableObject<ThuongHieuResponse>paging(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<ThuongHieu> page = thuongHieuRepository.findAll(pageable);
        Page<ThuongHieuResponse> responses = page.map(ThuongHieuResponse::new);
        return new PageableObject<>(responses);
    }
}
