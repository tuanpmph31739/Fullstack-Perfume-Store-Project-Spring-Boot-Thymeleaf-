package com.shop.fperfume.service.admin;

import com.shop.fperfume.model.response.DonHangResponse;
import com.shop.fperfume.model.response.PageableObject;

import java.util.List;

public interface DonHangService {

    // =============== ĐƠN HÀNG (admin/don-hang) ===============
    PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                  int pageSize,
                                                  String kenhBan,
                                                  String keyword,
                                                  String trangThai,
                                                  String sortNgayTao);

    // Bản đầy đủ: sort nâng cao + filter payment
    PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                  int pageSize,
                                                  String kenhBan,
                                                  String keyword,
                                                  String trangThai,
                                                  String sortKey,
                                                  Integer idThanhToan);

    // =============== HÓA ĐƠN (admin/hoa-don) ===============
    PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                 int pageSize,
                                                 String kenhBan,
                                                 String keyword,
                                                 String trangThai,
                                                 String sortNgayTao);

    PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                 int pageSize,
                                                 String kenhBan,
                                                 String keyword,
                                                 String trangThai);

    // Bản đầy đủ cho Hóa đơn
    PageableObject<DonHangResponse> pagingHoaDon(int pageNo,
                                                 int pageSize,
                                                 String kenhBan,
                                                 String keyword,
                                                 String trangThai,
                                                 String sortKey,
                                                 Integer idThanhToan);

    DonHangResponse getById(Integer id);

    void updateDonHang(Integer idHoaDon,
                       String tenNguoiNhan,
                       String sdt,
                       String diaChi,
                       String trangThaiMoi);

    java.util.List<String> getAllowedNextTrangThais(String currentTrangThai, String kenhBan);

    java.util.List<String> getAllowedNextTrangThais(DonHangResponse donHang);
}

