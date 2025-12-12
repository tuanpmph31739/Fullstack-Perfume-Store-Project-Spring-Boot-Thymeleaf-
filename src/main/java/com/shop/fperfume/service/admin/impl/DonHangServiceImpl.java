package com.shop.fperfume.service.admin.impl;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.HoaDonChiTiet;
import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.response.DonHangResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.DonHangRepository;
import com.shop.fperfume.repository.HoaDonChiTietRepository;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import com.shop.fperfume.service.admin.DonHangService;
import com.shop.fperfume.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class DonHangServiceImpl implements DonHangService {

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    // ================== STATE MACHINE CƠ BẢN ==================
    private static final java.util.Map<String, java.util.List<String>> ALLOWED_TRANSITIONS
            = new java.util.HashMap<>();

    static {
        // VNPay chưa thanh toán -> hoặc thanh toán xong (CHO_XAC_NHAN), hoặc hủy
        ALLOWED_TRANSITIONS.put("DANG_CHO_THANH_TOAN",
                java.util.List.of("CHO_XAC_NHAN", "DA_HUY"));

        // Đơn mới -> đang chuẩn bị / hủy
        ALLOWED_TRANSITIONS.put("CHO_XAC_NHAN",
                java.util.List.of("DANG_CHUAN_BI", "DA_HUY"));

        // Đang chuẩn bị -> đang giao / hủy
        ALLOWED_TRANSITIONS.put("DANG_CHUAN_BI",
                java.util.List.of("DANG_GIAO", "DA_HUY"));

        // Đang giao -> hoàn thành / hủy
        ALLOWED_TRANSITIONS.put("DANG_GIAO",
                java.util.List.of("HOAN_THANH", "DA_HUY"));

        // Hoàn thành -> mặc định cho phép hủy (sẽ custom theo kênh phía dưới)
        ALLOWED_TRANSITIONS.put("HOAN_THANH",
                java.util.List.of("DA_HUY"));

        // Đã hủy -> không được đi đâu nữa
        ALLOWED_TRANSITIONS.put("DA_HUY",
                java.util.List.of());
    }



    @Override
    public java.util.List<String> getAllowedNextTrangThais(String currentTrangThai, String kenhBan) {
        String cur = currentTrangThai == null ? "" : currentTrangThai.trim().toUpperCase();
        String channel = kenhBan == null ? "" : kenhBan.trim().toUpperCase();

        java.util.List<String> base =
                ALLOWED_TRANSITIONS.getOrDefault(cur, java.util.List.of());

        if ("WEB".equals(channel)) {
            // Đơn online đã HOÀN_THÀNH -> không cho huỷ nữa
            if ("HOAN_THANH".equals(cur)) {
                return java.util.List.of(); // không đi đâu được nữa
            }
            return base;
        }

        if ("TAI_QUAY".equals(channel)) {
            // Đơn tại quầy đang chờ thanh toán (mới tạo ở POS)
            // => chỉ cho phép HUỶ, không cho đi luồng giao hàng online
            if ("DANG_CHO_THANH_TOAN".equals(cur)) {
                return java.util.List.of("DA_HUY");
            }

            if ("HOAN_THANH".equals(cur)) {
                return java.util.List.of("DA_HUY");
            }

            // Các trạng thái khác (nếu có) dùng rule base
            return base;
        }

        // Default cho kênh khác (nếu sau này có)
        return base;
    }

    // ================== PAGING ĐƠN HÀNG (ADMIN) ==================
    @Override
    public PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                        int pageSize,
                                                        String kenhBan,
                                                        String keyword,
                                                        String trangThai,
                                                        String sortNgayTao) {

        if (pageNo < 1) pageNo = 1;

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(sortNgayTao) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(direction, "ngayTao"));

        Page<HoaDon> pageHoaDon =
                donHangRepository.searchHoaDon(keyword, kenhBan, trangThai, pageable);

        Page<DonHangResponse> pageDto = pageHoaDon.map(this::mapToResponse);

        return new PageableObject<>(pageDto);
    }

    @Override
    public PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                        int pageSize,
                                                        String kenhBan,
                                                        String keyword,
                                                        String trangThai) {

        if (pageNo < 1) pageNo = 1;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "ngayTao"));

        Page<HoaDon> pageHoaDon =
                donHangRepository.searchHoaDon(keyword, kenhBan, trangThai, pageable);

        Page<DonHangResponse> pageDto = pageHoaDon.map(this::mapToResponse);

        return new PageableObject<>(pageDto);
    }

    @Override
    public DonHangResponse getById(Integer id) {
        HoaDon hd = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        return mapToResponse(hd);
    }

    private DonHangResponse mapToResponse(HoaDon hd) {
        DonHangResponse dto = MapperUtils.map(hd, DonHangResponse.class);

        if (hd.getThanhToan() != null) {
            dto.setPhuongThucThanhToan(hd.getThanhToan().getHinhThucThanhToan());
            dto.setIdThanhToan(hd.getThanhToan().getId());
        }

        if (hd.getKhachHang() != null) {
            dto.setTenKhachHang(hd.getKhachHang().getHoTen());
            dto.setIdKH(hd.getKhachHang().getId());
        }

        if (hd.getNhanVien() != null) {
            dto.setTenNhanVien(hd.getNhanVien().getHoTen());
            dto.setIdNV(hd.getNhanVien().getId());
        }

        if (hd.getHoaDonChiTiets() != null) {
            dto.setSoDongChiTiet(hd.getHoaDonChiTiets().size());
            int tongSL = hd.getHoaDonChiTiets().stream()
                    .collect(Collectors.summingInt(HoaDonChiTiet::getSoLuong));
            dto.setTongSoLuongSanPham(tongSL);
        }

        return dto;
    }

    @Override
    @Transactional
    public void updateDonHang(Integer idHoaDon,
                              String tenNguoiNhan,
                              String sdt,
                              String diaChi,
                              String trangThaiMoi) {

        HoaDon hoaDon = donHangRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + idHoaDon));

        String old = hoaDon.getTrangThai() == null ? "" : hoaDon.getTrangThai().trim().toUpperCase();
        String neo = trangThaiMoi == null ? "" : trangThaiMoi.trim().toUpperCase();
        String kenhBan = hoaDon.getKenhBan() == null ? "" : hoaDon.getKenhBan().trim().toUpperCase();

        // ✅ 1. Kiểm tra trạng thái mới có hợp lệ cho kênh này không
        java.util.List<String> allowedNext = getAllowedNextTrangThais(old, kenhBan);
        if (!old.equals(neo) && !allowedNext.contains(neo)) {
            throw new RuntimeException(
                    "Không thể chuyển trạng thái từ " + old + " sang " + neo + " cho kênh " + kenhBan);
        }

        boolean wasCancelled = "DA_HUY".equals(old);
        boolean isCancelled  = "DA_HUY".equals(neo);

        boolean wasCompleted = "HOAN_THANH".equals(old);
        boolean isCompleted  = "HOAN_THANH".equals(neo);

        boolean wasPendingPayment = "DANG_CHO_THANH_TOAN".equals(old);

        // 1) ACTIVE -> HỦY  => HOÀN KHO (nhưng không hoàn nếu DANG_CHO_THANH_TOAN)
        if (!wasCancelled && isCancelled && hoaDon.getHoaDonChiTiets() != null) {
            if (!wasPendingPayment) {
                for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                    if (ct.getSanPhamChiTiet() != null && ct.getSoLuong() != null) {
                        SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                        Integer ton = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                        spct.setSoLuongTon(ton + ct.getSoLuong());
                    }
                }
            }
            if (wasCompleted) {
                // Đơn bị hủy -> không còn tính doanh thu
                hoaDon.setNgayThanhToan(null);
            }
        }
        // 2) HỦY -> ACTIVE => TRỪ KHO LẠI
        else if (wasCancelled && !isCancelled && hoaDon.getHoaDonChiTiets() != null) {
            for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                if (ct.getSanPhamChiTiet() != null && ct.getSoLuong() != null) {
                    SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                    Integer ton = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                    if (ton < ct.getSoLuong()) {
                        throw new RuntimeException("Không đủ tồn kho để kích hoạt lại đơn hàng.");
                    }
                    spct.setSoLuongTon(ton - ct.getSoLuong());
                }
            }
        }

        // 3) Ngày thanh toán cho trạng thái HOÀN THÀNH
        if (!wasCompleted && isCompleted) {
            if (hoaDon.getNgayThanhToan() == null) {
                hoaDon.setNgayThanhToan(LocalDateTime.now());
            }
        }
        if (wasCompleted && !isCompleted && !isCancelled) {
            hoaDon.setNgayThanhToan(null);
        }

        // Cập nhật thông tin khác
        hoaDon.setTenNguoiNhan(tenNguoiNhan);
        hoaDon.setSdt(sdt);
        hoaDon.setDiaChi(diaChi);
        hoaDon.setTrangThai(trangThaiMoi);
        hoaDon.setNgaySua(LocalDateTime.now());

        donHangRepository.save(hoaDon);
    }

    // ================== PAGING ĐƠN HÀNG (LỌC THEO KÊNH) ==================
    @Override
    public PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                         int pageSize,
                                                         String kenhBan,
                                                         String keyword,
                                                         String trangThai,
                                                         String sortNgayTao) {

        if (pageNo < 1) pageNo = 1;

        Sort.Direction direction = Sort.Direction.DESC;
        if ("ASC".equalsIgnoreCase(sortNgayTao)) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(direction, "ngayTao"));

        Page<HoaDon> pageHoaDon =
                donHangRepository.searchDonHang(kenhBan, keyword, trangThai, pageable);

        Page<DonHangResponse> pageDto = pageHoaDon.map(this::mapToResponse);

        return new PageableObject<>(pageDto);
    }

}
