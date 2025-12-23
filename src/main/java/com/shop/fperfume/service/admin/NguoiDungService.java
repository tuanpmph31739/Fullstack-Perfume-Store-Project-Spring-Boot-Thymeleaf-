package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NguoiDungService {

    @Autowired
    private NguoiDungRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;



    // 1. Lấy danh sách NHÂN VIÊN (Chỉ Admin dùng)
    public Page<NguoiDung> getAllNhanVien(String keyword, Boolean trangThai, Pageable pageable) {
        // Chỉ lấy role NHAN_VIEN
        return repo.searchAndFilter(keyword, List.of("NHANVIEN"), trangThai, pageable);
    }

    // 2. Lấy danh sách KHÁCH HÀNG (Admin và Nhân viên đều dùng)
    public Page<NguoiDung> getAllKhachHang(String keyword, Boolean trangThai, Pageable pageable) {
        // Chỉ lấy role KHACH_HANG
        return repo.searchAndFilter(keyword, List.of("KHACHHANG"), trangThai, pageable);
    }

    public Optional<NguoiDung> getById(Long id) {
        return repo.findById(id);
    }

    public NguoiDung save(NguoiDung nd) {

        // Tạo mã nếu chưa có
        if (nd.getMa() == null || nd.getMa().isEmpty()) {
            String prefix = (nd.getVaiTro() != null && nd.getVaiTro().equals("NHANVIEN")) ? "NV" : "KH";
            nd.setMa(prefix + System.currentTimeMillis());
        }

        // THÊM MỚI
        if (nd.getId() == null) {
            if (nd.getMatKhau() != null && !nd.getMatKhau().isBlank()) {
                // tránh encode lại nếu đâu đó đã encode sẵn
                if (!isBcryptHash(nd.getMatKhau())) {
                    nd.setMatKhau(passwordEncoder.encode(nd.getMatKhau()));
                }
            }
            return repo.save(nd);
        }

        // CẬP NHẬT
        NguoiDung oldUser = repo.findById(nd.getId()).orElse(null);
        if (oldUser == null) return repo.save(nd);

        String newPass = nd.getMatKhau();
        String oldPass = oldUser.getMatKhau();

        if (newPass == null || newPass.isBlank() || newPass.equals(oldPass)) {
            // không đổi pass / hoặc object đang mang hash cũ => giữ nguyên
            nd.setMatKhau(oldPass);
        } else {
            // đổi pass: nếu pass mới là plain thì encode, nếu đã là bcrypt thì giữ
            if (!isBcryptHash(newPass)) {
                nd.setMatKhau(passwordEncoder.encode(newPass));
            }
        }

        return repo.save(nd);
    }

    private boolean isBcryptHash(String s) {
        // BCrypt thường có dạng: $2a$ / $2b$ / $2y$...
        return s != null && s.matches("^\\$2[aby]\\$\\d\\d\\$.+");
    }



    public void delete(Long id) {
        repo.deleteById(id);
    }

    public void register(NguoiDung user, String siteURL) {
        user.setMatKhau(passwordEncoder.encode(user.getMatKhau()));
        user.setVerificationCode(UUID.randomUUID().toString());
        user.setEnabled(false);
        repo.save(user);

        try {
            sendVerificationEmail(user, siteURL);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Không thể gửi email xác minh: " + e.getMessage(), e);
        }
    }

    public void sendVerificationEmail(NguoiDung user, String siteURL)
            throws MessagingException, UnsupportedEncodingException {

        String subject = "Xác minh tài khoản FPerfume của bạn";
        String verifyURL = siteURL + "/verify?code=" + user.getVerificationCode();

        String content = "<p>Xin chào <b>" + user.getHoTen() + "</b>,</p>"
                + "<p>Bạn đã đăng ký tài khoản trên <b>FPerfume</b>.</p>"
                + "<p>Nhấn vào link dưới đây để kích hoạt tài khoản:</p>"
                + "<h3><a href=\"" + verifyURL + "\">XÁC MINH TÀI KHOẢN</a></h3>"
                + "<br><p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(content, true);
        helper.setFrom("famumintouan@gmail.com", "FPerfume");

        mailSender.send(message);
    }

    public boolean verify(String code) {
        Optional<NguoiDung> userOpt = repo.findByVerificationCode(code);
        if (userOpt.isEmpty()) return false;

        NguoiDung user = userOpt.get();
        user.setEnabled(true);
        user.setVerificationCode(null);
        repo.save(user);
        return true;
    }

    public Optional<NguoiDung> findByEmail(String email) {
        return repo.findByEmail(email);
    }


    @Transactional
    public boolean toggleTrangThaiNhanVien(Long id) {
        NguoiDung nd = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));

        // đảm bảo đúng vai trò
        if (nd.getVaiTro() == null || !nd.getVaiTro().equals("NHANVIEN")) {
            throw new RuntimeException("Chỉ được khóa/mở khóa tài khoản nhân viên!");
        }

        boolean current = nd.getTrangThai() != null ? nd.getTrangThai() : true; // null coi như đang hoạt động
        boolean newStatus = !current;

        nd.setTrangThai(newStatus);
        repo.save(nd);

        return newStatus;
    }

    @Transactional
    public boolean toggleTrangThaiKhachHang(Long id) {
        NguoiDung nd = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng!"));

        // đảm bảo đúng vai trò
        if (nd.getVaiTro() == null || !nd.getVaiTro().equals("KHACHHANG")) {
            throw new RuntimeException("Chỉ được khóa/mở khóa tài khoản khách hàng!");
        }

        boolean current = nd.getTrangThai() != null ? nd.getTrangThai() : true; // null coi như đang hoạt động
        boolean newStatus = !current;

        nd.setTrangThai(newStatus);
        repo.save(nd);

        return newStatus; // true = hoạt động, false = khóa
    }


}