package com.shop.fperfume.controller;

import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DungTichService  dungTichService;

    @Autowired
    private LoaiNuocHoaService  loaiNuocHoaService;

    @Autowired
    private ThuongHieuService   thuongHieuService;

    @Autowired
    private XuatXuService  xuatXuService;

    @Autowired
    private MuaThichHopService muaThichHopService;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {

        int pageSize = 5;
        PageableObject<SanPhamResponse> pageObject = sanPhamService.paging(pageNo, pageSize);
        model.addAttribute("page", pageObject);
        return "admin/san_pham/index";
    }

    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("sanPhamRequest", new SanPhamRequest());
        model.addAttribute("listDungTich", dungTichService.getDungTich());
        model.addAttribute("listLoaiNuocHoa",  loaiNuocHoaService.getLoaiNuocHoa());
        model.addAttribute("listThuongHieu", thuongHieuService.getThuongHieu());
        model.addAttribute("listXuatXu", xuatXuService.getAllXuatXu());
        model.addAttribute("listMuaThichHop", muaThichHopService.getMuaThichHop());
        return "admin/san_pham/add";
    }

    @PostMapping("/save")
    public String add(@ModelAttribute("sanPhamRequest") SanPhamRequest request) {
        sanPhamService.addSanPham(request);
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable Long id, Model model) {
        model.addAttribute("sanPhamRequest", sanPhamService.getById(id));
        model.addAttribute("listDungTich", dungTichService.getDungTich());
        model.addAttribute("listLoaiNuocHoa", loaiNuocHoaService.getLoaiNuocHoa());
        model.addAttribute("listThuongHieu", thuongHieuService.getThuongHieu());
        model.addAttribute("listXuatXu", xuatXuService.getAllXuatXu());
        model.addAttribute("listMuaThichHop", muaThichHopService.getMuaThichHop());
        return "admin/san_pham/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("sanPhamRequest") SanPhamRequest request) {
        sanPhamService.updateSanPham(id, request);
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        sanPhamService.deleteSanPham(id);
        return "redirect:/admin/san-pham";
    }
}

