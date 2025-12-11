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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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


    public List<SanPhamChiTietResponse> getAllSanPhamChiTiet(){
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
     */
    public PageableObject<SanPhamChiTietResponse> getPage(Integer pageNo,
                                                          Integer pageSize,
                                                          String keyword,
                                                          Integer dungTichId,
                                                          Integer nongDoId,
                                                          String trangThai,   // <- String
                                                          String sort) {

        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }

        // Chuẩn hoá keyword
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) keyword = null;
        }

        // Chuẩn hoá trangThai filter
        if (trangThai != null) {
            trangThai = trangThai.trim();
            if (trangThai.isEmpty()) trangThai = null;
        }

        // Xử lý sort
        Sort sortSpec;
        if ("priceAsc".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.ASC, "giaBan");
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.DESC, "giaBan");
        } else {
            // Mặc định: mới nhất trước (id giảm dần)
            sortSpec = Sort.by(Sort.Direction.DESC, "id");
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortSpec);

        Page<SanPhamChiTiet> pageEntity =
                sanPhamChiTietRepository.searchSanPhamChiTiet(
                        keyword,
                        dungTichId,
                        nongDoId,
                        trangThai,   // <- truyền String xuống repo
                        pageable
                );

        Page<SanPhamChiTietResponse> pageResponse = pageEntity.map(SanPhamChiTietResponse::new);

        return new PageableObject<>(pageResponse);
    }

    @Transactional
    public void addSanPhamChiTiet(SanPhamChiTietRequest sanPhamChiTietRequest) {

        Optional<SanPhamChiTiet> existingSku = sanPhamChiTietRepository.findByMaSKU(sanPhamChiTietRequest.getMaSKU().trim());

        if (existingSku.isPresent()) {
            throw new RuntimeException("Mã SKU '" + sanPhamChiTietRequest.getMaSKU() + "' đã tồn tại!");
        }

        String tenFileAnh = saveFile(sanPhamChiTietRequest.getHinhAnh());
        sanPhamChiTietRequest.setHinhAnh(null);

        SanPhamChiTiet sanPhamChiTiet = MapperUtils.map(sanPhamChiTietRequest, SanPhamChiTiet.class);
        sanPhamChiTiet.setHinhAnh(tenFileAnh);

        SanPham sanPham = sanPhamRepository.findById(sanPhamChiTietRequest.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + sanPhamChiTietRequest.getIdSanPham()));
        DungTich dungTich = dungTichRepository.findById(sanPhamChiTietRequest.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("DungTich không tìm thấy với ID: " + sanPhamChiTietRequest.getIdDungTich()));
        NongDo nongDo = nongDoRepository.findById(sanPhamChiTietRequest.getIdNongDo())
                .orElseThrow(() -> new RuntimeException("NongDo không tìm thấy với ID: " + sanPhamChiTietRequest.getIdNongDo()));

        sanPhamChiTiet.setSanPham(sanPham);
        sanPhamChiTiet.setDungTich(dungTich);
        sanPhamChiTiet.setNongDo(nongDo);
        sanPhamChiTiet.setNgayTao(LocalDateTime.now());
        sanPhamChiTiet.setNgaySua(LocalDateTime.now());

        sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Transactional
    public void updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest sanPhamChiTietRequest) {
        Optional<SanPhamChiTiet> existingSku = sanPhamChiTietRepository.findByMaSKU(sanPhamChiTietRequest.getMaSKU().trim());
        if (existingSku.isPresent() && !existingSku.get().getId().equals(id)) {
            throw new RuntimeException("Mã SKU '" + sanPhamChiTietRequest.getMaSKU() + "' đã tồn tại!");
        }

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));

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

        sanPhamChiTiet.setSanPham(sanPhamRepository.findById(sanPhamChiTietRequest.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("SanPham không tìm thấy với ID: " + sanPhamChiTietRequest.getIdSanPham())));
        sanPhamChiTiet.setDungTich(dungTichRepository.findById(sanPhamChiTietRequest.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("DungTich không tìm thấy với ID: " + sanPhamChiTietRequest.getIdDungTich())));
        sanPhamChiTiet.setNongDo(nongDoRepository.findById(sanPhamChiTietRequest.getIdNongDo())
                .orElseThrow(() -> new RuntimeException("NongDo không tìm thấy với ID: " + sanPhamChiTietRequest.getIdNongDo())));

        sanPhamChiTiet.setMaSKU(sanPhamChiTietRequest.getMaSKU());
        sanPhamChiTiet.setSoLuongTon(sanPhamChiTietRequest.getSoLuongTon());
        sanPhamChiTiet.setGiaNhap(sanPhamChiTietRequest.getGiaNhap());
        sanPhamChiTiet.setGiaBan(sanPhamChiTietRequest.getGiaBan());
        sanPhamChiTiet.setTrangThai(sanPhamChiTietRequest.getTrangThai());
        sanPhamChiTiet.setNgaySua(LocalDateTime.now());

        sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Transactional
    public void deleteSanPhamChiTiet(Integer id){
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));

        String tenFileAnh = sanPhamChiTiet.getHinhAnh();
        sanPhamChiTietRepository.deleteById(id);
        deleteFile(tenFileAnh);
    }

    public SanPhamChiTietResponse getById(Integer id) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findByIdFetchingRelationships(id)
                .orElseThrow(() -> new RuntimeException("SanPhamChiTiet không tìm thấy với ID: " + id));
        return new SanPhamChiTietResponse(sanPhamChiTiet);
    }

    public List<SanPhamChiTietResponse> getAllChiTietBySanPhamId(Integer sanPhamId) {
        // 1. Gọi phương thức repository đã tạo (đã bao gồm JOIN FETCH)
        List<SanPhamChiTiet> listChiTietEntities = sanPhamChiTietRepository.findBySanPhamIdFetchingRelationships(sanPhamId);

        // 2. Map (chuyển đổi) từ List<Entity> sang List<ResponseDTO>
        return listChiTietEntities.stream()
                .map(SanPhamChiTietResponse::new) // Dùng constructor của SanPhamChiTietResponse
                .collect(Collectors.toList()); // Hoặc .toList() nếu dùng Java 16+
    }
}