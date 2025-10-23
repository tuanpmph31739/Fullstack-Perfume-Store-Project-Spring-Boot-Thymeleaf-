package com.shop.fperfume.service;

import com.shop.fperfume.entity.DungTich;
import com.shop.fperfume.model.request.DungTichRequest;
import com.shop.fperfume.model.response.DungTichResponse;
import com.shop.fperfume.repository.DungTichRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void addDungTich(DungTichRequest dungTichRequest){
        DungTich  dungTich = MapperUtils.map(dungTichRequest, DungTich.class);
        dungTichRepository.save(dungTich);
    }

    @Transactional
    public void updateDungTich(Long id, DungTichRequest dungTichRequest){
        DungTich dungTich = dungTichRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dung tích với ID: " + id));
        MapperUtils.mapToExisting(dungTichRequest, dungTich);
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

}
