package com.shop.fperfume.controller.admin;

import com.shop.fperfume.model.request.SanPhamRequest;
import com.shop.fperfume.model.response.PageableObject;
import com.shop.fperfume.model.response.SanPhamChiTietResponse;
import com.shop.fperfume.model.response.SanPhamResponse;
import com.shop.fperfume.service.admin.*;
import com.shop.fperfume.util.MapperUtils;
import jakarta.servlet.http.HttpServletRequest;
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

    private final int PAGE_SIZE = 12;


    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "loaiId", required = false) Long loaiId,
                        @RequestParam(name = "thuongHieuId", required = false) Long thuongHieuId,
                        @RequestParam(name = "nhomHuongId", required = false) Long nhomHuongId,
                        @RequestParam(name = "muaId", required = false) Long muaId) {

        if (pageNo < 1) {
            pageNo = 1;
        }

        PageableObject<SanPhamResponse> pageObject = sanPhamService.paging(
                pageNo, PAGE_SIZE,
                keyword,
                loaiId,
                thuongHieuId,
                nhomHuongId,
                muaId
        );

        model.addAttribute("page", pageObject);
        model.addAttribute("page.metaDataAvailable", true);
        model.addAttribute("page.size", PAGE_SIZE);

        // giữ lại giá trị filter để đổ lại lên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiId", loaiId);
        model.addAttribute("thuongHieuId", thuongHieuId);
        model.addAttribute("nhomHuongId", nhomHuongId);
        model.addAttribute("muaId", muaId);

        // cần dropdown cho form lọc
        loadBaseProductDropdownData(model);

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
    public String viewEdit(@PathVariable("id") Integer id,
                           Model model,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            SanPhamResponse entity = sanPhamService.getById(id);

            SanPhamRequest requestDto = MapperUtils.map(entity, SanPhamRequest.class);
            model.addAttribute("sanPhamRequest", requestDto);
            loadBaseProductDropdownData(model);


            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", referer);

            model.addAttribute("currentPath", "/admin/san-pham");
            return "admin/san_pham/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dòng sản phẩm!");
            return "redirect:/admin/san-pham";
        }
    }



    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("sanPhamRequest") SanPhamRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "backUrl", required = false) String backUrl,
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
            model.addAttribute("backUrl", backUrl);
            return "admin/san_pham/edit";
        }
        if (backUrl != null && !backUrl.isBlank()) {
            return "redirect:" + backUrl;
        }

        return "redirect:/admin/san-pham";
    }

    // Trong SanPhamController.java

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Integer id,
                       @RequestParam(value = "backUrl", required = false) String backUrl,
                       Model model,
                       HttpServletRequest request,
                       RedirectAttributes redirectAttributes) {
        try {
            SanPhamResponse sanPham = sanPhamService.getById(id);
            List<SanPhamChiTietResponse> listChiTiet =
                    sanPhamChiTietService.getAllChiTietBySanPhamId(id);

            model.addAttribute("sanPham", sanPham);
            model.addAttribute("listChiTiet", listChiTiet);
            model.addAttribute("currentPath", "/admin/san-pham");

            // ƯU TIÊN backUrl từ query param
            if (backUrl == null || backUrl.isBlank()) {
                backUrl = request.getHeader("Referer");
            }
            model.addAttribute("backUrl", backUrl);

            return "admin/san_pham/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dòng sản phẩm!");
            return "redirect:/admin/san-pham";
        }
    }



    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.deleteSanPham(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa dòng sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }

        // Lấy URL trang trước (có cả page & filter)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/san-pham");
    }


    private void loadBaseProductDropdownData(Model model) {
        model.addAttribute("listLoaiNuocHoa", loaiNuocHoaService.getLoaiNuocHoa());
        model.addAttribute("listThuongHieu", thuongHieuService.getThuongHieu());
        model.addAttribute("listXuatXu", xuatXuService.getAllXuatXu());
        model.addAttribute("listMuaThichHop", muaThichHopService.getMuaThichHop());
        model.addAttribute("listNhomHuong", nhomHuongService.getNhomHuong());
    }

}