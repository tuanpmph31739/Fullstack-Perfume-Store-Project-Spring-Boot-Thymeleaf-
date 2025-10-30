package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class NguoiDungService {

    private final NguoiDungRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public NguoiDungService(NguoiDungRepository repo) {
        this.repo = repo;
    }

    public Page<NguoiDung> getAll(String vaiTro, Boolean trangThai, Pageable pageable) {
        return repo.findByFilter(vaiTro, trangThai, pageable);
    }

    public Optional<NguoiDung> getById(Long id) {
        return repo.findById(id);
    }

    public NguoiDung save(NguoiDung nd) {
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
        // Gửi email xác nhận
        sendVerificationEmail(user, siteURL);
    }

    private void sendVerificationEmail(NguoiDung user, String siteURL) {
        // Bạn sẽ thêm phần gửi mail ở đây (JavaMailSender)
        // Gợi ý nội dung:
        // "Nhấn vào link để kích hoạt: " + siteURL + "/verify?code=" + user.getVerificationCode();
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
    public boolean updatePassword(Long id, String newPassword) {
        Optional<NguoiDung> userOpt = repo.findById(id);
        if (userOpt.isPresent()) {
            NguoiDung user = userOpt.get();
            user.setMatKhau(passwordEncoder.encode(newPassword)); // Mã hoá lại mật khẩu
            repo.save(user);
            return true;
        }
        return false;
    }

}
