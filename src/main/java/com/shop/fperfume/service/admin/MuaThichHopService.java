package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.MuaThichHop;
import com.shop.fperfume.model.request.MuaThichHopRequest;
import com.shop.fperfume.model.response.MuaThichHopResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.MuaThichHopRepository;
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
public class MuaThichHopService {
    @Autowired
    private MuaThichHopRepository muaThichHopRepository;

    public List<MuaThichHopResponse> getMuaThichHop() {
        return muaThichHopRepository.findAll()
                .stream()
                .map(MuaThichHopResponse::new)
                .toList();
    }

    public void addMuaThichHop(MuaThichHopRequest muaThichHopRequest){

        String maMuaMoi = muaThichHopRequest.getMaMua().trim();
        String tenMuaMoi = muaThichHopRequest.getTenMua().trim();

        if (muaThichHopRepository.existsByMaMua(maMuaMoi)) {
            throw new RuntimeException("Mã mùa '" + maMuaMoi + "' đã tồn tại!");
        }

        if (muaThichHopRepository.existsByTenMua(tenMuaMoi)) {
            throw new RuntimeException("Tên mùa '" + tenMuaMoi + "' đã tồn tại!");
        }

        MuaThichHop  muaThichHop = MapperUtils.map(muaThichHopRequest, MuaThichHop.class);
        muaThichHop.setNgayTao(LocalDateTime.now());
        muaThichHop.setNgaySua(LocalDateTime.now());
        muaThichHopRepository.save(muaThichHop);
    }

    @Transactional
    public void updateMuaThichHop(Long id, MuaThichHopRequest muaThichHopRequest) {
        MuaThichHop muaThichHop = muaThichHopRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy mùa thích hợp với ID: " + id));

        String maMuaMoi = muaThichHopRequest.getMaMua().trim();
        String tenMuaMoi = muaThichHopRequest.getTenMua().trim();

        if (muaThichHopRepository.existsByMaMua(maMuaMoi) && !maMuaMoi.equals(muaThichHop.getMaMua())) {
            throw new RuntimeException("Mã mùa '" + maMuaMoi + "' đã tồn tại!");
        }

        if (muaThichHopRepository.existsByTenMua(tenMuaMoi) && !tenMuaMoi.equals(muaThichHop.getTenMua())) {
            throw new RuntimeException("Tên mùa '" + tenMuaMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(muaThichHopRequest, muaThichHop);
        muaThichHop.setNgaySua(LocalDateTime.now());
        muaThichHopRepository.save(muaThichHop);
    }

    public void deleteMuaThichHop(Long id) {
        muaThichHopRepository.deleteById(id);
    }

    public MuaThichHopResponse getMuaThichHopById(Long id) {
        MuaThichHop muaThichHop = muaThichHopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mùa thích hợp với ID: " + id));
        return new MuaThichHopResponse(muaThichHop);
    }

    public PageableObject<MuaThichHopResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<MuaThichHop> page = muaThichHopRepository.findAll(pageable);
        Page<MuaThichHopResponse>  responses = page.map(MuaThichHopResponse::new);
        return new PageableObject<>(responses);
    }
}
