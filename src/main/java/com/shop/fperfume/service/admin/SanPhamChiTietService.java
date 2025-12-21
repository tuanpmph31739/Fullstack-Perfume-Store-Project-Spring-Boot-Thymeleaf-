package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.model.request.SanPhamChiTietRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.repository.DungTichRepository;
import com.shop.fperfume.repository.NongDoRepository;
import com.shop.fperfume.repository.SanPhamChiTietRepository;
import com.shop.fperfume.repository.SanPhamRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SanPhamChiTietService {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DungTichRepository dungTichRepository;

    @Autowired
    private NongDoRepository nongDoRepository;

    private final Path uploadDir = Paths.get("uploads");

    // --- Phương thức xử lý file ---
    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
            Path destinationFile = this.uploadDir.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + file.getOriginalFilename(), e);
        }
    }

    private void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) return;
        try {
            Path filePath = uploadDir.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + filename + " - " + e.getMessage());
        }
    }

    public List<SanPhamChiTietResponse> getAllSanPhamChiTiet() {
        return sanPhamChiTietRepository.findAllFetchingRelationships()
                .stream()
                .map(SanPhamChiTietResponse::new)
                .toList();
    }

    /** Dành cho chỗ khác cần chỉ phân trang basic */
    public PageableObject<SanPhamChiTietResponse> getPage(Integer pageNo, Integer pageSize) {
        return getPage(pageNo, pageSize, null, null, null, null, null);
    }

    /**
     * Phân trang + lọc + sort cho màn ADMIN
     * Admin thấy cả sản phẩm bị ẨN (hienThi=false)
     */
    public PageableObject<SanPhamChiTietResponse> getPage(Integer pageNo,
                                                          Integer pageSize,
                                                          String keyword,
                                                          Integer dungTichId,
                                                          Integer nongDoId,
                                                          String trangThai,
                                                          String sort) {

        if (pageNo == null || pageNo < 1) pageNo = 1;

        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) keyword = null;
        }

        if (trangThai != null) {
            trangThai = trangThai.trim();
            if (trangThai.isEmpty()) trangThai = null;
        }

        Sort sortSpec;
        if ("priceAsc".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.ASC, "giaBan");
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.DESC, "giaBan");
        } else {
            sortSpec = Sort.by(Sort.Direction.DESC, "id");
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortSpec);

        Page<SanPhamChiTiet> pageEntity =
                sanPhamChiTietRepository.searchSanPhamChiTiet(
                        keyword,
                        dungTichId,
                        nongDoId,
                        trangThai,
                        pageable
                );

        return new PageableObject<>(pageEntity.map(SanPhamChiTietResponse::new));
    }

    @Transactional
    public void addSanPhamChiTiet(SanPhamChiTietRequest req) {
        Optional<SanPhamChiTiet> existingSku =
                sanPhamChiTietRepository.findByMaSKU(req.getMaSKU().trim());

        if (existingSku.isPresent()) {
            throw new RuntimeException("Mã SKU '" + req.getMaSKU() + "' đã tồn tại!");
        }

        String tenFileAnh = saveFile(req.getHinhAnh());
        req.setHinhAnh(null);

        SanPhamChiTiet spct = MapperUtils.map(req, SanPhamChiTiet.class);
        spct.setHinhAnh(tenFileAnh);

        SanPham sanPham = sanPhamRepository.findById(req.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + req.getIdSanPham()));
        DungTich dungTich = dungTichRepository.findById(req.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("DungTich không tìm thấy với ID: " + req.getIdDungTich()));
        NongDo nongDo = nongDoRepository.findById(req.getIdNongDo())
                .orElseThrow(() -> new RuntimeException("NongDo không tìm thấy với ID: " + req.getIdNongDo()));

        spct.setSanPham(sanPham);
        spct.setDungTich(dungTich);
        spct.setNongDo(nongDo);

        // ✅ MẶC ĐỊNH: HIỂN THỊ TRÊN CLIENT
        // (ngừng kinh doanh vẫn hiển thị nếu hienThi = true)
        if (spct.getHienThi() == null) spct.setHienThi(true);

        spct.setNgayTao(LocalDateTime.now());
        spct.setNgaySua(LocalDateTime.now());

        sanPhamChiTietRepository.save(spct);
    }

    @Transactional
    public void updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest req) {
        Optional<SanPhamChiTiet> existingSku =
                sanPhamChiTietRepository.findByMaSKU(req.getMaSKU().trim());

        if (existingSku.isPresent() && !existingSku.get().getId().equals(id)) {
            throw new RuntimeException("Mã SKU '" + req.getMaSKU() + "' đã tồn tại!");
        }

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));

        // ✅ giữ trạng thái hiện/ẩn hiện tại (vì form edit thường không gửi hienThi)
        Boolean oldHienThi = spct.getHienThi();

        String tenFileAnhCu = spct.getHinhAnh();
        String tenFileAnhMoi = tenFileAnhCu;

        MultipartFile fileMoi = req.getHinhAnh();
        if (fileMoi != null && !fileMoi.isEmpty()) {
            tenFileAnhMoi = saveFile(fileMoi);
            if (tenFileAnhMoi != null && tenFileAnhCu != null && !tenFileAnhCu.equals(tenFileAnhMoi)) {
                deleteFile(tenFileAnhCu);
            }
        }

        req.setHinhAnh(null);
        MapperUtils.mapToExisting(req, spct);
        spct.setHinhAnh(tenFileAnhMoi);

        spct.setSanPham(sanPhamRepository.findById(req.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + req.getIdSanPham())));
        spct.setDungTich(dungTichRepository.findById(req.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("DungTich không tìm thấy với ID: " + req.getIdDungTich())));
        spct.setNongDo(nongDoRepository.findById(req.getIdNongDo())
                .orElseThrow(() -> new RuntimeException("NongDo không tìm thấy với ID: " + req.getIdNongDo())));

        spct.setMaSKU(req.getMaSKU());
        spct.setSoLuongTon(req.getSoLuongTon());
        spct.setGiaNhap(req.getGiaNhap());
        spct.setGiaBan(req.getGiaBan());
        spct.setTrangThai(req.getTrangThai());

        // ✅ RESTORE hienThi (không cho form edit vô tình làm null/đổi)
        spct.setHienThi(oldHienThi == null ? true : oldHienThi);

        spct.setNgaySua(LocalDateTime.now());
        sanPhamChiTietRepository.save(spct);
    }

    @Transactional
    public void deleteSanPhamChiTiet(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));

        String tenFileAnh = spct.getHinhAnh();
        sanPhamChiTietRepository.deleteById(id);
        deleteFile(tenFileAnh);
    }

    public SanPhamChiTietResponse getById(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdFetchingRelationships(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));
        return new SanPhamChiTietResponse(spct);
    }

    public List<SanPhamChiTietResponse> getAllChiTietBySanPhamId(Integer sanPhamId) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamIdFetchingRelationships(sanPhamId);
        return list.stream().map(SanPhamChiTietResponse::new).collect(Collectors.toList());
    }

    // =========================
    // ✅ TOGGLE (ADMIN AJAX)
    // =========================
    @Transactional
    public void toggleKinhDoanh(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));

        boolean current = Boolean.TRUE.equals(spct.getTrangThai());
        spct.setTrangThai(!current);
        spct.setNgaySua(LocalDateTime.now());

        sanPhamChiTietRepository.save(spct);
    }

    @Transactional
    public void toggleHienThi(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));

        boolean current = Boolean.TRUE.equals(spct.getHienThi());
        spct.setHienThi(!current);
        spct.setNgaySua(LocalDateTime.now());

        sanPhamChiTietRepository.save(spct);
    }
}
