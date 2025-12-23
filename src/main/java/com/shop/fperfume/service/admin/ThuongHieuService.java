package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.ThuongHieu;
import com.shop.fperfume.model.request.ThuongHieuRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.repository.ThuongHieuRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ThuongHieuService {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    private final Path uploadDir = Paths.get("uploads/thuong-hieu");

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
            Path destinationFile = uploadDir.resolve(uniqueFilename);

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
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException ignored) {}
    }

    public List<ThuongHieuResponse> getThuongHieu() {
        return thuongHieuRepository.findAll().stream().map(ThuongHieuResponse::new).toList();
    }

    // ✅ normalize tên (gom space, trim) để lưu nhất quán
    private String normalizeName(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }

    @Transactional
    public void addThuongHieu(ThuongHieuRequest req) {
        String maMoi  = (req.getMaThuongHieu() == null) ? "" : req.getMaThuongHieu().trim();
        String tenMoi = normalizeName(req.getTenThuongHieu());

        if (thuongHieuRepository.existsByMaThuongHieu(maMoi)) {
            throw new RuntimeException("Mã thương hiệu '" + maMoi + "' đã tồn tại!");
        }

        if (thuongHieuRepository.existsByTenThuongHieu(tenMoi)) {
            throw new RuntimeException("Tên thương hiệu '" + tenMoi + "' đã tồn tại!");
        }

        // ✅ CHECK TRÙNG SLUG (ngăn lỗi UNIQUE KEY)
        String slug = generateSlug(tenMoi);
        if (thuongHieuRepository.existsBySlug(slug)) {
            throw new RuntimeException("Tên thương hiệu bị trùng (chỉ khác khoảng trắng/ký tự đặc biệt). Vui lòng chọn tên khác!");
        }

        String tenFileAnh = saveFile(req.getHinhAnh());
        req.setHinhAnh(null);

        // để lưu nhất quán
        req.setMaThuongHieu(maMoi);
        req.setTenThuongHieu(tenMoi);

        ThuongHieu thuongHieu = MapperUtils.map(req, ThuongHieu.class);

        thuongHieu.setHinhAnh(tenFileAnh);
        thuongHieu.setNgayTao(LocalDateTime.now());
        thuongHieu.setNgaySua(LocalDateTime.now());
        thuongHieu.setSlug(slug);

        thuongHieuRepository.save(thuongHieu);
    }

    @Transactional
    public void updateThuongHieu(Long id, ThuongHieuRequest req) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));

        String maMoi  = (req.getMaThuongHieu() == null) ? "" : req.getMaThuongHieu().trim();
        String tenMoi = normalizeName(req.getTenThuongHieu());

        if (thuongHieuRepository.existsByMaThuongHieu(maMoi)
                && !maMoi.equals(thuongHieu.getMaThuongHieu())) {
            throw new RuntimeException("Mã thương hiệu '" + maMoi + "' đã tồn tại!");
        }

        if (thuongHieuRepository.existsByTenThuongHieu(tenMoi)
                && !tenMoi.equals(thuongHieu.getTenThuongHieu())) {
            throw new RuntimeException("Tên thương hiệu '" + tenMoi + "' đã tồn tại!");
        }

        // ✅ CHECK TRÙNG SLUG (ngoại trừ chính nó)
        String slugMoi = generateSlug(tenMoi);
        if (thuongHieuRepository.existsBySlugAndIdNot(slugMoi, id)) {
            throw new RuntimeException("Tên thương hiệu bị trùng (chỉ khác khoảng trắng/ký tự đặc biệt). Vui lòng chọn tên khác!");
        }

        // ===== xử lý ảnh =====
        String tenFileAnhCu = thuongHieu.getHinhAnh();
        String tenFileAnhMoi = tenFileAnhCu;

        MultipartFile fileMoi = req.getHinhAnh();
        if (fileMoi != null && !fileMoi.isEmpty()) {
            tenFileAnhMoi = saveFile(fileMoi);
            if (tenFileAnhCu != null && !tenFileAnhCu.equals(tenFileAnhMoi)) {
                deleteFile(tenFileAnhCu);
            }
        }

        req.setHinhAnh(null);

        // lưu nhất quán
        req.setMaThuongHieu(maMoi);
        req.setTenThuongHieu(tenMoi);

        MapperUtils.mapToExisting(req, thuongHieu);

        thuongHieu.setHinhAnh(tenFileAnhMoi);
        thuongHieu.setNgaySua(LocalDateTime.now());
        thuongHieu.setSlug(slugMoi);

        thuongHieuRepository.save(thuongHieu);
    }

    public void deleteThuongHieu(Long id) {
        thuongHieuRepository.deleteById(id);
    }

    public ThuongHieuResponse getThuongHieuById(Long id) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        return new ThuongHieuResponse(thuongHieu);
    }

    public PageableObject<ThuongHieuResponse> paging(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<ThuongHieu> page = thuongHieuRepository.findAll(pageable);
        return new PageableObject<>(page.map(ThuongHieuResponse::new));
    }

    public PageableObject<ThuongHieuResponse> paging(Integer pageNo, Integer pageSize, String keyword) {
        if (pageNo == null || pageNo < 1) pageNo = 1;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        Page<ThuongHieu> page;
        if (keyword == null || keyword.trim().isEmpty()) {
            page = thuongHieuRepository.findAll(pageable);
        } else {
            page = thuongHieuRepository.searchByKeyword(keyword.trim(), pageable);
        }

        return new PageableObject<>(page.map(ThuongHieuResponse::new));
    }

    // slug: bỏ dấu -> a-z0-9 -> '-' (đảm bảo cùng logic hiện tại của bạn)
    private String generateSlug(String tenThuongHieu) {
        String slug = Normalizer.normalize(tenThuongHieu, Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{M}", "");
        slug = slug.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return StringUtils.strip(slug, "-");
    }
}
