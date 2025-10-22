package com.shop.fperfume.service;

import com.shop.fperfume.entity.LoaiNuocHoa;
import com.shop.fperfume.entity.SanPham;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.repository.*;
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

}
