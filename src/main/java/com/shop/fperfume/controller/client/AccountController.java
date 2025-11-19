package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.entity.NguoiDung;
import com.shop.fperfume.security.CustomUserDetails;
import com.shop.fperfume.service.client.HoaDonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private HoaDonClientService hoaDonService;

    // 1. Xem danh sách đơn hàng (Kèm Tìm kiếm & Lọc)
    @GetMapping("/orders")
    public String orderHistory(Model model,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "fromDate", required = false) String fromDate,
                               @RequestParam(value = "toDate", required = false) String toDate,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return "redirect:/login";

        NguoiDung user = userDetails.getUser();
        List<HoaDon> orders = hoaDonService.getOrdersByUser(user, keyword, fromDate, toDate);

        model.addAttribute("orders", orders);
        // Gửi lại giá trị để điền vào ô input sau khi reload trang
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "client/account/order-history";
    }

    // 2. Xem chi tiết đơn hàng
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Integer id,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            HoaDon order = hoaDonService.getOrderDetailForUser(id, userDetails.getUser());
            model.addAttribute("order", order);
            return "client/account/order-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/orders";
        }
    }

    // 3. Xử lý hủy đơn hàng
    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam("id") Integer id,
                              @RequestParam("lyDo") String lyDo,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            hoaDonService.cancelOrder(id, userDetails.getUser(), lyDo);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/account/orders/" + id;
    }
}