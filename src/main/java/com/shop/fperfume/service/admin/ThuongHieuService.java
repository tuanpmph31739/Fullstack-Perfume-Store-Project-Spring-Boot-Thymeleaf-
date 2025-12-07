package com.shop.fperfume.service.admin;

import com.shop.fperfume.entity.ThuongHieu;
import com.shop.fperfume.model.request.ThuongHieuRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.ThuongHieuResponse;
import com.shop.fperfume.repository.ThuongHieuRepository;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Optional;
@Service
public class ThuongHieuService {
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    private final Path uploadDir = Paths.get("uploads/thuong-hieu");

    // --- XỬ LÝ FILE ẢNH (GIỐNG STYLE SanPhamChiTietService) ---

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path destinationFile = this.uploadDir.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename;
        } catch (IOException e) {
            System.err.println("Lỗi lưu file: " + e.getMessage());
            throw new RuntimeException("Không thể lưu file: " + file.getOriginalFilename(), e);
        }
    }

    private void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        try {
            Path filePath = uploadDir.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + filename + " - " + e.getMessage());
        }
    }

    public List<ThuongHieuResponse> getThuongHieu() {
        return thuongHieuRepository.findAll()
                .stream()
                .map(ThuongHieuResponse::new)
                .toList();
    }



    @Transactional
    public void addThuongHieu(ThuongHieuRequest thuongHieuRequest) {
        String maThuongHieuMoi = thuongHieuRequest.getMaThuongHieu().trim();
        String tenThuongHieuMoi = thuongHieuRequest.getTenThuongHieu().trim();

        if (thuongHieuRepository.existsByMaThuongHieu(maThuongHieuMoi)) {
            throw new RuntimeException("Mã thương hiệu '" + maThuongHieuMoi + "' đã tồn tại!");
        }

        if (thuongHieuRepository.existsByTenThuongHieu(tenThuongHieuMoi)) {
            throw new RuntimeException("Tên thương hiệu '" + tenThuongHieuMoi + "' đã tồn tại!");
        }

        // Lưu file ảnh, lấy tên file
        String tenFileAnh = saveFile(thuongHieuRequest.getHinhAnh());

        // Tránh MapperUtils map chồng lên MultipartFile
        thuongHieuRequest.setHinhAnh(null);

        // Map request -> entity
        ThuongHieu thuongHieu = MapperUtils.map(thuongHieuRequest, ThuongHieu.class);

        thuongHieu.setHinhAnh(tenFileAnh);

        thuongHieu.setNgayTao(LocalDateTime.now());
        thuongHieu.setNgaySua(LocalDateTime.now());
        thuongHieu.setSlug(generateSlug(tenThuongHieuMoi));

        thuongHieuRepository.save(thuongHieu);
    }

    @Transactional
    public void updateThuongHieu(Long id, ThuongHieuRequest thuongHieuRequest) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));

        String maThuongHieuMoi = thuongHieuRequest.getMaThuongHieu().trim();
        String tenThuongHieuMoi = thuongHieuRequest.getTenThuongHieu().trim();

        // --- Check trùng mã ---
        if (thuongHieuRepository.existsByMaThuongHieu(maThuongHieuMoi)
                && !maThuongHieuMoi.equals(thuongHieu.getMaThuongHieu())) {
            throw new RuntimeException("Mã thương hiệu '" + maThuongHieuMoi + "' đã tồn tại!");
        }

        // --- Check trùng tên ---
        if (thuongHieuRepository.existsByTenThuongHieu(tenThuongHieuMoi)
                && !tenThuongHieuMoi.equals(thuongHieu.getTenThuongHieu())) {
            throw new RuntimeException("Tên thương hiệu '" + tenThuongHieuMoi + "' đã tồn tại!");
        }

        // ================== XỬ LÝ ẢNH ==================
        String tenFileAnhCu = thuongHieu.getHinhAnh();     // tên file cũ đang lưu trong DB
        String tenFileAnhMoi = tenFileAnhCu;               // mặc định giữ nguyên nếu không up ảnh mới

        MultipartFile fileMoi = thuongHieuRequest.getHinhAnh();
        if (fileMoi != null && !fileMoi.isEmpty()) {
            // Lưu file mới
            tenFileAnhMoi = saveFile(fileMoi);

            // Nếu có file cũ và khác tên file mới thì xoá file cũ
            if (tenFileAnhCu != null && !tenFileAnhCu.equals(tenFileAnhMoi)) {
                deleteFile(tenFileAnhCu);
            }
        }

        // Để tránh MapperUtils map chồng lên MultipartFile
        thuongHieuRequest.setHinhAnh(null);

        // Map các field khác từ request -> entity hiện có
        MapperUtils.mapToExisting(thuongHieuRequest, thuongHieu);

        // Gán lại tên file ảnh (cũ hoặc mới) cho entity
        thuongHieu.setHinhAnh(tenFileAnhMoi);

        // Cập nhật ngày sửa
        thuongHieu.setNgaySua(LocalDateTime.now());

        // 🔹 Cập nhật lại slug khi tên thương hiệu đổi
        thuongHieu.setSlug(generateSlug(tenThuongHieuMoi));

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
        Page<ThuongHieuResponse> responses = page.map(ThuongHieuResponse::new);
        return new PageableObject<>(responses);
    }

    public PageableObject<ThuongHieuResponse> paging(Integer pageNo, Integer pageSize, String keyword) {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        Page<ThuongHieu> page;

        if (keyword == null || keyword.trim().isEmpty()) {
            page = thuongHieuRepository.findAll(pageable);
        } else {
            String kw = keyword.trim();
            page = thuongHieuRepository.searchByKeyword(kw, pageable);
        }

        Page<ThuongHieuResponse> responses = page.map(ThuongHieuResponse::new);
        return new PageableObject<>(responses);
    }

    // 🧩 Hàm generateSlug tái sử dụng cho thêm/sửa
    private String generateSlug(String tenThuongHieu) {
        String slug = Normalizer.normalize(tenThuongHieu, Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{M}", ""); // bỏ dấu tiếng Việt
        slug = slug.toLowerCase().replaceAll("[^a-z0-9]+", "-"); // chỉ giữ chữ + số, thay khoảng trắng bằng "-"
        return StringUtils.strip(slug, "-"); // bỏ dấu - ở đầu/cuối
    }
}
