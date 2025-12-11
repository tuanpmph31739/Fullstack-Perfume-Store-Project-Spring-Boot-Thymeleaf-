package com.shop.fperfume.service.admin;

import com.shop.fperfume.model.response.DonHangResponse;
import com.shop.fperfume.model.response.PageableObject;

import java.util.List;

public interface DonHangService {

    PageableObject<DonHangResponse> pagingDonHang(int pageNo,
                                                  int pageSize,
                                                  String kenhBan,
                                                  String keyword,
                                                  String trangThai,
                                                  String sortNgayTao);

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

    DonHangResponse getById(Integer id);

    void updateDonHang(Integer idHoaDon,
                       String tenNguoiNhan,
                       String sdt,
                       String diaChi,
                       String trangThaiMoi);

    List<String> getAllowedNextTrangThais(String currentTrangThai);
}
