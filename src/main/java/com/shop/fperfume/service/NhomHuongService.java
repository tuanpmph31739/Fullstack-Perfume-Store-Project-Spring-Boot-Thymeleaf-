package com.shop.fperfume.service;


import com.shop.fperfume.entity.NhomHuong;
import com.shop.fperfume.model.response.NhomHuongResponse;
import com.shop.fperfume.repository.NhomHuongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public NhomHuongResponse getNhomHuongById(Long id){
        NhomHuong nhomHuong = nhomHuongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm hương với ID: " + id));
                return new NhomHuongResponse(nhomHuong);
    }
}
