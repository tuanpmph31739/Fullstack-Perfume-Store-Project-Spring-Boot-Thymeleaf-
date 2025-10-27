package com.shop.fperfume.service;

import com.shop.fperfume.entity.XuatXu;
import com.shop.fperfume.model.request.XuatXuRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.XuatXuResponse;
import com.shop.fperfume.repository.XuatXuRepository;
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
public class XuatXuService {

    @Autowired
    private XuatXuRepository xuatXuRepository;

    public List<XuatXuResponse>getAllXuatXu(){
        return xuatXuRepository.findAll()
                .stream()
                .map(XuatXuResponse::new)
                .toList();
    }

    public void addXuatXu(XuatXuRequest xuatXuRequest){

        String maXuatXuMoi = xuatXuRequest.getMaXuatXu().trim();
        String tenXuatXuMoi = xuatXuRequest.getTenXuatXu().trim();

        if (xuatXuRepository.existsByMaXuatXu(maXuatXuMoi)) {
            throw new RuntimeException("Mã xuất xứ '" + maXuatXuMoi + "' đã tồn tại!");
        }

        if (xuatXuRepository.existsByTenXuatXu(tenXuatXuMoi)) {
            throw new RuntimeException("Nơi xuất xứ '" + tenXuatXuMoi + "' đã tồn tại!");
        }

        XuatXu xuatXu = MapperUtils.map(xuatXuRequest, XuatXu.class);
        xuatXu.setNgayTao(LocalDateTime.now());
        xuatXu.setNgaySua(LocalDateTime.now());

        xuatXuRepository.save(xuatXu);
    }

    @Transactional
    public void updateXuatXu(Long id, XuatXuRequest xuatXuRequest){
        XuatXu xuatXu = xuatXuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với ID: " + id));

        String maXuatXuMoi = xuatXuRequest.getMaXuatXu().trim();
        String tenXuatXuMoi = xuatXuRequest.getTenXuatXu().trim();

        if (xuatXuRepository.existsByMaXuatXu(maXuatXuMoi) && !maXuatXuMoi.equals(xuatXu.getMaXuatXu())) {
            throw new RuntimeException("Mã xuất xứ '" + maXuatXuMoi + "' đã tồn tại!");
        }

        if (xuatXuRepository.existsByTenXuatXu(tenXuatXuMoi) && !tenXuatXuMoi.equals(xuatXu.getTenXuatXu())) {
            throw new RuntimeException("Nơi xuất xứ '" + tenXuatXuMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(xuatXuRequest, xuatXu);
        xuatXu.setNgaySua(LocalDateTime.now());
        xuatXuRepository.save(xuatXu);
    }

    public void deleteXuatXu(Long id){
        xuatXuRepository.deleteById(id);
    }

    public XuatXuResponse getXuatXuById(Long id){
        XuatXu xuatXu = xuatXuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với ID: " + id));
        return new  XuatXuResponse(xuatXu);
    }

    public PageableObject<XuatXuResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<XuatXu>page = xuatXuRepository.findAll(pageable);
        Page<XuatXuResponse>  responses = page.map(XuatXuResponse::new);
        return new PageableObject<>(responses);
    }
}
