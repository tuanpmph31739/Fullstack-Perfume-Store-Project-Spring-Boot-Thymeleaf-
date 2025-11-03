package com.shop.fperfume.service.admin;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


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

    @Transactional
    public SanPham addSanPham(SanPhamRequest sanPhamRequest) {

        // --- BƯỚC 1: KIỂM TRA TRÙNG TÊN ---
        String tenMoi = sanPhamRequest.getTenNuocHoa().trim();
        Optional<SanPham> existingSanPham = sanPhamRepository.findByTenNuocHoa(tenMoi);

        if (existingSanPham.isPresent()) {
            // Nếu tìm thấy, ném ra lỗi để Controller bắt
            throw new RuntimeException("Tên nước hoa '" + tenMoi + "' đã tồn tại!");
        }
        // ---------------------------------

        SanPham sanPham = MapperUtils.map(sanPhamRequest, SanPham.class);

        // Tìm và gán các entity liên quan
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
        sanPham.setNgayTao(LocalDateTime.now());
        sanPham.setNgaySua(LocalDateTime.now());

        return sanPhamRepository.save(sanPham);
    }

    /**
     * Sửa lại hàm Cập nhật, đổi tên và thêm logic kiểm tra trùng tên
     */
    @Transactional
    public SanPham updateSanPham(Long id, SanPhamRequest sanPhamRequest) {

        // --- BƯỚC 1: KIỂM TRA TRÙNG TÊN KHI CẬP NHẬT ---
        String tenMoi = sanPhamRequest.getTenNuocHoa().trim();
        Optional<SanPham> existingSanPham = sanPhamRepository.findByTenNuocHoa(tenMoi);

        // Nếu tìm thấy tên VÀ ID của tên đó không giống ID đang sửa -> Báo lỗi
        if (existingSanPham.isPresent() && !existingSanPham.get().getId().equals(id)) {
            throw new RuntimeException("Tên nước hoa '" + tenMoi + "' đã tồn tại!");
        }
        // -------------------------------------------

        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Cập nhật các trường cơ bản
        sanPham.setTenNuocHoa(sanPhamRequest.getTenNuocHoa());
        sanPham.setMoTa(sanPhamRequest.getMoTa());

        // Cập nhật các entity liên quan
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
        sanPham.setNgaySua(LocalDateTime.now());

        return sanPhamRepository.save(sanPham);
    }


    public void deleteSanPham(Long id){
        sanPhamRepository.deleteById(id);
    }

    public SanPhamResponse getById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return new SanPhamResponse(sanPham);
    }

}
