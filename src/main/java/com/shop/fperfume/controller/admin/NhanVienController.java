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
@RequestMapping("/admin/nhan-vien")
public class NhanVienController {

    private final NguoiDungService service;
    private final NguoiDungRepository repo;

    public NhanVienController(NguoiDungService service, NguoiDungRepository repo) {
        this.service = service;
        this.repo = repo;
//        hi
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Boolean trangThai,
                        Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NguoiDung> pageData = service.getAllNhanVien(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTrangThai", trangThai);
        return "admin/nhan_vien/index"; // Trỏ về view riêng
    }

    @GetMapping("/add")
    public String add(Model model) {
        NguoiDung nd = new NguoiDung();
        nd.setVaiTro("NHANVIEN"); // Mặc định vai trò
        model.addAttribute("nguoiDung", nd);
        return "admin/nhan_vien/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("nguoiDung", service.getById(id).orElseThrow());
        return "admin/nhan_vien/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute NguoiDung nguoiDung, RedirectAttributes redirect) {
        nguoiDung.setVaiTro("NHANVIEN"); // Cố định vai trò

        // Check email trùng
        Optional<NguoiDung> existing = repo.findByEmail(nguoiDung.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(nguoiDung.getId())) {
            redirect.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/admin/nhan-vien/add";
        }

        service.save(nguoiDung);
        redirect.addFlashAttribute("success", "Lưu nhân viên thành công!");
        return "redirect:/admin/nhan-vien";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/nhan-vien";
    }

    // API check email cho JS gọi
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return repo.findByEmail(email).isPresent();
    }
}