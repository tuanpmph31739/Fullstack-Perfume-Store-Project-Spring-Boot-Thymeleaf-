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

        // ✅ để view đang dùng totalPages/currentPage không bị null
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("currentPage", pageData.getNumber());

        model.addAttribute("currentPath", "/admin/nhan-vien");
        return "admin/nhan_vien/index";
    }

    @GetMapping("/add")
    public String add(Model model) {
        NguoiDung nd = new NguoiDung();
        nd.setVaiTro("NHANVIEN");
        // ✅ mặc định hoạt động nếu entity của bạn cho phép null
        if (nd.getTrangThai() == null) nd.setTrangThai(true);

        model.addAttribute("nguoiDung", nd);
        model.addAttribute("currentPath", "/admin/nhan-vien");
        return "admin/nhan_vien/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Optional<NguoiDung> opt = service.getById(id);
        if (opt.isEmpty()) {
            redirect.addFlashAttribute("error", "Không tìm thấy nhân viên!");
            return "redirect:/admin/nhan-vien";
        }
        model.addAttribute("nguoiDung", opt.get());
        model.addAttribute("currentPath", "/admin/nhan-vien");
        return "admin/nhan_vien/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                       @RequestParam(value = "newPassword", required = false) String newPassword,
                       RedirectAttributes redirect) {

        // cố định vai trò
        nguoiDung.setVaiTro("NHANVIEN");

        // ✅ nếu chưa set trạng thái thì mặc định hoạt động
        if (nguoiDung.getTrangThai() == null) nguoiDung.setTrangThai(true);

        // Check email trùng
        Optional<NguoiDung> existing = repo.findByEmail(nguoiDung.getEmail());
        if (existing.isPresent() && (nguoiDung.getId() == null || !existing.get().getId().equals(nguoiDung.getId()))) {
            redirect.addFlashAttribute("error", "Email đã tồn tại!");
            if (nguoiDung.getId() != null) return "redirect:/admin/nhan-vien/edit/" + nguoiDung.getId();
            return "redirect:/admin/nhan-vien/add";
        }

        // THÊM MỚI: bắt buộc có mật khẩu
        if (nguoiDung.getId() == null) {
            if (newPassword == null || newPassword.isBlank()) {
                redirect.addFlashAttribute("error", "Nhập mật khẩu cho nhân viên mới!");
                return "redirect:/admin/nhan-vien/add";
            }
            nguoiDung.setMatKhau(newPassword.trim());
        } else {
            // UPDATE: nếu có nhập mật khẩu mới thì đổi, không nhập thì giữ nguyên
            if (newPassword != null && !newPassword.isBlank()) {
                nguoiDung.setMatKhau(newPassword.trim());
            } else {
                nguoiDung.setMatKhau(null);
            }
        }

        service.save(nguoiDung);

        redirect.addFlashAttribute("success", "Lưu nhân viên thành công!");
        return "redirect:/admin/nhan-vien";
    }

    // ✅ KHÓA / MỞ KHÓA
    @GetMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            boolean nowActive = service.toggleTrangThaiNhanVien(id); // true=hoạt động, false=khóa
            redirect.addFlashAttribute("success",
                    nowActive ? "Đã mở khóa tài khoản!" : "Đã khóa tài khoản!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không thể đổi trạng thái tài khoản!");
        }
        return "redirect:/admin/nhan-vien";
    }

    // (Tuỳ chọn) giữ lại delete, nhưng bạn sẽ không dùng nữa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("success", "Đã xóa nhân viên!");
        return "redirect:/admin/nhan-vien";
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return repo.findByEmail(email).isPresent();
    }
}
