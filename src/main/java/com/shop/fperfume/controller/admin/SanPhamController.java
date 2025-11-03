package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.service.admin.*;
import com.shop.fperfume.util.MapperUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private LoaiNuocHoaService loaiNuocHoaService;

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private XuatXuService xuatXuService;

    @Autowired
    private MuaThichHopService muaThichHopService;

    @Autowired
    private NhomHuongService nhomHuongService;

    private final int PAGE_SIZE = 15;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo) {

        // ✅ Đảm bảo pageNo luôn >= 1 để tránh lỗi "Page index must not be less than zero"
        if (pageNo < 1) {
            pageNo = 1;
        }

        PageableObject<SanPhamResponse> pageObject = sanPhamService.paging(pageNo, PAGE_SIZE);
        model.addAttribute("page", pageObject);
        model.addAttribute("page.metaDataAvailable", true);
        model.addAttribute("page.size", PAGE_SIZE);
        model.addAttribute("currentPath", "/admin/san-pham");
        return "admin/san_pham/index";
    }



    @GetMapping("/add")
    public String viewAdd(Model model) {
        model.addAttribute("sanPhamRequest", new SanPhamRequest());
        model.addAttribute("currentPath", "/admin/san-pham");
        loadBaseProductDropdownData(model);
        return "admin/san_pham/add";
    }


    @PostMapping("/save")
    public String add(@Valid @ModelAttribute("sanPhamRequest") SanPhamRequest request,
                      BindingResult bindingResult, // <-- Thêm BindingResult
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Dữ liệu không hợp lệ, vui lòng kiểm tra lại.");
            loadBaseProductDropdownData(model);
            model.addAttribute("currentPath", "/admin/san-pham");
            return "admin/san_pham/add"; // Trả về form
        }

        try {
            sanPhamService.addSanPham(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm gốc thành công!");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Tên nước hoa")) {
                bindingResult.rejectValue("tenNuocHoa", "error.tenNuocHoa", e.getMessage());
            } else {
                model.addAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            }
            loadBaseProductDropdownData(model);
            model.addAttribute("currentPath", "/admin/san-pham");
            return "admin/san_pham/add";
        }

        return "redirect:/admin/san-pham";
    }


    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy Entity (Cần SanPhamService có hàm findEntityById)
            SanPhamResponse entity = sanPhamService.getById(id);
            // 2. Map Entity sang Request DTO (Cần SanPhamService có hàm mapEntityToRequest)
            SanPhamRequest requestDto = MapperUtils.map(entity, SanPhamRequest.class);
            model.addAttribute("sanPhamRequest", requestDto);
            model.addAttribute("currentPath", "/admin/san-pham");
            loadBaseProductDropdownData(model);
            return "admin/san_pham/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/san-pham";
        }
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("sanPhamRequest") SanPhamRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {


        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Dữ liệu không hợp lệ, vui lòng kiểm tra lại.");
            loadBaseProductDropdownData(model);
            model.addAttribute("currentPath", "/admin/san-pham");
            return "admin/san_pham/edit";
        }

        try {
            sanPhamService.updateSanPham(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm gốc thành công!");
        } catch (RuntimeException e) {
            // Kiểm tra có phải lỗi trùng tên không
            if (e.getMessage() != null && e.getMessage().contains("Tên nước hoa")) {
                // Gán lỗi vào trường tenNuocHoa
                bindingResult.rejectValue("tenNuocHoa", "error.tenNuocHoa", e.getMessage());
            } else {
                // Lỗi chung khác
                model.addAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            }

            // Load lại dropdown và trả về form edit
            loadBaseProductDropdownData(model);
            model.addAttribute("currentPath", "/admin/san-pham");
            return "admin/san_pham/edit";
        }

        return "redirect:/admin/san-pham";
    }

    // Trong SanPhamController.java

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        // Khối try-catch này là BẮT BUỘC
        try {
            SanPhamResponse sanPham = sanPhamService.getById(id); // Nếu ID sai, hàm này sẽ ném Exception
            List<SanPhamChiTietResponse> listChiTiet = sanPhamChiTietService.getAllChiTietBySanPhamId(id);

            model.addAttribute("sanPham", sanPham); // Chỉ chạy nếu tìm thấy
            model.addAttribute("listChiTiet", listChiTiet);
            model.addAttribute("currentPath", "/admin/san-pham");

            return "admin/san_pham/view"; // Trả về view

        } catch (Exception e) {
            // Nếu không tìm thấy, Exception bị bắt và chuyển hướng về trang index
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/san-pham"; // Chuyển hướng
        }
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        sanPhamService.deleteSanPham(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");

        return "redirect:/admin/san-pham";
    }

    private void loadBaseProductDropdownData(Model model) {
        model.addAttribute("listLoaiNuocHoa", loaiNuocHoaService.getLoaiNuocHoa());
        model.addAttribute("listThuongHieu", thuongHieuService.getThuongHieu());
        model.addAttribute("listXuatXu", xuatXuService.getAllXuatXu());
        model.addAttribute("listMuaThichHop", muaThichHopService.getMuaThichHop());
        model.addAttribute("listNhomHuong", nhomHuongService.getNhomHuong());
    }

}