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
    private NhomHuongRepository nhomHuongRepository;

    @Autowired
    private LoaiNuocHoaRepository loaiNuocHoaRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private XuatXuRepository xuatXuRepository;

    @Autowired
    private MuaThichHopRepository muaThichHopRepository;



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

    public void addSanPham(SanPhamRequest sanPhamRequest){

        SanPham sanPham = MapperUtils.map(sanPhamRequest, SanPham.class);

        ThuongHieu thuongHieu = thuongHieuRepository.findById(sanPhamRequest.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + sanPhamRequest.getIdThuongHieu()));
        XuatXu xuatXu = xuatXuRepository.findById(sanPhamRequest.getIdXuatXu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với ID: " + sanPhamRequest.getIdXuatXu()));
        LoaiNuocHoa loaiNuocHoa = loaiNuocHoaRepository.findById(sanPhamRequest.getIdLoaiNuocHoa())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + sanPhamRequest.getIdLoaiNuocHoa()));
        MuaThichHop muaThichHop = muaThichHopRepository.findById(sanPhamRequest.getIdMuaThichHop())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mùa thích hợp với ID: " + sanPhamRequest.getIdMuaThichHop()));
        NhomHuong nhomHuong = nhomHuongRepository.findById(sanPhamRequest.getIdNhomHuong())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm hương với ID: " + sanPhamRequest.getIdNhomHuong()));

        sanPham.setThuongHieu(thuongHieu);
        sanPham.setXuatXu(xuatXu);
        sanPham.setLoaiNuocHoa(loaiNuocHoa);
        sanPham.setMuaThichHop(muaThichHop);
        sanPham.setNhomHuong(nhomHuong);


        sanPhamRepository.save(sanPham);
    }

    @Transactional
    public void updateSanPham(Long id, SanPhamRequest sanPhamRequest){
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        sanPham.setThuongHieu(thuongHieuRepository.findById(sanPhamRequest.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + sanPhamRequest.getIdThuongHieu())));
        sanPham.setXuatXu(xuatXuRepository.findById(sanPhamRequest.getIdXuatXu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với ID: " + sanPhamRequest.getIdXuatXu())));
        sanPham.setLoaiNuocHoa(loaiNuocHoaRepository.findById(sanPhamRequest.getIdLoaiNuocHoa())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại nước hoa với ID: " + sanPhamRequest.getIdLoaiNuocHoa())));
        sanPham.setMuaThichHop(muaThichHopRepository.findById(sanPhamRequest.getIdMuaThichHop())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mùa thích hợp với ID: " + sanPhamRequest.getIdMuaThichHop())));
        sanPham.setNhomHuong(nhomHuongRepository.findById(sanPhamRequest.getIdNhomHuong())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm hương với ID: " + sanPhamRequest.getIdNhomHuong())));

        sanPham.setTenNuocHoa(sanPhamRequest.getTenNuocHoa());
        sanPham.setMoTa(sanPhamRequest.getMoTa());
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

    /**
     * Hàm tiện ích map Entity sang Request DTO (Giữ nguyên)
     */
    private SanPhamRequest mapEntityToRequest(SanPham entity) {
        SanPhamRequest dto = new SanPhamRequest();
        dto.setId(entity.getId());
        dto.setTenNuocHoa(entity.getTenNuocHoa());
        dto.setMoTa(entity.getMoTa());
        if (entity.getThuongHieu() != null) dto.setIdThuongHieu(entity.getThuongHieu().getId());
        if (entity.getXuatXu() != null) dto.setIdXuatXu(entity.getXuatXu().getId());
        if (entity.getLoaiNuocHoa() != null) dto.setIdLoaiNuocHoa(entity.getLoaiNuocHoa().getId());
        if (entity.getMuaThichHop() != null) dto.setIdMuaThichHop(entity.getMuaThichHop().getId());
        if (entity.getNhomHuong() != null) dto.setIdNhomHuong(entity.getNhomHuong().getId());
        return dto;
    }

//    // Bạn cần đảm bảo phương thức này tồn tại trong SanPhamService
//    // và nó trả về entity SanPham
//    public SanPham findEntityById(Long id) {
//        return sanPhamService.findEntityById(id);
//    }

}
