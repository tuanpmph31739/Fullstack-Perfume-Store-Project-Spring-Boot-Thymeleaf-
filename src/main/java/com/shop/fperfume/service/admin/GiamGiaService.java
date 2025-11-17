package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.GiamGia;
import com.shop.fperfume.entity.SanPhamChiTiet;
import com.shop.fperfume.model.request.GiamGiaRequest;
import com.shop.fperfume.model.response.GiamGiaResponse;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.repository.GiamGiaRepository;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GiamGiaService {

    @Autowired
    private GiamGiaRepository giamGiaRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    // Lấy tất cả giảm giá
    @Transactional
    public List<GiamGiaResponse> getAllGiamGia() {
        return giamGiaRepository.findAll()
                .stream()
                .map(GiamGiaResponse::new)
                .toList();
    }

    // ================================
    //  Thêm mới
    // ================================
    @Transactional
    public void addGiamGia(GiamGiaRequest request) {

        if (giamGiaRepository.existsByMa(request.getMa().trim())) {
            throw new RuntimeException("Mã giảm giá '" + request.getMa() + "' đã tồn tại!");
        }

        GiamGia giamGia = MapperUtils.map(request, GiamGia.class);

        // ⭐ soLuong
        giamGia.setSoLuong(request.getSoLuong());

        // ⭐ Xác định phạm vi áp dụng tự động
        if (request.getIdSanPhamChiTiet() != null) {
            giamGia.setPhamViApDung("SANPHAM");

            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(request.getIdSanPhamChiTiet())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy sản phẩm chi tiết với ID: " + request.getIdSanPhamChiTiet()
                    ));
            giamGia.setSanPhamChiTiet(spct);

        } else {
            giamGia.setPhamViApDung("TOAN_CUA_HANG");
            giamGia.setSanPhamChiTiet(null);
        }

        giamGiaRepository.save(giamGia);
    }

    // ================================
    //  Cập nhật
    // ================================
    @Transactional
    public void updateGiamGia(Integer id, GiamGiaRequest request) {

        GiamGia giamGia = giamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá ID: " + id));

        String maMoi = request.getMa().trim();

        if (!giamGia.getMa().equals(maMoi) && giamGiaRepository.existsByMa(maMoi)) {
            throw new RuntimeException("Mã giảm giá '" + maMoi + "' đã tồn tại!");
        }

        MapperUtils.mapToExisting(request, giamGia);

        // ⭐ soLuong
        giamGia.setSoLuong(request.getSoLuong());

        // ⭐ Cập nhật phạm vi áp dụng tự động
        if (request.getIdSanPhamChiTiet() != null) {
            giamGia.setPhamViApDung("SANPHAM");

            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(request.getIdSanPhamChiTiet())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy sản phẩm chi tiết với ID: " + request.getIdSanPhamChiTiet()
                    ));
            giamGia.setSanPhamChiTiet(spct);

        } else {
            giamGia.setPhamViApDung("TOAN_CUA_HANG");
            giamGia.setSanPhamChiTiet(null);
        }

        giamGiaRepository.save(giamGia);
    }

    // Xóa
    @Transactional
    public void deleteGiamGia(Integer id) {
        giamGiaRepository.deleteById(id);
    }

    // Lấy theo ID
    @Transactional
    public GiamGiaResponse getGiamGiaById(Integer id) {
        return new GiamGiaResponse(
                giamGiaRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá ID: " + id))
        );
    }

    // Phân trang
    @Transactional
    public PageableObject<GiamGiaResponse> paging(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<GiamGia> page = giamGiaRepository.findAll(pageable);
        return new PageableObject<>(page.map(GiamGiaResponse::new));
    }
}
