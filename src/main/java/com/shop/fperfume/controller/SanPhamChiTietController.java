//package com.shop.fperfume.controller; // Đảm bảo đúng package
//
//import com.shop.fperfume.entity.SanPhamChiTiet; // Cần entity để map sang request khi edit
//import com.shop.fperfume.model.request.SanPhamChiTietRequest;
//import com.shop.fperfume.model.response.PageableObject;
//import com.shop.fperfume.model.response.SanPhamChiTietResponse;
//import com.shop.fperfume.service.DungTichService; // Service cho DungTich
//import com.shop.fperfume.service.NongDoService; // Service cho NongDo
//import com.shop.fperfume.service.SanPhamChiTietService;
//import com.shop.fperfume.service.SanPhamService; // Service cho SanPham (để lấy list SP gốc)
//import jakarta.validation.Valid; // Để dùng @Valid
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequestMapping("/admin/san-pham-chi-tiet") // Base URL cho controller này
//public class SanPhamChiTietController {
//
//    @Autowired
//    private SanPhamChiTietService sanPhamChiTietService;
//
//    // Inject các service cần thiết khác để lấy list cho dropdown
//    @Autowired
//    private SanPhamService sanPhamService;
//
//    @Autowired
//    private DungTichService dungTichService;
//
//    @Autowired
//    private NongDoService nongDoService;
//
//    private final int PAGE_SIZE = 10; // Số lượng item mỗi trang
//
//    /**
//     * Hiển thị danh sách chi tiết sản phẩm (phân trang)
//     */
//    @GetMapping
//    public String index(Model model,
//                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {
//        PageableObject<SanPhamChiTietResponse> page = sanPhamChiTietService.paging(pageNo, PAGE_SIZE);
//        model.addAttribute("page", page);
//        // Thêm các thuộc tính cần thiết khác cho PageableObject nếu nó không tự có
//        // Ví dụ: kiểm tra xem metadata có sẵn không
//        model.addAttribute("page.metaDataAvailable", true); // Hoặc false tùy thuộc vào PageableObject của bạn
//        model.addAttribute("page.size", PAGE_SIZE);
//
//        return "admin/san_pham_chi_tiet/index"; // Trỏ đến file index.html mới
//    }
//
//    /**
//     * Hiển thị form thêm mới chi tiết sản phẩm
//     */
//    @GetMapping("/add")
//    public String viewAdd(Model model) {
//        model.addAttribute("sanPhamChiTietRequest", new SanPhamChiTietRequest());
//        // Load danh sách cần thiết cho dropdowns
//        loadDropdownData(model);
//        return "admin/san_pham_chi_tiet/add"; // Trỏ đến file add.html
//    }
//
//    /**
//     * Xử lý thêm mới chi tiết sản phẩm
//     */
//    @PostMapping("/save")
//    public String add(@Valid @ModelAttribute("sanPhamChiTietRequest") SanPhamChiTietRequest request,
//                      BindingResult bindingResult, // Để bắt lỗi validation
//                      RedirectAttributes redirectAttributes,
//                      Model model) {
//
//        // Kiểm tra lỗi validation
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ!");
//            // Load lại dropdown data khi có lỗi
//            loadDropdownData(model);
//            return "admin/san_pham_chi_tiet/add"; // Trả về form add với lỗi
//        }
//
//        try {
//            sanPhamChiTietService.addSanPhamChiTiet(request);
//            redirectAttributes.addFlashAttribute("successMessage", "Thêm chi tiết sản phẩm thành công!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm chi tiết sản phẩm: " + e.getMessage());
//            // Load lại dropdown data khi có lỗi
//            loadDropdownData(model);
//            model.addAttribute("sanPhamChiTietRequest", request); // Giữ lại dữ liệu đã nhập
//            return "admin/san_pham_chi_tiet/add"; // Trả về form add với lỗi
//        }
//
//        return "redirect:/admin/san-pham-chi-tiet"; // Chuyển hướng về trang danh sách
//    }
//
//    /**
//     * Hiển thị form chỉnh sửa chi tiết sản phẩm
//     */
//    @GetMapping("/edit/{id}")
//    public String viewEdit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            // Lấy entity để map sang request (an toàn hơn là dùng response)
//            // Bạn cần thêm phương thức findEntityById vào service
//            SanPhamChiTiet entity = sanPhamChiTietService.findEntityById(id);
//            SanPhamChiTietRequest requestDto = mapEntityToRequest(entity); // Hàm map thủ công
//
//            model.addAttribute("sanPhamChiTietRequest", requestDto);
//            // Load danh sách cần thiết cho dropdowns
//            loadDropdownData(model);
//            return "admin/san_pham_chi_tiet/edit"; // Trỏ đến file edit.html
//        } catch (Exception e) { // Hoặc bắt ResourceNotFoundException cụ thể
//            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm!");
//            return "redirect:/admin/san-pham-chi-tiet";
//        }
//    }
//
//    /**
//     * Xử lý cập nhật chi tiết sản phẩm
//     */
//    @PostMapping("/update/{id}")
//    public String update(@PathVariable("id") Long id,
//                         @Valid @ModelAttribute("sanPhamChiTietRequest") SanPhamChiTietRequest request,
//                         BindingResult bindingResult,
//                         RedirectAttributes redirectAttributes,
//                         Model model) {
//
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ!");
//            // Load lại dropdown data khi có lỗi
//            loadDropdownData(model);
//            // Giữ lại ID trong path variable khi trả về form
//            request.setId(id); // Đảm bảo ID được giữ lại nếu cần hiển thị trên form
//            model.addAttribute("sanPhamChiTietRequest", request);
//            return "admin/san_pham_chi_tiet/edit"; // Trả về form edit với lỗi
//        }
//
//        try {
//            sanPhamChiTietService.updateSanPhamChiTiet(id, request);
//            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi tiết sản phẩm thành công!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật chi tiết sản phẩm: " + e.getMessage());
//            // Load lại dropdown data khi có lỗi
//            loadDropdownData(model);
//            request.setId(id); // Giữ lại ID
//            model.addAttribute("sanPhamChiTietRequest", request); // Giữ lại dữ liệu đã nhập
//            return "admin/san_pham_chi_tiet/edit"; // Trả về form edit với lỗi
//        }
//
//        return "redirect:/admin/san-pham-chi-tiet";
//    }
//
//    /**
//     * Xử lý xóa chi tiết sản phẩm
//     */
//    @GetMapping("/delete/{id}")
//    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        try {
//            sanPhamChiTietService.deleteSanPhamChiTiet(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Xóa chi tiết sản phẩm thành công!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa chi tiết sản phẩm: " + e.getMessage());
//        }
//        return "redirect:/admin/san-pham-chi-tiet";
//    }
//
//    /**
//     * Hàm tiện ích để load dữ liệu cho các dropdown trong form add/edit
//     */
//    private void loadDropdownData(Model model) {
//        // Lấy danh sách sản phẩm gốc (chỉ cần ID và Tên là đủ)
//        // Bạn nên tạo một phương thức trong SanPhamService chỉ trả về list ID+Tên
//        model.addAttribute("listSanPham", sanPhamService.getAllSanPhamBaseInfo()); // Giả sử có hàm này
//
//        // Lấy danh sách dung tích
//        model.addAttribute("listDungTich", dungTichService.getAll()); // Giả sử service có hàm này
//
//        // Lấy danh sách nồng độ
//        model.addAttribute("listNongDo", nongDoService.getAll()); // Giả sử service có hàm này
//    }
//
//    /**
//     * Hàm tiện ích để map từ Entity sang Request DTO cho form edit
//     */
////    private SanPhamChiTietRequest mapEntityToRequest(SanPhamChiTiet entity) {
////        SanPhamChiTietRequest dto = new SanPhamChiTietRequest();
////        dto.setId(entity.getId());
////        dto.setMaSKU(entity.getMaSKU());
////        dto.setSoLuongTon(entity.getSoLuongTon());
////        dto.setGiaNhap(entity.getGiaNhap());
////        dto.setGiaBan(entity.getGiaBan());
////        // Không set hinhAnh (MultipartFile) ở đây, chỉ hiển thị ảnh cũ trên view
////        dto.setTrangThai(entity.getTrangThai());
////        if(entity.getSanPham() != null) {
////            dto.setIdSanPham(entity.getSanPham().getId());
////        }
////        if(entity.getDungTich() != null) {
////            dto.setIdDungTich(entity.getDungTich().getId());
////        }
////        if(entity.getNongDo() != null) {
////            dto.setIdNongDo(entity.getNongDo().getId());
////        }
////        return dto;
////    }
//
//
//}