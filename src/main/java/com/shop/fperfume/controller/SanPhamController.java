package com.shop.fperfume.controller;

import com.shop.fperfume.entity.SanPham;
import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private LoaiNuocHoaService loaiNuocHoaService;

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private XuatXuService xuatXuService;

    @Autowired
    private MuaThichHopService muaThichHopService;

    @Autowired
    private NhomHuongService nhomHuongService;

    private final int PAGE_SIZE = 5;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
        PageableObject<SanPhamResponse> pageObject = sanPhamService.paging(pageNo, PAGE_SIZE);
        model.addAttribute("page", pageObject);
        model.addAttribute("page.metaDataAvailable", true);
        model.addAttribute("page.size", PAGE_SIZE);
        return "admin/san_pham/index";
    }


    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("sanPhamRequest", new SanPhamRequest());
        loadBaseProductDropdownData(model);
        return "admin/san_pham/add";
    }


    @PostMapping("/save")
    public String add(@ModelAttribute("sanPhamRequest") SanPhamRequest request,
                      RedirectAttributes redirectAttributes) {

        sanPhamService.addSanPham(request);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm gốc thành công!");


        return "redirect:/admin/san-pham";
    }


    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("sanPhamRequest", sanPhamService.getById(id));
        loadBaseProductDropdownData(model);
        return "admin/san_pham/edit";

    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("sanPhamRequest") SanPhamRequest request) {
        sanPhamService.updateSanPham(id, request);
        return "redirect:/admin/san-pham";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        sanPhamService.deleteSanPham(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");

        return "redirect:/admin/san-pham";
    }

    private void loadBaseProductDropdownData(Model model) {
        model.addAttribute("listLoaiNuocHoa", loaiNuocHoaService.getLoaiNuocHoa());
        model.addAttribute("listThuongHieu", thuongHieuService.getThuongHieu());
        model.addAttribute("listXuatXu", xuatXuService.getAllXuatXu());
        model.addAttribute("listMuaThichHop", muaThichHopService.getMuaThichHop());
        model.addAttribute("listNhomHuong", nhomHuongService.getNhomHuong());
    }

}