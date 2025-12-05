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

import java.util.Optional;

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
        Page<NguoiDung> pageData = service.getAllKhachHang(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTrangThai", trangThai);
        return "admin/khach_hang/index";
    }

    @GetMapping("/add")
    public String add(Model model) {
        NguoiDung nd = new NguoiDung();
        nd.setVaiTro("KHACHHANG");
        model.addAttribute("nguoiDung", nd);
        return "admin/khach_hang/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("nguoiDung", service.getById(id).orElseThrow());
        return "admin/khach_hang/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute NguoiDung nguoiDung, RedirectAttributes redirect) {
        nguoiDung.setVaiTro("KHACHHANG");

        Optional<NguoiDung> existing = repo.findByEmail(nguoiDung.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(nguoiDung.getId())) {
            redirect.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/admin/khach-hang/add";
        }

        service.save(nguoiDung);
        redirect.addFlashAttribute("success", "Lưu khách hàng thành công!");
        return "redirect:/admin/khach-hang";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/khach-hang";
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return repo.findByEmail(email).isPresent();
    }
}