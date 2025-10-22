package com.shop.fperfume.service;

import com.shop.fperfume.entity.DungTich;
import com.shop.fperfume.entity.LoaiNuocHoa;
import com.shop.fperfume.entity.SanPham;
import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanPhamService {
    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DungTichRepository dungTichRepository;

    @Autowired
    private LoaiNuocHoaRepository loaiNuocHoaRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private XuatXuRepository xuatXuRepository;

    public List<SanPhamResponse>getAllSanPham(){
        return sanPhamRepository.findAll()
                .stream()
                .map(SanPhamResponse::new)
                .toList();
    }

    public PageableObject<SanPhamResponse>paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<SanPham>page = sanPhamRepository.findAll(pageable);
        Page<SanPhamResponse>responses = page.map(SanPhamResponse::new);
        return new PageableObject<>(responses);
    }

    public void addSanPham(SanPhamRequest sanPhamRequest){
        SanPham sanPham = MapperUtils.map(sanPhamRequest, SanPham.class);

        DungTich dungTich = dungTichRepository.findById(sanPhamRequest.getIdDungTich()).orElseThrow(() -> new RuntimeException("Không tìm thấy dung tích với ID: " + sanPhamRequest.getIdDungTich()));
        sanPham.setDungTich(dungTich);

        sanPhamRepository.save(sanPham);
    }
}
