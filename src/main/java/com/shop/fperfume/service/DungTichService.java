package com.shop.fperfume.service;

import com.shop.fperfume.entity.DungTich;
import com.shop.fperfume.entity.XuatXu;
import com.shop.fperfume.model.request.DungTichRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.XuatXuResponse;
import com.shop.fperfume.repository.DungTichRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DungTichService {

    @Autowired
    private DungTichRepository dungTichRepository;

    public List<DungTichResponse> getDungTich(){
        return dungTichRepository.findAll()
                .stream()
                .map(DungTichResponse::new)
                .toList();
    }

    @Transactional
    public void addDungTich(DungTichRequest dungTichRequest){

        String maDungTichMoi = dungTichRequest.getMaDungTich().trim();
        Integer soMlMoi = dungTichRequest.getSoMl();

        if (dungTichRepository.existsByMaDungTich(maDungTichMoi)) {
            throw new RuntimeException("Mã dung tích '" + maDungTichMoi + "' đã tồn tại!");
        }

        if (dungTichRepository.existsBySoMl(soMlMoi)) {
            throw new RuntimeException("Số Ml '" + soMlMoi + "' đã tồn tại!");
        }

        DungTich dungTich = MapperUtils.map(dungTichRequest, DungTich.class);
        dungTich.setNgayTao(LocalDateTime.now());
        dungTich.setNgaySua(LocalDateTime.now());

        dungTichRepository.save(dungTich);
    }

    @Transactional
    public void updateDungTich(Long id, DungTichRequest dungTichRequest){

        DungTich dungTich = dungTichRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dung tích với ID: " + id));

        String maDungTichMoi = dungTichRequest.getMaDungTich().trim();
        Integer soMlMoi = dungTichRequest.getSoMl();

        if (dungTichRepository.existsByMaDungTich(maDungTichMoi) && !maDungTichMoi.equals(dungTich.getMaDungTich())) {
            throw new RuntimeException("Mã dung tích '" + maDungTichMoi + "' đã tồn tại!");
        }

        if (dungTichRepository.existsBySoMl(soMlMoi) && !soMlMoi.equals(dungTich.getSoMl())) {
            throw new RuntimeException("Số Ml '" + soMlMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(dungTichRequest, dungTich);
        dungTich.setNgaySua(LocalDateTime.now());

        dungTichRepository.save(dungTich);
    }

    public void deleteDungTich(Long id){
        dungTichRepository.deleteById(id);
    }

    public DungTichResponse getDungTichById(Long id) {
        DungTich dungTich = dungTichRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dung tích với ID: " + id));
        return new DungTichResponse(dungTich);
    }

    public PageableObject<DungTichResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<DungTich> page = dungTichRepository.findAll(pageable);
        Page<DungTichResponse>  responses = page.map(DungTichResponse::new);
        return new PageableObject<>(responses);
    }

}
