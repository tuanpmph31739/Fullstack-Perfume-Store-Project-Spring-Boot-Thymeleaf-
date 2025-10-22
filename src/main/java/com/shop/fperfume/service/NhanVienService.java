package com.shop.fperfume.service;


import com.shop.fperfume.entity.*;
import com.shop.fperfume.model.request.NhanVienRequest;
import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.NhanVienResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.repository.ChucVuRepository;
import com.shop.fperfume.repository.CuaHangRepository;
import com.shop.fperfume.repository.NhanVienRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private ChucVuRepository chucVuRepository;

    @Autowired
    private CuaHangRepository cuaHangRepository;

    public List<NhanVienResponse> findAll() {
        return nhanVienRepository.findAll().stream().map(NhanVienResponse::new).toList();
    }

    public PageableObject<NhanVienResponse>paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<NhanVien> page = nhanVienRepository.findAll(pageable);
        Page<NhanVienResponse>responses = page.map(NhanVienResponse::new);
        return new PageableObject<>(responses);
    }

    public void addNhanVien(NhanVienRequest nhanVienRequest){
        NhanVien nhanVien = MapperUtils.map(nhanVienRequest, NhanVien.class);

        ChucVu chucVu = chucVuRepository.findById(nhanVienRequest.getIdCv()).orElseThrow(() -> new RuntimeException("Không tìm chức vụ với id " + nhanVienRequest.getIdCv()));
        CuaHang cuaHang = cuaHangRepository.findById(nhanVienRequest.getIdCh()).orElseThrow(() -> new RuntimeException("Không tìm cửa hàng với id " + nhanVienRequest.getIdCh()));


        nhanVien.setChucVu(chucVu);
        nhanVien.setCuaHang(cuaHang);
        nhanVienRepository.save(nhanVien);
    }

    @Transactional
    public void updateNhanVien(Long id, NhanVienRequest nhanVienRequest){
        NhanVien nhanVien = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        MapperUtils.map(nhanVienRequest, NhanVien.class);
        nhanVien.setChucVu(chucVuRepository.findById(nhanVienRequest.getIdCv())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ với ID: " + nhanVienRequest.getIdCv())));

        nhanVien.setCuaHang(cuaHangRepository.findById(nhanVienRequest.getIdCh())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + nhanVienRequest.getIdCh())));
    }


    public void deleteNhanVien(Long id){
        nhanVienRepository.deleteById(id);
    }
}
