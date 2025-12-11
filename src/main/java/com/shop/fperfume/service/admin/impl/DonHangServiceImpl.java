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

    // ================== STATE MACHINE CHO TRẠNG THÁI ĐƠN ==================
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

        // Hoàn thành -> (rất hiếm) cho phép hủy
        ALLOWED_TRANSITIONS.put("HOAN_THANH",
                java.util.List.of("DA_HUY"));

        // Đã hủy -> không được đi đâu nữa
        ALLOWED_TRANSITIONS.put("DA_HUY",
                java.util.List.of());
    }

    // ================== HÀM HỖ TRỢ DROPDOWN TRẠNG THÁI ==================
    @Override
    public java.util.List<String> getAllowedNextTrangThais(String currentTrangThai) {
        String current = currentTrangThai == null ? "" : currentTrangThai.trim().toUpperCase();

        java.util.List<String> result = new java.util.ArrayList<>();
        if (!current.isEmpty()) {
            // luôn cho phép hiển thị trạng thái hiện tại
            result.add(current);
        }

        result.addAll(ALLOWED_TRANSITIONS.getOrDefault(current, java.util.List.of()));

        // bỏ trùng cho chắc
        return result.stream().distinct().toList();
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

    // ================== CẬP NHẬT ĐƠN HÀNG ==================
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

        // ====== CHẶN CHUYỂN TRẠNG THÁI SAI FLOW ======
        if (!old.equals(neo)) {
            java.util.List<String> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(old, java.util.List.of());
            if (!allowedTargets.contains(neo)) {
                throw new RuntimeException(
                        "Không được phép chuyển trạng thái từ " + old + " sang " + neo);
            }
        }

        boolean wasCancelled = "DA_HUY".equals(old);
        boolean isCancelled  = "DA_HUY".equals(neo);

        // trạng thái HOÀN THÀNH
        boolean wasCompleted = "HOAN_THANH".equals(old);
        boolean isCompleted  = "HOAN_THANH".equals(neo);

        // trạng thái ĐANG CHỜ THANH TOÁN (VNPay chưa trả về)
        boolean wasPendingPayment = "DANG_CHO_THANH_TOAN".equals(old);

        // 1) ACTIVE -> HỦY  => HOÀN KHO (NHƯNG KHÔNG HOÀN KHO NẾU ĐANG CHỜ THANH TOÁN)
        if (!wasCancelled && isCancelled && hoaDon.getHoaDonChiTiets() != null) {

            // Chỉ hoàn kho nếu đơn TRƯỚC ĐÓ đã trừ kho
            // => Không phải DANG_CHO_THANH_TOAN (vì VNPay chưa trừ)
            if (!wasPendingPayment) {
                for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                    if (ct.getSanPhamChiTiet() != null && ct.getSoLuong() != null) {
                        SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                        Integer ton = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                        spct.setSoLuongTon(ton + ct.getSoLuong());
                        // sanPhamChiTietRepository.save(spct); // bật nếu không dùng cascade
                    }
                }
            }

            // Nếu từ HOAN_THANH -> DA_HUY thì bỏ ngày thanh toán
            if (wasCompleted) {
                hoaDon.setNgayThanhToan(null);
            }
        }

        // 2) HỦY -> ACTIVE  => TRỪ KHO LẠI (nếu bạn còn cho phép DA_HUY -> trạng thái khác)
        else if (wasCancelled && !isCancelled && hoaDon.getHoaDonChiTiets() != null) {
            for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                if (ct.getSanPhamChiTiet() != null && ct.getSoLuong() != null) {
                    SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                    Integer ton = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();

                    if (ton < ct.getSoLuong()) {
                        throw new RuntimeException("Không đủ tồn kho để kích hoạt lại đơn hàng.");
                    }

                    spct.setSoLuongTon(ton - ct.getSoLuong());
                    // sanPhamChiTietRepository.save(spct);
                }
            }
        }

        // 3) XỬ LÝ NGÀY THANH TOÁN THEO TRẠNG THÁI HOÀN THÀNH

        // Từ trạng thái không phải HOAN_THANH -> HOAN_THANH
        if (!wasCompleted && isCompleted) {
            if (hoaDon.getNgayThanhToan() == null) {
                hoaDon.setNgayThanhToan(LocalDateTime.now());
            }
        }

        // Từ HOAN_THANH -> trạng thái khác (không gồm DA_HUY đã clear ở trên)
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
