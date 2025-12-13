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


    @Override
    public java.util.List<String> getAllowedNextTrangThais(DonHangResponse donHang) {
        if (donHang == null) {
            return java.util.List.of();
        }

        String currentTrangThai = donHang.getTrangThai() == null
                ? ""
                : donHang.getTrangThai().trim().toUpperCase();

        String kenhBan = donHang.getKenhBan() == null
                ? ""
                : donHang.getKenhBan().trim().toUpperCase();

        Long idThanhToan = donHang.getIdThanhToan();
        String phuongThuc   = donHang.getPhuongThucThanhToan();

        // Xác định có phải VNPay không
        boolean isVnPay = false;
        if (idThanhToan != null) {
            // theo checkout: 1 = COD, 3 = VNPay
            isVnPay = (idThanhToan == 3);
        } else if (phuongThuc != null) {
            isVnPay = phuongThuc.toUpperCase().contains("VNPAY");
        }

        // Lấy rule base theo trạng thái + kênh
        java.util.List<String> base = getAllowedNextTrangThais(currentTrangThai, kenhBan);

        // Không phải WEB / không phải DANG_CHO_THANH_TOAN / không phải VNPay -> dùng rule cũ
        if (!"WEB".equalsIgnoreCase(kenhBan)
                || !"DANG_CHO_THANH_TOAN".equalsIgnoreCase(currentTrangThai)
                || !isVnPay) {
            return base;
        }

        // Trường hợp đặc biệt:
        // Web + VNPay + ĐANG_CHỜ_THANH_TOÁN -> CHỈ được huỷ
        return java.util.List.of("DA_HUY");
    }


    // ================== PAGING HÓA ĐƠN (ADMIN) ==================
    @Override
    public PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                        int pageSize,
                                                        String kenhBan,
                                                        String keyword,
                                                        String trangThai,
                                                        String sortNgayTao) {

        if (pageNo < 1) pageNo = 1;

        // sortNgayTao có thể là:
        //  - "ASC"/"DESC" (cũ)
        //  - "DATE_ASC"/"DATE_DESC"/"TOTAL_ASC"/"TOTAL_DESC" (mới)
        String sortKey;
        if ("ASC".equalsIgnoreCase(sortNgayTao)) {
            sortKey = "DATE_ASC";
        } else if ("DESC".equalsIgnoreCase(sortNgayTao)
                || sortNgayTao == null
                || sortNgayTao.isBlank()) {
            sortKey = "DATE_DESC";
        } else {
            // nếu client đã gửi dạng mới thì dùng luôn
            sortKey = sortNgayTao;
        }

        // không lọc theo phương thức thanh toán => idThanhToan = null
        return pagingHoaDon(pageNo, pageSize, kenhBan, keyword, trangThai, sortKey, null);
    }

    @Override
    public PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                        int pageSize,
                                                        String kenhBan,
                                                        String keyword,
                                                        String trangThai) {
        // mặc định: ngày tạo mới nhất
        return pagingHoaDon(pageNo, pageSize, kenhBan, keyword, trangThai, "DATE_DESC", null);
    }

    // ✅ BẢN ĐẦY ĐỦ: sort nâng cao + filter phương thức thanh toán
    @Override
    public PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                        int pageSize,
                                                        String kenhBan,
                                                        String keyword,
                                                        String trangThai,
                                                        String sortKey,
                                                        Integer idThanhToan) {

        if (pageNo < 1) pageNo = 1;

        String sort = (sortKey == null) ? "" : sortKey.trim().toUpperCase();
        Sort sortSpec;

        switch (sort) {
            case "DATE_ASC":
            case "ASC":
                sortSpec = Sort.by(Sort.Direction.ASC, "ngayTao");
                break;

            case "DATE_DESC":
            case "DESC":
            case "":
                sortSpec = Sort.by(Sort.Direction.DESC, "ngayTao");
                break;

            case "TOTAL_ASC":
                sortSpec = Sort.by(Sort.Direction.ASC, "tongThanhToan");
                break;

            case "TOTAL_DESC":
                sortSpec = Sort.by(Sort.Direction.DESC, "tongThanhToan");
                break;

            default:
                sortSpec = Sort.by(Sort.Direction.DESC, "ngayTao");
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortSpec);

        // ⚠ GỌI REPO CÓ THÊM idThanhToan
        Page<HoaDon> pageHoaDon =
                donHangRepository.searchHoaDon(keyword, kenhBan, trangThai, idThanhToan, pageable);

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

        String old     = hoaDon.getTrangThai() == null ? "" : hoaDon.getTrangThai().trim().toUpperCase();
        String neo     = trangThaiMoi == null ? "" : trangThaiMoi.trim().toUpperCase();
        String kenhBan = hoaDon.getKenhBan() == null ? "" : hoaDon.getKenhBan().trim().toUpperCase();

        // ==============================
        // 1. Kiểm tra trạng thái hợp lệ
        // ==============================
        java.util.List<String> allowedNext = getAllowedNextTrangThais(old, kenhBan);
        if (!old.equals(neo) && !allowedNext.contains(neo)) {
            throw new RuntimeException(
                    "Không thể chuyển trạng thái từ " + old + " sang " + neo + " cho kênh " + kenhBan);
        }

        boolean wasCancelled      = "DA_HUY".equals(old);
        boolean isCancelled       = "DA_HUY".equals(neo);
        boolean wasCompleted      = "HOAN_THANH".equals(old);
        boolean isCompleted       = "HOAN_THANH".equals(neo);
        boolean wasPendingPayment = "DANG_CHO_THANH_TOAN".equals(old);

        // ==============================
        // 2. Quy định có được sửa TÊN/SDT/ĐỊA CHỈ không?
        // ==============================
        boolean canEditBasicInfo = true;

        if ("WEB".equalsIgnoreCase(kenhBan)) {
            // Đơn online: nếu đã hoàn thành hoặc đã hủy -> KHÔNG cho sửa thông tin người nhận
            if ("HOAN_THANH".equals(old) || "DA_HUY".equals(old)) {
                canEditBasicInfo = false;
            }
        } else if ("TAI_QUAY".equalsIgnoreCase(kenhBan)) {
            // Đơn tại quầy:
            // - ĐÃ HỦY: không cho sửa nữa
            // - HOÀN THÀNH: cho phép đổi sang ĐÃ HỦY, nhưng không cho sửa tên/sđt/địa chỉ
            if ("DA_HUY".equals(old) || "HOAN_THANH".equals(old)) {
                canEditBasicInfo = false;
            }
        }

        // ==============================
        // 3. Xử lý tồn kho khi HỦY / KHÔI PHỤC / HOÀN THÀNH
        // ==============================

        // TỪ ACTIVE -> ĐÃ HỦY : hoàn kho (trừ trường hợp đang chờ thanh toán VNPay)
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
                hoaDon.setNgayThanhToan(null);
            }
        }
        // TỪ ĐÃ HỦY -> ACTIVE : trừ kho lại
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

        // Ngày thanh toán cho trạng thái HOÀN THÀNH
        if (!wasCompleted && isCompleted) {
            if (hoaDon.getNgayThanhToan() == null) {
                hoaDon.setNgayThanhToan(LocalDateTime.now());
            }
        }
        if (wasCompleted && !isCompleted && !isCancelled) {
            hoaDon.setNgayThanhToan(null);
        }

        // ==============================
        // 4. Cập nhật thông tin
        // ==============================

        // Chỉ cho phép sửa tên/sđt/địa chỉ nếu canEditBasicInfo = true
        if (canEditBasicInfo) {
            hoaDon.setTenNguoiNhan(tenNguoiNhan);
            hoaDon.setSdt(sdt);
            hoaDon.setDiaChi(diaChi);
        }

        // Trạng thái luôn được cập nhật (vì đã check allowedNext bên trên)
        hoaDon.setTrangThai(trangThaiMoi);
        hoaDon.setNgaySua(LocalDateTime.now());

        donHangRepository.save(hoaDon);
    }

    // ================== PAGING ĐƠN HÀNG (LỌC THEO KÊNH + SORT + PAYMENT) ==================
    @Override
    public PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                         int pageSize,
                                                         String kenhBan,
                                                         String keyword,
                                                         String trangThai,
                                                         String sortNgayTao) {
        // sortNgayTao có thể là:
        //  - "ASC"/"DESC" (cũ)
        //  - "DATE_ASC"/"DATE_DESC"/"TOTAL_ASC"/"TOTAL_DESC" (mới)
        String sortKey;
        if ("ASC".equalsIgnoreCase(sortNgayTao)) {
            sortKey = "DATE_ASC";
        } else if ("DESC".equalsIgnoreCase(sortNgayTao) || sortNgayTao == null || sortNgayTao.isBlank()) {
            sortKey = "DATE_DESC";
        } else {
            sortKey = sortNgayTao; // đã là key mới thì dùng luôn
        }

        // Mặc định không filter theo phương thức thanh toán
        return pagingDonHang(pageNo, pageSize, kenhBan, keyword, trangThai, sortKey, null);
    }

    /**
     * Bản đầy đủ: có sort nâng cao + filter phương thức thanh toán
     */
    @Override
    public PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                         int pageSize,
                                                         String kenhBan,
                                                         String keyword,
                                                         String trangThai,
                                                         String sortKey,
                                                         Integer idThanhToan) {

        if (pageNo < 1) pageNo = 1;

        // Xử lý sort: DATE_ASC / DATE_DESC / TOTAL_ASC / TOTAL_DESC (+ tương thích ASC/DESC)
        String sort = (sortKey == null) ? "" : sortKey.trim().toUpperCase();
        Sort sortSpec;

        switch (sort) {
            case "DATE_ASC":
            case "ASC":
                sortSpec = Sort.by(Sort.Direction.ASC, "ngayTao");
                break;

            case "DATE_DESC":
            case "DESC":
            case "":
                sortSpec = Sort.by(Sort.Direction.DESC, "ngayTao");
                break;

            case "TOTAL_ASC":
                sortSpec = Sort.by(Sort.Direction.ASC, "tongThanhToan");
                break;

            case "TOTAL_DESC":
                sortSpec = Sort.by(Sort.Direction.DESC, "tongThanhToan");
                break;

            default:
                sortSpec = Sort.by(Sort.Direction.DESC, "ngayTao");
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortSpec);

        // GỌI REPO MỚI: có thêm idThanhToan
        Page<HoaDon> pageHoaDon =
                donHangRepository.searchDonHang(kenhBan, keyword, trangThai, idThanhToan, pageable);

        Page<DonHangResponse> pageDto = pageHoaDon.map(this::mapToResponse);

        return new PageableObject<>(pageDto);
    }

}
