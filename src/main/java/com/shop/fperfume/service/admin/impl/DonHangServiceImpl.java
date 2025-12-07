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

    @Override
    public PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                         int pageSize,
                                                         String kenhBan,
                                                         String keyword,
                                                         String trangThai) {

        if (pageNo < 1) pageNo = 1;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "ngayTao"));

        Page<HoaDon> pageHoaDon =
                donHangRepository.searchDonHang(kenhBan, keyword, trangThai, pageable);

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
        // Nếu dùng MapperUtils để map basic field:
        DonHangResponse dto = MapperUtils.map(hd, DonHangResponse.class);

        // Bổ sung các field từ quan hệ (nếu entity có mapping)
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

        // Nếu HoaDon có List<HoaDonChiTiet> chiTiets:
        if (hd.getHoaDonChiTiets() != null) {
            dto.setSoDongChiTiet(hd.getHoaDonChiTiets().size());
            int tongSL = hd.getHoaDonChiTiets().stream()
                    .collect(Collectors.summingInt(ct -> ct.getSoLuong()));
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

        boolean wasCancelled = "DA_HUY".equals(old);
        boolean isCancelled  = "DA_HUY".equals(neo);

        // 1) ACTIVE -> HỦY  => HOÀN KHO
        if (!wasCancelled && isCancelled && hoaDon.getHoaDonChiTiets() != null) {
            for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                if (ct.getSanPhamChiTiet() != null && ct.getSoLuong() != null) {
                    SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                    Integer ton = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                    spct.setSoLuongTon(ton + ct.getSoLuong());
                    // sanPhamChiTietRepository.save(spct);
                }
            }
        }

        // 2) HỦY -> ACTIVE  => TRỪ KHO LẠI
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

        // Cập nhật thông tin khác
        hoaDon.setTenNguoiNhan(tenNguoiNhan);
        hoaDon.setSdt(sdt);
        hoaDon.setDiaChi(diaChi);
        hoaDon.setTrangThai(trangThaiMoi);
        hoaDon.setNgaySua(LocalDateTime.now());

        donHangRepository.save(hoaDon);
    }




}
