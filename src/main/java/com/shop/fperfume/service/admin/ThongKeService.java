package com.shop.fperfume.service.admin;

import com.shop.fperfume.repository.HoaDonChiTietRepository;
import com.shop.fperfume.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThongKeService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    // ===================== DTO =====================

    /** Doanh thu theo từng ngày */
    public static class DoanhThuNgayDTO {
        public LocalDate ngay;
        public BigDecimal doanhThu;

        public DoanhThuNgayDTO(LocalDate ngay, BigDecimal doanhThu) {
            this.ngay = ngay;
            this.doanhThu = doanhThu;
        }
    }

    /** Top sản phẩm bán chạy */
    public static class TopSanPhamDTO {
        public String tenSanPham;
        public Long tongSoLuong;
        public BigDecimal doanhThu;  // có thể dùng để hiển thị thêm nếu muốn

        public TopSanPhamDTO(String tenSanPham, Long tongSoLuong, BigDecimal doanhThu) {
            this.tenSanPham = tenSanPham;
            this.tongSoLuong = tongSoLuong;
            this.doanhThu = doanhThu;
        }
    }

    /** Tổng quan: tổng doanh thu + tổng đơn hoàn thành trong khoảng thời gian */
    public static class ThongKeTongQuanDTO {
        public BigDecimal tongDoanhThu;
        public Long tongDonThanhToan;

        public ThongKeTongQuanDTO(BigDecimal tongDoanhThu, Long tongDonThanhToan) {
            this.tongDoanhThu = tongDoanhThu;
            this.tongDonThanhToan = tongDonThanhToan;
        }
    }

    /** Thống kê số đơn theo trạng thái */
    public static class ThongKeTrangThaiDTO {
        public long donChoXacNhan;
        public long donDangChoThanhToan;
        public long donDangGiao;
        public long donDaHuy;
        public long donHoanThanh;

        public ThongKeTrangThaiDTO(long donChoXacNhan,
                                   long donDangChoThanhToan,
                                   long donDangGiao,
                                   long donDaHuy,
                                   long donHoanThanh) {
            this.donChoXacNhan = donChoXacNhan;
            this.donDangChoThanhToan = donDangChoThanhToan;
            this.donDangGiao = donDangGiao;
            this.donDaHuy = donDaHuy;
            this.donHoanThanh = donHoanThanh;
        }
    }

    // ===================== Helper =====================

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    // ===================== TỔNG QUAN – TẤT CẢ KÊNH =====================

    /** Tổng quan cho tất cả kênh (chỉ đơn HOAN_THANH, trong khoảng from–to) */
    public ThongKeTongQuanDTO thongKeTongQuan(LocalDate from, LocalDate to) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        BigDecimal tongDoanhThu =
                hoaDonRepository.tongDoanhThuTrongKhoang(start, end);
        Long tongDon =
                hoaDonRepository.tongDonDaThanhToanTrongKhoang(start, end);

        return new ThongKeTongQuanDTO(
                tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO,
                tongDon != null ? tongDon : 0L
        );
    }

    /** Doanh thu theo ngày cho tất cả kênh (chỉ đơn HOAN_THANH) */
    public List<DoanhThuNgayDTO> thongKeDoanhThuTheoNgay(LocalDate from, LocalDate to) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        List<Object[]> raw = hoaDonRepository.thongKeDoanhThuTheoNgay(start, end);
        List<DoanhThuNgayDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            // row[0] = java.sql.Date, row[1] = BigDecimal
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate ngay = sqlDate.toLocalDate();
            BigDecimal doanhThu = (BigDecimal) row[1];
            result.add(new DoanhThuNgayDTO(ngay, doanhThu));
        }

        return result;
    }

    /** Top sản phẩm bán chạy cho tất cả kênh (theo số lượng, chỉ đơn HOAN_THANH) */
    public List<TopSanPhamDTO> topSanPhamBanChay(LocalDate from, LocalDate to, int limit) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        List<Object[]> raw = hoaDonChiTietRepository.topSanPhamBanChay(start, end);
        List<TopSanPhamDTO> result = new ArrayList<>();

        int count = 0;
        for (Object[] row : raw) {
            if (count >= limit) break;

            String tenSP = (String) row[0];
            Long tongSoLuong = ((Number) row[1]).longValue();
            BigDecimal doanhThu = (BigDecimal) row[2];

            result.add(new TopSanPhamDTO(tenSP, tongSoLuong, doanhThu));
            count++;
        }
        return result;
    }

    /** Thống kê số đơn theo trạng thái – tổng (không phân kênh) */
    public ThongKeTrangThaiDTO thongKeDonTheoTrangThai(LocalDate from, LocalDate to) {
        // ⚠️ HIỆN TẠI ĐANG ĐẾM TOÀN BỘ THEO TRẠNG THÁI, KHÔNG LỌC THEO NGÀY
        // Nếu muốn lọc theo ngày nữa thì phải thêm query mới trong HoaDonRepository.

        long choXacNhan = hoaDonRepository.countByTrangThai("CHO_XAC_NHAN");
        long dangChoThanhToan = hoaDonRepository.countByTrangThai("DANG_CHO_THANH_TOAN");
        long dangGiao = hoaDonRepository.countByTrangThai("DANG_GIAO");
        long daHuy = hoaDonRepository.countByTrangThai("DA_HUY");
        long hoanThanh = hoaDonRepository.countByTrangThai("HOAN_THANH");

        return new ThongKeTrangThaiDTO(
                choXacNhan,
                dangChoThanhToan,
                dangGiao,
                daHuy,
                hoanThanh
        );
    }

    // ===================== THEO KÊNH (WEB / POS) =====================

    /** Tổng quan theo kênh (WEB hoặc TAI_QUAY) */
    public ThongKeTongQuanDTO thongKeTongQuanTheoKenh(LocalDate from, LocalDate to, String kenhBan) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        BigDecimal tongDoanhThu =
                hoaDonRepository.tongDoanhThuTrongKhoangTheoKenh(start, end, kenhBan);
        Long tongDon =
                hoaDonRepository.tongDonDaThanhToanTrongKhoangTheoKenh(start, end, kenhBan);

        return new ThongKeTongQuanDTO(
                tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO,
                tongDon != null ? tongDon : 0L
        );
    }

    /** Doanh thu theo ngày theo kênh (WEB / TAI_QUAY) */
    public List<DoanhThuNgayDTO> thongKeDoanhThuTheoNgayTheoKenh(LocalDate from, LocalDate to, String kenhBan) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        List<Object[]> raw = hoaDonRepository.thongKeDoanhThuTheoNgayTheoKenh(start, end, kenhBan);
        List<DoanhThuNgayDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate ngay = sqlDate.toLocalDate();
            BigDecimal doanhThu = (BigDecimal) row[1];
            result.add(new DoanhThuNgayDTO(ngay, doanhThu));
        }

        return result;
    }

    /** Top sản phẩm bán chạy theo kênh (WEB / TAI_QUAY) */
    public List<TopSanPhamDTO> topSanPhamBanChayTheoKenh(LocalDate from, LocalDate to, int limit, String kenhBan) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        List<Object[]> raw = hoaDonChiTietRepository.topSanPhamBanChayTheoKenh(start, end, kenhBan);
        List<TopSanPhamDTO> result = new ArrayList<>();

        int count = 0;
        for (Object[] row : raw) {
            if (count >= limit) break;

            String tenSP = (String) row[0];
            Long tongSoLuong = ((Number) row[1]).longValue();
            BigDecimal doanhThu = (BigDecimal) row[2];

            result.add(new TopSanPhamDTO(tenSP, tongSoLuong, doanhThu));
            count++;
        }
        return result;
    }

    /** Thống kê số đơn theo trạng thái trong từng kênh (WEB / TAI_QUAY) */
    public ThongKeTrangThaiDTO thongKeDonTheoTrangThaiTheoKenh(LocalDate from, LocalDate to, String kenhBan) {
        // Tương tự hàm tổng, hiện tại đang đếm toàn bộ theo trạng thái & kênh,
        // chưa lọc theo khoảng ngày.

        long choXacNhan = hoaDonRepository.countByKenhBanAndTrangThai(kenhBan, "CHO_XAC_NHAN");
        long dangChoThanhToan = hoaDonRepository.countByKenhBanAndTrangThai(kenhBan, "DANG_CHO_THANH_TOAN");
        long dangGiao = hoaDonRepository.countByKenhBanAndTrangThai(kenhBan, "DANG_GIAO");
        long daHuy = hoaDonRepository.countByKenhBanAndTrangThai(kenhBan, "DA_HUY");
        long hoanThanh = hoaDonRepository.countByKenhBanAndTrangThai(kenhBan, "HOAN_THANH");

        return new ThongKeTrangThaiDTO(
                choXacNhan,
                dangChoThanhToan,
                dangGiao,
                daHuy,
                hoanThanh
        );
    }
}
