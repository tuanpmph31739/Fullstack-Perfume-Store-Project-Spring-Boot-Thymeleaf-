package com.shop.fperfume.service.client.impl;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.service.client.GioHangClientService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;



import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class GioHangClientServiceImpl implements GioHangClientService {

    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private GioHangChiTietRepository gioHangChiTietRepository;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired private GiamGiaRepository giamGiaRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public GioHang getCartByUser(NguoiDung khachHang) {
        return gioHangRepository.findByKhachHang(khachHang)
                .orElseGet(() -> {
                    GioHang gioHang = new GioHang();
                    gioHang.setKhachHang(khachHang);
                    return gioHangRepository.save(gioHang);
                });
    }

    @Override
    public GioHang addItemToCart(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer soLuongThem) {
        if (soLuongThem <= 0) throw new RuntimeException("Số lượng phải lớn hơn 0");

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        int soLuongTonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
        if (soLuongTonKho == 0) throw new RuntimeException("Sản phẩm đã hết hàng");

        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        Optional<GioHangChiTiet> existing = gioHangChiTietRepository
                .findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet);

        int soLuongTrongGio = existing.map(GioHangChiTiet::getSoLuong).orElse(0);
        int tongSoLuongMongMuon = soLuongTrongGio + soLuongThem;

        if (tongSoLuongMongMuon > soLuongTonKho) {
            int coTheThem = soLuongTonKho - soLuongTrongGio;
            throw new RuntimeException("Chỉ còn " + soLuongTonKho + " sản phẩm. Bạn chỉ có thể thêm " + coTheThem + " nữa.");
        }

        if (existing.isPresent()) {
            GioHangChiTiet item = existing.get();
            item.setSoLuong(tongSoLuongMongMuon);
            gioHangChiTietRepository.save(item);
        } else {
            GioHangChiTiet item = new GioHangChiTiet();
            item.setId(id);
            item.setGioHang(gioHang);
            item.setSanPhamChiTiet(spct);
            item.setSoLuong(soLuongThem);
            gioHangChiTietRepository.save(item);
        }
        return gioHangRepository.findById(gioHang.getId()).orElseThrow();
    }

    @Override
    public GioHang updateItemQuantity(NguoiDung khachHang, Integer idSanPhamChiTiet, Integer newSoLuong) {
        GioHang gioHang = getCartByUser(khachHang);
        GioHangChiTietId id = new GioHangChiTietId(gioHang.getId(), idSanPhamChiTiet);
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (newSoLuong <= 0) {
            gioHangChiTietRepository.delete(chiTiet);
            if (gioHang.getGioHangChiTiets() != null) gioHang.getGioHangChiTiets().remove(chiTiet);
        } else {
            SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
            int soLuongTonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            if (newSoLuong > soLuongTonKho) {
                throw new RuntimeException("Số lượng cập nhật vượt quá tồn kho! (Tồn kho: " + soLuongTonKho + ")");
            }
            chiTiet.setSoLuong(newSoLuong);
            gioHangChiTietRepository.save(chiTiet);
        }
        return gioHang;
    }

    @Override
    public GioHang removeItemFromCart(NguoiDung khachHang, Integer idSanPhamChiTiet) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHangChiTietRepository.findByGioHang_IdAndSanPhamChiTiet_Id(gioHang.getId(), idSanPhamChiTiet)
                .ifPresent(chiTiet -> {
                    gioHangChiTietRepository.delete(chiTiet);
                    if (gioHang.getGioHangChiTiets() != null) gioHang.getGioHangChiTiets().remove(chiTiet);
                });
        return gioHangRepository.findById(gioHang.getId()).orElse(gioHang);
    }

    @Override
    public GioHang applyVoucher(NguoiDung khachHang, String maGiamGia) {

        GioHang gioHang = getCartByUser(khachHang);

        GiamGia voucher = giamGiaRepository.findByMa(maGiamGia)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getNgayBatDau().isAfter(now) || voucher.getNgayKetThuc().isBefore(now)) {
            throw new RuntimeException("Voucher đã hết hạn hoặc chưa được kích hoạt");
        }

        if (!voucher.getTrangThai()) {
            throw new RuntimeException("Voucher đang bị khóa");
        }

        if (voucher.getSoLuong() == null || voucher.getSoLuong() <= 0) {
            throw new RuntimeException("Voucher đã hết số lượt sử dụng!");
        }

        if ("SANPHAM".equals(voucher.getPhamViApDung())) {
            SanPhamChiTiet spctDuocGiam = voucher.getSanPhamChiTiet();
            boolean hopLe = gioHang.getGioHangChiTiets().stream()
                    .anyMatch(ct -> ct.getSanPhamChiTiet().getId().equals(spctDuocGiam.getId()));

            if (!hopLe) {
                throw new RuntimeException("Voucher không áp dụng cho sản phẩm trong giỏ!");
            }
        }

        // ✔️ CHỈ GÁN VOUCHER VÀO GIỎ – KHÔNG TRỪ SỐ LƯỢNG
        gioHang.setGiamGia(voucher);

        return gioHangRepository.save(gioHang);
    }


    @Override
    public GioHang removeVoucher(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);
        gioHang.setGiamGia(null);
        return gioHangRepository.save(gioHang);
    }

    // --- MỚI: IMPLEMENT HÀM XÓA GIỎ HÀNG ---
    @Override
    public void clearCart(NguoiDung khachHang) {
        GioHang gioHang = getCartByUser(khachHang);

        // xóa voucher
        gioHang.setGiamGia(null);

        if (gioHang.getGioHangChiTiets() != null && !gioHang.getGioHangChiTiets().isEmpty()) {
            gioHangChiTietRepository.deleteAll(gioHang.getGioHangChiTiets());
            gioHang.getGioHangChiTiets().clear();
        }

        gioHangRepository.save(gioHang);
    }


    @Override
    public void GuiMailDonHang(HoaDon hoaDon, String emailNhan) {
        String subject = "Hóa đơn mua hàng #" + hoaDon.getMa() + " - FPerfume";

        Context context = new Context();
        context.setVariable("hoaDon", hoaDon);
        context.setVariable("emailNhan", emailNhan);

        // ⚠️ Base URL – tạm dùng localhost, sau này cho vào application.properties
        String baseUrl = "http://localhost:8080";
        String orderDetailUrl = baseUrl + "/user/orders/" + hoaDon.getMa();

        // truyền xuống template email
        context.setVariable("orderDetailUrl", orderDetailUrl);

        // Render ra HTML từ template
        String htmlContent = templateEngine.process("client/email-order", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailNhan);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // HTML
            helper.setFrom("famumintouan@gmail.com", "FPerfume");

            mailSender.send(message);
            System.out.println("Đã gửi hóa đơn cho: " + emailNhan);

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }


}