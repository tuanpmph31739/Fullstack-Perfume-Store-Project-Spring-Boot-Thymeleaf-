package com.shop.fperfume.controller.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import com.shop.fperfume.service.admin.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/nguoidung")
public class NguoiDungController {

    private final NguoiDungService service;
    private final NguoiDungRepository nguoiDungRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public NguoiDungController(NguoiDungService service, NguoiDungRepository nguoiDungRepository) {
        this.service = service;
        this.nguoiDungRepository = nguoiDungRepository;
    }


    @GetMapping
    public String danhSach(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(required = false) Boolean trangThai,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NguoiDung> pageData = service.getAll(vaiTro, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("selectedVaiTro", vaiTro);
        model.addAttribute("selectedTrangThai", trangThai);

        return "admin/nguoi_dung/index";
    }

    // ➕ Form thêm mới
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "admin/nguoi_dung/form";
    }


    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        NguoiDung nd = service.getById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        model.addAttribute("nguoiDung", nd);
        return "admin/nguoi_dung/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute NguoiDung nguoiDung,
                       RedirectAttributes redirect) {

        Optional<NguoiDung> existing = nguoiDungRepository.findByEmail(nguoiDung.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(nguoiDung.getId())) {
            redirect.addFlashAttribute("error", "Email này đã tồn tại!");
            return "redirect:/admin/nguoidung/add";
        }

        // ✅ Nếu không có mã => sinh tự động
        if (nguoiDung.getMa() == null || nguoiDung.getMa().isEmpty()) {
            nguoiDung.setMa("ND" + System.currentTimeMillis());
        }

        // ✅ Nếu là cập nhật user cũ
        if (nguoiDung.getId() != null) {
            NguoiDung oldUser = nguoiDungRepository.findById(nguoiDung.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));


            if (nguoiDung.getMatKhau() == null || nguoiDung.getMatKhau().isEmpty()) {
                nguoiDung.setMatKhau(oldUser.getMatKhau());
            } else {
                nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
            }
        } else {
            nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        }

        nguoiDungRepository.save(nguoiDung);
        redirect.addFlashAttribute("success", "Lưu người dùng thành công!");
        return "redirect:/admin/nguoidung";
    }



    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return nguoiDungRepository.findByEmail(email).isPresent();
    }




    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/nguoidung";
    }

}
