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

    public static class DoanhThuNgayDTO {
        public LocalDate ngay;
        public BigDecimal doanhThu;

        public DoanhThuNgayDTO(LocalDate ngay, BigDecimal doanhThu) {
            this.ngay = ngay;
            this.doanhThu = doanhThu;
        }
    }

    public static class TopSanPhamDTO {
        public String tenSanPham;
        public Long tongSoLuong;

        public TopSanPhamDTO(String tenSanPham, Long tongSoLuong) {
            this.tenSanPham = tenSanPham;
            this.tongSoLuong = tongSoLuong;
        }
    }

    public static class ThongKeTongQuanDTO {
        public BigDecimal tongDoanhThu;
        public Long tongDonThanhToan;

        public ThongKeTongQuanDTO(BigDecimal tongDoanhThu, Long tongDonThanhToan) {
            this.tongDoanhThu = tongDoanhThu;
            this.tongDonThanhToan = tongDonThanhToan;
        }
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

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

    public List<DoanhThuNgayDTO> thongKeDoanhThuTheoNgay(LocalDate from, LocalDate to) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end = toEndOfDay(to);

        List<Object[]> raw = hoaDonRepository.thongKeDoanhThuTheoNgay(start, end);
        List<DoanhThuNgayDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate ngay = sqlDate.toLocalDate();
            BigDecimal doanhThu = (BigDecimal) row[1];
            result.add(new DoanhThuNgayDTO(ngay, doanhThu));
        }

        return result;
    }

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
            result.add(new TopSanPhamDTO(tenSP, tongSoLuong));
            count++;
        }
        return result;
    }
}
