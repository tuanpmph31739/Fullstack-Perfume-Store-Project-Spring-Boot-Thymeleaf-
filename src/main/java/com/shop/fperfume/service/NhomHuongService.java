package com.shop.fperfume.service;

import com.shop.fperfume.entity.DungTich;
import com.shop.fperfume.entity.NhomHuong;
import com.shop.fperfume.model.request.NhomHuongRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.NhomHuongResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.NhomHuongRepository;
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
public class NhomHuongService {
    @Autowired
    private NhomHuongRepository nhomHuongRepository;

    public List<NhomHuongResponse> getNhomHuong(){
        return nhomHuongRepository.findAll()
                .stream()
                .map(NhomHuongResponse::new)
                .toList();
    }

    public void addNhomHuong(NhomHuongRequest nhomHuongRequest){

        String maNhomHuongMoi = nhomHuongRequest.getMaNhomHuong().trim();
        String tenNhomHuongMoi = nhomHuongRequest.getTenNhomHuong().trim();

        if (nhomHuongRepository.existsByMaNhomHuong(maNhomHuongMoi)) {
            throw new RuntimeException("Mã nhóm hương '" + maNhomHuongMoi + "' đã tồn tại!");
        }

        if (nhomHuongRepository.existsByTenNhomHuong(tenNhomHuongMoi)) {
            throw new RuntimeException("Tên nhóm hương '" + tenNhomHuongMoi + "' đã tồn tại!");
        }

        NhomHuong nhomHuong = MapperUtils.map(nhomHuongRequest, NhomHuong.class);
        nhomHuong.setNgayTao(LocalDateTime.now());
        nhomHuong.setNgaySua(LocalDateTime.now());
        nhomHuongRepository.save(nhomHuong);
    }

    @Transactional
    public void updateNhomHuong(Long id, NhomHuongRequest nhomHuongRequest){
        NhomHuong nhomHuong = nhomHuongRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm hương với ID: " + id));

        String maNhomHuongMoi = nhomHuongRequest.getMaNhomHuong().trim();
        String tenNhomHuongMoi = nhomHuongRequest.getTenNhomHuong().trim();

        if (nhomHuongRepository.existsByMaNhomHuong(maNhomHuongMoi) && !maNhomHuongMoi.equals(nhomHuong.getMaNhomHuong())) {
            throw new RuntimeException("Mã nhóm hương '" + maNhomHuongMoi + "' đã tồn tại!");
        }

        if (nhomHuongRepository.existsByTenNhomHuong(tenNhomHuongMoi) && !tenNhomHuongMoi.equals(nhomHuong.getTenNhomHuong())) {
            throw new RuntimeException("Tên nhóm hương '" + tenNhomHuongMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(nhomHuongRequest, nhomHuong);
        nhomHuong.setNgaySua(LocalDateTime.now());
        nhomHuongRepository.save(nhomHuong);
    }

    public void deleteNhomHuong(Long id){
        nhomHuongRepository.deleteById(id);
    }

    public NhomHuongResponse getNhomHuongById(Long id){
        NhomHuong nhomHuong = nhomHuongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm hương với ID: " + id));
                return new NhomHuongResponse(nhomHuong);
    }

    public PageableObject<NhomHuongResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<NhomHuong> page = nhomHuongRepository.findAll(pageable);
        Page<NhomHuongResponse>  responses = page.map(NhomHuongResponse::new);
        return new PageableObject<>(responses);
    }
}
