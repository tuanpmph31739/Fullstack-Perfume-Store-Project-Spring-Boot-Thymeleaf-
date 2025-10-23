package com.shop.fperfume.service;

import com.shop.fperfume.entity.LoaiNuocHoa;
import com.shop.fperfume.model.request.LoaiNuocHoaRequest;
import com.shop.fperfume.model.response.LoaiNuocHoaResponse;
import com.shop.fperfume.repository.LoaiNuocHoaRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoaiNuocHoaService {
    @Autowired
    private LoaiNuocHoaRepository loaiNuocHoaRepository;

    public List<LoaiNuocHoaResponse> getLoaiNuocHoa(){
        return loaiNuocHoaRepository.findAll()
                .stream()
                .map(LoaiNuocHoaResponse::new)
                .toList();
    }

    public void addLoaiNuocHoa(LoaiNuocHoaRequest loaiNuocHoaRequest){
        LoaiNuocHoa loaiNuocHoa = MapperUtils.map(loaiNuocHoaRequest, LoaiNuocHoa.class);
        loaiNuocHoaRepository.save(loaiNuocHoa);
    }

    @Transactional
    public void updateLoaiNuocHoa(Long id, LoaiNuocHoaRequest loaiNuocHoaRequest){
        LoaiNuocHoa loaiNuocHoa = loaiNuocHoaRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + id));
        MapperUtils.mapToExisting(loaiNuocHoaRequest, loaiNuocHoa);
        loaiNuocHoaRepository.save(loaiNuocHoa);
    }

    public void deleteLoaiNuocHoa(Long id){
        loaiNuocHoaRepository.deleteById(id);
    }

    public LoaiNuocHoaResponse getLoaiNuocHoaById(Long id){
        LoaiNuocHoa loaiNuocHoa = loaiNuocHoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + id));
        return new LoaiNuocHoaResponse(loaiNuocHoa);
    }
}
