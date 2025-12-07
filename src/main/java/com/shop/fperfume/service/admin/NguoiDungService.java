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

    // 3. Hàm Save thông minh (Xử lý pass và mã tại đây luôn)
    public NguoiDung save(NguoiDung nd) {
        // Tạo mã nếu chưa có
        if (nd.getMa() == null || nd.getMa().isEmpty()) {
            String prefix = nd.getVaiTro().equals("NHANVIEN") ? "NV" : "KH";
            nd.setMa(prefix + System.currentTimeMillis());
        }

        // Xử lý mật khẩu
        if (nd.getId() == null) {
            // Thêm mới -> Mã hóa pass
            nd.setMatKhau(passwordEncoder.encode(nd.getMatKhau()));
        } else {
            // Cập nhật
            NguoiDung oldUser = repo.findById(nd.getId()).orElse(null);
            if (oldUser != null) {
                if (nd.getMatKhau() == null || nd.getMatKhau().isEmpty()) {
                    // Không nhập pass mới -> Giữ pass cũ
                    nd.setMatKhau(oldUser.getMatKhau());
                } else {
                    // Nhập pass mới -> Mã hóa lại
                    nd.setMatKhau(passwordEncoder.encode(nd.getMatKhau()));
                }
            }
        }
        return repo.save(nd);
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
}