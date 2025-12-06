package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.service.admin.NguoiDungService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/khach-hang")
public class KhachHangController {

    private final NguoiDungService service;
    private final NguoiDungRepository repo;

    public KhachHangController(NguoiDungService service, NguoiDungRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Boolean trangThai,
                        Model model) {

        Pageable pageable = PageRequest.of(page, size);
        // Gọi hàm lấy Khách hàng
        Page<NguoiDung> pageData = service.getAllKhachHang(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTrangThai", trangThai);
        // Trả về view riêng cho khách hàng
        return "admin/khach_hang/index";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "admin/khach_hang/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("nguoiDung", service.getById(id).orElseThrow());
        return "admin/khach_hang/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute NguoiDung nguoiDung, RedirectAttributes redirect) {
        // Cố định vai trò là KHACHHANG
        nguoiDung.setVaiTro("KHACHHANG");

        if (nguoiDung.getMa() == null || nguoiDung.getMa().isEmpty()) {
            nguoiDung.setMa("KH" + System.currentTimeMillis());
        }

        // Check trùng email logic...

        service.save(nguoiDung);
        redirect.addFlashAttribute("success", "Lưu khách hàng thành công!");
        return "redirect:/admin/khach-hang";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/khach-hang";
    }
}