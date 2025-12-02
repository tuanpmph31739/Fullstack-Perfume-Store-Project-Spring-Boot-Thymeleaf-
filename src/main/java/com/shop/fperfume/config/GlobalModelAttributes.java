package com.shop.fperfume.config;

import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.service.client.ThuongHieuClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private ThuongHieuClientService thuongHieuClientService;

    @ModelAttribute("brands")
    public List<ThuongHieuResponse> getAllBrands() {
        return thuongHieuClientService.getAllThuongHieu();
    }
    @ModelAttribute("brandsHot")
    public List<ThuongHieuResponse> getHotBrands() {
        return thuongHieuClientService.getAllThuongHieu()
                .stream()
                .limit(6)
                .toList();
    }
}
