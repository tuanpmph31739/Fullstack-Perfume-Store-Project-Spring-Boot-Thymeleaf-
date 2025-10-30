package com.shop.fperfume.service.admin;

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

import java.time.LocalDateTime;
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

        String maThuongHieuMoi = thuongHieuRequest.getMaThuongHieu().trim();
        String tenThuongHieuMoi = thuongHieuRequest.getTenThuongHieu().trim();

        if (thuongHieuRepository.existsByMaThuongHieu(maThuongHieuMoi)) {
            throw new RuntimeException("Mã thương hiệu '" + maThuongHieuMoi + "' đã tồn tại!");
        }

        if (thuongHieuRepository.existsByTenThuongHieu(tenThuongHieuMoi)) {
            throw new RuntimeException("Tên thương hiệu '" + tenThuongHieuMoi + "' đã tồn tại!");
        }
        ThuongHieu thuongHieu = MapperUtils.map(thuongHieuRequest, ThuongHieu.class);
        thuongHieu.setNgayTao(LocalDateTime.now());
        thuongHieu.setNgaySua(LocalDateTime.now());
        thuongHieuRepository.save(thuongHieu);
    }

    @Transactional
    public void updateThuongHieu(Long id, ThuongHieuRequest thuongHieuRequest) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));

        String maThuongHieuMoi = thuongHieuRequest.getMaThuongHieu().trim();
        String tenThuongHieuMoi = thuongHieuRequest.getTenThuongHieu().trim();

        if (thuongHieuRepository.existsByMaThuongHieu(maThuongHieuMoi) && !maThuongHieuMoi.equals(thuongHieu.getMaThuongHieu())) {
            throw new RuntimeException("Mã thương hiệu '" + maThuongHieuMoi + "' đã tồn tại!");
        }

        if (thuongHieuRepository.existsByTenThuongHieu(tenThuongHieuMoi) && !tenThuongHieuMoi.equals(thuongHieu.getTenThuongHieu())) {
            throw new RuntimeException("Tên thương hiệu '" + tenThuongHieuMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(thuongHieuRequest, thuongHieu);
        thuongHieu.setNgaySua(LocalDateTime.now());

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
