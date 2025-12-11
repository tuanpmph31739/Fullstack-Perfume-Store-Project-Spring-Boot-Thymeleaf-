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
        public String hinhAnh;
        public Long tongSoLuong;
        public BigDecimal tongDoanhThu;

        public TopSanPhamDTO(String tenSanPham, String hinhAnh, Long tongSoLuong, BigDecimal tongDoanhThu) {
            this.tenSanPham = tenSanPham;
            this.hinhAnh = hinhAnh;
            this.tongSoLuong = tongSoLuong;
            this.tongDoanhThu = tongDoanhThu;
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
            Object dateObj = row[0];
            LocalDate ngay;

            if (dateObj instanceof java.sql.Date sqlDate) {
                ngay = sqlDate.toLocalDate();
            } else if (dateObj instanceof LocalDate ld) {
                ngay = ld;
            } else if (dateObj instanceof LocalDateTime ldt) {
                ngay = ldt.toLocalDate();
            } else {
                // fallback: để tránh crash nếu kiểu lạ
                ngay = from;
            }

            Object doanhThuObj = row[1];
            BigDecimal doanhThu;
            if (doanhThuObj instanceof BigDecimal bd) {
                doanhThu = bd;
            } else if (doanhThuObj instanceof Number num) {
                doanhThu = BigDecimal.valueOf(num.doubleValue());
            } else {
                doanhThu = BigDecimal.ZERO;
            }

            result.add(new DoanhThuNgayDTO(ngay, doanhThu));
        }

        return result;
    }


    public List<TopSanPhamDTO> topSanPhamBanChay(LocalDate from, LocalDate to, int limit) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end   = toEndOfDay(to);

        List<Object[]> raw = hoaDonChiTietRepository.topSanPhamBanChay(start, end);
        List<TopSanPhamDTO> result = new ArrayList<>();

        int count = 0;
        for (Object[] row : raw) {
            if (count >= limit) break;

            String tenSP       = (String) row[0];
            String hinhAnh     = (String) row[1];
            Long tongSoLuong   = ((Number) row[2]).longValue();
            BigDecimal tongDT  = (BigDecimal) row[3];

            result.add(new TopSanPhamDTO(tenSP, hinhAnh, tongSoLuong,
                    tongDT != null ? tongDT : BigDecimal.ZERO));
            count++;
        }
        return result;
    }



    public ThongKeTongQuanDTO thongKeTongQuanTheoKenh(LocalDate from, LocalDate to, String kenhBan) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end   = toEndOfDay(to);

        BigDecimal tongDoanhThu =
                hoaDonRepository.tongDoanhThuTrongKhoangTheoKenh(start, end, kenhBan);
        Long tongDon =
                hoaDonRepository.tongDonDaThanhToanTrongKhoangTheoKenh(start, end, kenhBan);

        return new ThongKeTongQuanDTO(
                tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO,
                tongDon != null ? tongDon : 0L
        );
    }

    public List<DoanhThuNgayDTO> thongKeDoanhThuTheoNgayTheoKenh(LocalDate from, LocalDate to, String kenhBan) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end   = toEndOfDay(to);

        List<Object[]> raw = hoaDonRepository.thongKeDoanhThuTheoNgayTheoKenh(start, end, kenhBan);
        List<DoanhThuNgayDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            Object dateObj = row[0];
            LocalDate ngay;
            if (dateObj instanceof java.sql.Date sqlDate) {
                ngay = sqlDate.toLocalDate();
            } else if (dateObj instanceof LocalDate ld) {
                ngay = ld;
            } else if (dateObj instanceof LocalDateTime ldt) {
                ngay = ldt.toLocalDate();
            } else {
                ngay = from;
            }

            Object doanhThuObj = row[1];
            BigDecimal doanhThu;
            if (doanhThuObj instanceof BigDecimal bd) {
                doanhThu = bd;
            } else if (doanhThuObj instanceof Number num) {
                doanhThu = BigDecimal.valueOf(num.doubleValue());
            } else {
                doanhThu = BigDecimal.ZERO;
            }

            result.add(new DoanhThuNgayDTO(ngay, doanhThu));
        }

        return result;
    }


    public List<TopSanPhamDTO> topSanPhamBanChayTheoKenh(LocalDate from, LocalDate to, String kenhBan, int limit) {
        LocalDateTime start = toStartOfDay(from);
        LocalDateTime end   = toEndOfDay(to);

        List<Object[]> raw = hoaDonChiTietRepository.topSanPhamBanChayTheoKenh(start, end, kenhBan);
        List<TopSanPhamDTO> result = new ArrayList<>();

        int count = 0;
        for (Object[] row : raw) {
            if (count >= limit) break;

            String tenSP       = (String) row[0];
            String hinhAnh     = (String) row[1];
            Long tongSoLuong   = ((Number) row[2]).longValue();
            BigDecimal tongDT  = (BigDecimal) row[3];

            result.add(new TopSanPhamDTO(tenSP, hinhAnh, tongSoLuong,
                    tongDT != null ? tongDT : BigDecimal.ZERO));
            count++;
        }
        return result;
    }

}
