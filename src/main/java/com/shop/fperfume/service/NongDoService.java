package com.shop.fperfume.service;

import com.shop.fperfume.entity.DungTich;
import com.shop.fperfume.entity.NongDo;
import com.shop.fperfume.model.request.NongDoRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.NongDoResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.NongDoRepository;
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
public class NongDoService {
    @Autowired
    private NongDoRepository nongDoRepository;

    public List<NongDoResponse>getAllNongDo(){
        return nongDoRepository.findAll()
                .stream()
                .map(NongDoResponse::new)
                .toList();
    }

    public void addNongDo(NongDoRequest nongDoRequest){

        String maNongDoMoi = nongDoRequest.getMaNongDo().trim();
        String tenNongDoMoi = nongDoRequest.getTenNongDo().trim();

        if (nongDoRepository.existsByMaNongDo(maNongDoMoi)) {
            throw new RuntimeException("Mã nồng độ '" + maNongDoMoi + "' đã tồn tại!");
        }

        if (nongDoRepository.existsByTenNongDo(tenNongDoMoi)) {
            throw new RuntimeException("Tên nồng độ '" + tenNongDoMoi + "' đã tồn tại!");
        }

        NongDo nongDo = MapperUtils.map(nongDoRequest, NongDo.class);
        nongDo.setNgayTao(LocalDateTime.now());
        nongDo.setNgaySua(LocalDateTime.now());
        nongDoRepository.save(nongDo);
    }

    @Transactional
    public void updateNongDo(Long id, NongDoRequest nongDoRequest){
        NongDo nongDo = nongDoRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nồng độ với ID: " + id));

        String maNongDoMoi = nongDoRequest.getMaNongDo().trim();
        String tenNongDoMoi = nongDoRequest.getTenNongDo().trim();

        if (nongDoRepository.existsByMaNongDo(maNongDoMoi) && !maNongDoMoi.equals(nongDo.getMaNongDo())) {
            throw new RuntimeException("Mã nồng độ '" + maNongDoMoi + "' đã tồn tại!");
        }

        if (nongDoRepository.existsByTenNongDo(tenNongDoMoi) && !tenNongDoMoi.equals(nongDo.getTenNongDo())) {
            throw new RuntimeException("Tên nồng độ '" + tenNongDoMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(nongDoRequest, nongDo);
        nongDo.setNgaySua(LocalDateTime.now());
        nongDoRepository.save(nongDo);
    }

    public void deleteNongDo(Long id){
        nongDoRepository.deleteById(id);
    }

    public NongDoResponse getNongDoById(Long id){
        NongDo nongDo = nongDoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nồng độ với ID: " + id));
        return new NongDoResponse(nongDo);
    }

    public PageableObject<NongDoResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<NongDo> page = nongDoRepository.findAll(pageable);
        Page<NongDoResponse>  responses = page.map(NongDoResponse::new);
        return new PageableObject<>(responses);
    }
}
