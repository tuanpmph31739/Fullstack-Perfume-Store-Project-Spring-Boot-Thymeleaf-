package com.shop.fperfume.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

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

    private final Path uploadDir = Paths.get("src", "main", "resources", "static", "images", "uploads");

    // --- Phương thức xử lý file ---
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

    // --- Các phương thức CRUD ---

    public List<SanPhamChiTietResponse> getAllSanPhamChiTiet(){
        // Sử dụng JOIN FETCH (Giả sử repository có phương thức này)
        return sanPhamChiTietRepository.findAllFetchingRelationships()
                .stream()
                .map(SanPhamChiTietResponse::new)
                .toList();
    }

    public PageableObject<SanPhamChiTietResponse> paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        // Sử dụng JOIN FETCH (Giả sử repository có phương thức này)
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAllFetchingRelationships(pageable);
        Page<SanPhamChiTietResponse> responses = page.map(SanPhamChiTietResponse::new);
        return new PageableObject<>(responses);
    }

    @Transactional
    public SanPhamChiTiet addSanPhamChiTiet(SanPhamChiTietRequest sanPhamChiTietRequest) {

        String tenFileAnh = saveFile(sanPhamChiTietRequest.getHinhAnh());
        sanPhamChiTietRequest.setHinhAnh(null);

        SanPhamChiTiet sanPhamChiTiet = MapperUtils.map(sanPhamChiTietRequest, SanPhamChiTiet.class);
        sanPhamChiTiet.setHinhAnh(tenFileAnh);

        // Tìm và set các entity liên quan (dùng RuntimeException)
        SanPham sanPham = sanPhamRepository.findById(sanPhamChiTietRequest.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + sanPhamChiTietRequest.getIdSanPham())); // <-- ĐÃ SỬA
        DungTich dungTich = dungTichRepository.findById(sanPhamChiTietRequest.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("DungTich không tìm thấy với ID: " + sanPhamChiTietRequest.getIdDungTich())); // <-- ĐÃ SỬA
        NongDo nongDo = nongDoRepository.findById(sanPhamChiTietRequest.getIdNongDo())
                .orElseThrow(() -> new RuntimeException("NongDo không tìm thấy với ID: " + sanPhamChiTietRequest.getIdNongDo())); // <-- ĐÃ SỬA

        sanPhamChiTiet.setSanPham(sanPham);
        sanPhamChiTiet.setDungTich(dungTich);
        sanPhamChiTiet.setNongDo(nongDo);

        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Transactional
    public SanPhamChiTiet updateSanPhamChiTiet(Long id, SanPhamChiTietRequest sanPhamChiTietRequest) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id)); // <-- ĐÃ SỬA

        String tenFileAnhCu = sanPhamChiTiet.getHinhAnh();
        String tenFileAnhMoi = tenFileAnhCu;

        MultipartFile fileMoi = sanPhamChiTietRequest.getHinhAnh();
        if (fileMoi != null && !fileMoi.isEmpty()) {
            tenFileAnhMoi = saveFile(fileMoi);
            if (tenFileAnhMoi != null && tenFileAnhCu != null && !tenFileAnhCu.equals(tenFileAnhMoi)) {
                deleteFile(tenFileAnhCu);
            }
        }

        sanPhamChiTietRequest.setHinhAnh(null);
        MapperUtils.mapToExisting(sanPhamChiTietRequest, sanPhamChiTiet);
        sanPhamChiTiet.setHinhAnh(tenFileAnhMoi);

        // Cập nhật các entity liên quan (dùng RuntimeException)
        sanPhamChiTiet.setSanPham(sanPhamRepository.findById(sanPhamChiTietRequest.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + sanPhamChiTietRequest.getIdSanPham()))); // <-- ĐÃ SỬA
        sanPhamChiTiet.setDungTich(dungTichRepository.findById(sanPhamChiTietRequest.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("DungTich không tìm thấy với ID: " + sanPhamChiTietRequest.getIdDungTich()))); // <-- ĐÃ SỬA
        sanPhamChiTiet.setNongDo(nongDoRepository.findById(sanPhamChiTietRequest.getIdNongDo())
                .orElseThrow(() -> new RuntimeException("NongDo không tìm thấy với ID: " + sanPhamChiTietRequest.getIdNongDo()))); // <-- ĐÃ SỬA

        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Transactional
    public void deleteSanPhamChiTiet(Long id){
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id)); // <-- ĐÃ SỬA

        String tenFileAnh = sanPhamChiTiet.getHinhAnh();
        sanPhamChiTietRepository.deleteById(id);
        deleteFile(tenFileAnh);
    }

    public SanPhamChiTietResponse getById(Long id) {
        // Sử dụng JOIN FETCH và RuntimeException
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findByIdFetchingRelationships(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id)); // <-- ĐÃ SỬA
        return new SanPhamChiTietResponse(sanPhamChiTiet);
    }
}