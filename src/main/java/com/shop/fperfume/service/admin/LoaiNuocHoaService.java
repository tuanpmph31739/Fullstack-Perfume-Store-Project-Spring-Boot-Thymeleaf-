package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.LoaiNuocHoa;
import com.shop.fperfume.model.request.LoaiNuocHoaRequest;
import com.shop.fperfume.model.response.LoaiNuocHoaResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.LoaiNuocHoaRepository;
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

        String tenLoaiMoi = loaiNuocHoaRequest.getTenLoai().trim();

        if (loaiNuocHoaRepository.existsByTenLoai(tenLoaiMoi)) {
            throw new RuntimeException("Tên loại nước hoa '" + tenLoaiMoi + "' đã tồn tại!");
        }

        LoaiNuocHoa loaiNuocHoa = MapperUtils.map(loaiNuocHoaRequest, LoaiNuocHoa.class);
        loaiNuocHoa.setNgayTao(LocalDateTime.now());
        loaiNuocHoa.setNgaySua(LocalDateTime.now());
        loaiNuocHoaRepository.save(loaiNuocHoa);
    }

    @Transactional
    public void updateLoaiNuocHoa(Long id, LoaiNuocHoaRequest loaiNuocHoaRequest){
        LoaiNuocHoa loaiNuocHoa = loaiNuocHoaRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + id));

        String tenLoaiMoi = loaiNuocHoaRequest.getTenLoai().trim();

        if (loaiNuocHoaRepository.existsByTenLoai(tenLoaiMoi) && !tenLoaiMoi.equals(loaiNuocHoa.getTenLoai())) {
            throw new RuntimeException("Tên loại nước hoa '" + tenLoaiMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(loaiNuocHoaRequest, loaiNuocHoa);
        loaiNuocHoa.setNgaySua(LocalDateTime.now());
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

    public PageableObject<LoaiNuocHoaResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<LoaiNuocHoa> page = loaiNuocHoaRepository.findAll(pageable);
        Page<LoaiNuocHoaResponse>  responses = page.map(LoaiNuocHoaResponse::new);
        return new PageableObject<>(responses);
    }
}
