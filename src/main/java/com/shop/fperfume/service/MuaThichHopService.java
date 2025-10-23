package com.shop.fperfume.service;

import com.shop.fperfume.entity.MuaThichHop;
import com.shop.fperfume.model.response.MuaThichHopResponse;
import com.shop.fperfume.repository.MuaThichHopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MuaThichHopService {
    @Autowired
    private MuaThichHopRepository muaThichHopRepository;

    public List<MuaThichHopResponse> getMuaThichHop() {
        return muaThichHopRepository.findAll()
                .stream()
                .map(MuaThichHopResponse::new)
                .toList();
    }

    public MuaThichHopResponse getMuaThichHopById(Long id) {
        MuaThichHop muaThichHop = muaThichHopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy muàn thích hợp với ID: " + id));
        return new MuaThichHopResponse(muaThichHop);
    }
}
