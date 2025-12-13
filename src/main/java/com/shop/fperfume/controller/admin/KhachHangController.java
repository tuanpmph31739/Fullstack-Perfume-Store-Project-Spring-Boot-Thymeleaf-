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
        model.addAttribute("currentPath", "/admin/khach-hang");
        return "admin/khach_hang/index";
    }

    @GetMapping("/add")
    public String add(Model model) {
        NguoiDung nd = new NguoiDung();
        nd.setVaiTro("KHACHHANG");
        model.addAttribute("nguoiDung", nd);
        model.addAttribute("currentPath", "/admin/khach-hang");
        return "admin/khach_hang/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        NguoiDung nd = service.getById(id).orElseThrow();
        model.addAttribute("nguoiDung", nd);
        model.addAttribute("currentPath", "/admin/khach-hang");
        return "admin/khach_hang/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                       @RequestParam(value = "newPassword", required = false) String newPassword,
                       RedirectAttributes redirect) {

        // cố định vai trò
        nguoiDung.setVaiTro("KHACHHANG");

        // Check email trùng
        Optional<NguoiDung> existing = repo.findByEmail(nguoiDung.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(nguoiDung.getId())) {
            redirect.addFlashAttribute("error", "Email đã tồn tại!");
            if (nguoiDung.getId() != null) {
                return "redirect:/admin/khach-hang/edit/" + nguoiDung.getId();
            }
            return "redirect:/admin/khach-hang/add";
        }

        // THÊM MỚI: bắt buộc có mật khẩu
        if (nguoiDung.getId() == null) {
            if (newPassword == null || newPassword.isBlank()) {
                redirect.addFlashAttribute("error", "Vui lòng tạo mật khẩu cho khách hàng mới!");
                return "redirect:/admin/khach-hang/add";
            }
            nguoiDung.setMatKhau(newPassword.trim()); // service.save sẽ encode
        } else {
            // UPDATE: nếu nhập pass mới thì đổi, không nhập thì giữ nguyên
            if (newPassword != null && !newPassword.isBlank()) {
                nguoiDung.setMatKhau(newPassword.trim()); // service.save sẽ encode
            } else {
                nguoiDung.setMatKhau(null); // để service.save tự giữ pass cũ
            }
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

    // API check email cho JS gọi (form của bạn đang gọi endpoint này)
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return repo.findByEmail(email).isPresent();
    }
}
