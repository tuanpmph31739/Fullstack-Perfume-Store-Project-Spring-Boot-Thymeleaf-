package com.shop.fperfume.service;

import com.shop.fperfume.entity.*;
import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.repository.*;
import com.shop.fperfume.util.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // <-- THÊM IMPORT
import java.io.InputStream; // <-- THÊM IMPORT
import java.nio.file.Files; // <-- THÊM IMPORT
import java.nio.file.Path; // <-- THÊM IMPORT
import java.nio.file.Paths; // <-- THÊM IMPORT
import java.nio.file.StandardCopyOption; // <-- THÊM IMPORT
import java.util.List;
import java.util.UUID; // <-- THÊM IMPORT

import java.util.List;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DungTichRepository dungTichRepository;

    @Autowired
    private LoaiNuocHoaRepository loaiNuocHoaRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private XuatXuRepository xuatXuRepository;

    @Autowired
    private MuaThichHopRepository muaThichHopRepository;

    // 1. Định nghĩa đường dẫn thư mục để lưu file upload
    private final Path uploadDir = Paths.get("src", "main", "resources", "static", "images", "uploads");

    // 2. Thêm phương thức trợ giúp để lưu file
    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null; // Không có file để lưu
        }

        try {
            // Đảm bảo thư mục upload tồn tại
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Tạo tên file duy nhất để tránh bị ghi đè
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // Đường dẫn đầy đủ đến file mới
            Path destinationFile = this.uploadDir.resolve(uniqueFilename);

            // Copy file vào thư mục đích
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename; // Trả về tên file đã lưu (để lưu vào DB)

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + file.getOriginalFilename(), e);
        }
    }

    public List<SanPhamResponse>getAllSanPham(){
        return sanPhamRepository.findAll()
                .stream()
                .map(SanPhamResponse::new)
                .toList();
    }

    public PageableObject<SanPhamResponse>paging(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo-1, pageSize);
        Page<SanPham>page = sanPhamRepository.findAll(pageable);
        Page<SanPhamResponse>responses = page.map(SanPhamResponse::new);
        return new PageableObject<>(responses);
    }

    // ... (Giữ nguyên các @Autowired và phương thức saveFile)

    @Transactional // Thêm @Transactional để đảm bảo nhất quán
    public void addSanPham(SanPhamRequest sanPhamRequest) {

        // 1. Lưu file ảnh trước và lấy ra tên file (String)
        String tenFileAnh = saveFile(sanPhamRequest.getHinhAnh());

        // 2. TẠM THỜI set trường hinhAnh trong request về null
        //    để MapperUtils không cố map đối tượng File sang String
        sanPhamRequest.setHinhAnh(null);

        // 3. Dùng MapperUtils để map tất cả các trường CÒN LẠI
        SanPham sanPham = MapperUtils.map(sanPhamRequest, SanPham.class);

        // 4. Set tên file ảnh đã lưu vào entity
        sanPham.setHinhAnh(tenFileAnh);

        // 5. Tìm và set các entity liên quan
        DungTich dungTich = dungTichRepository.findById(sanPhamRequest.getIdDungTich()).orElseThrow(() -> new RuntimeException("Không tìm thấy dung tích với ID: " + sanPhamRequest.getIdDungTich()));
        LoaiNuocHoa loaiNuocHoa = loaiNuocHoaRepository.findById(sanPhamRequest.getIdLoaiNuocHoa()).orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + sanPhamRequest.getIdLoaiNuocHoa()));
        ThuongHieu thuongHieu = thuongHieuRepository.findById(sanPhamRequest.getIdThuongHieu()).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + sanPhamRequest.getIdThuongHieu()));
        XuatXu xuatXu = xuatXuRepository.findById(sanPhamRequest.getIdXuatXu()).orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với ID: " + sanPhamRequest.getIdXuatXu()));
        MuaThichHop muaThichHop = muaThichHopRepository.findById(sanPhamRequest.getIdMuaThichHop()).orElseThrow(() -> new RuntimeException("Không tìm thấy mùa thích hợp với ID: " + sanPhamRequest.getIdMuaThichHop()));


        sanPham.setDungTich(dungTich);
        sanPham.setLoaiNuocHoa(loaiNuocHoa);
        sanPham.setThuongHieu(thuongHieu);
        sanPham.setXuatXu(xuatXu);
        sanPham.setMuaThichHop(muaThichHop);

        sanPhamRepository.save(sanPham);
    }

    @Transactional
    public void updateSanPham(Long id, SanPhamRequest sanPhamRequest) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // 1. Xử lý file ảnh
        String tenFileAnh = sanPham.getHinhAnh(); // Giữ lại tên file cũ

        if (sanPhamRequest.getHinhAnh() != null && !sanPhamRequest.getHinhAnh().isEmpty()) {
            // Nếu có file mới được tải lên, lưu file mới
            tenFileAnh = saveFile(sanPhamRequest.getHinhAnh());
        }

        // 2. TẠM THỜI set hinhAnh trong request về null
        //    để MapperUtils không cố map đè lên trường String cũ
        sanPhamRequest.setHinhAnh(null);

        // 3. Dùng MapperUtils để map các trường còn lại
        MapperUtils.mapToExisting(sanPhamRequest, sanPham);

        // 4. Set tên file (mới hoặc cũ) vào entity
        sanPham.setHinhAnh(tenFileAnh);

        // 5. Cập nhật các entity liên quan
        sanPham.setDungTich(dungTichRepository.findById(sanPhamRequest.getIdDungTich())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dung tích với ID: " + sanPhamRequest.getIdDungTich())));
        sanPham.setLoaiNuocHoa(loaiNuocHoaRepository.findById(sanPhamRequest.getIdLoaiNuocHoa())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + sanPhamRequest.getIdLoaiNuocHoa())));
        sanPham.setThuongHieu(thuongHieuRepository.findById(sanPhamRequest.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + sanPhamRequest.getIdThuongHieu())));
        sanPham.setXuatXu(xuatXuRepository.findById(sanPhamRequest.getIdXuatXu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với ID: " + sanPhamRequest.getIdXuatXu())));
        sanPham.setMuaThichHop(muaThichHopRepository.findById(sanPhamRequest.getIdMuaThichHop())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mùa thích hợp với ID: " + sanPhamRequest.getIdMuaThichHop())));


        sanPhamRepository.save(sanPham);
    }

    public void deleteSanPham(Long id){
        sanPhamRepository.deleteById(id);
    }

    public SanPhamResponse getById(Long id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return new SanPhamResponse(sanPham);
    }


}
