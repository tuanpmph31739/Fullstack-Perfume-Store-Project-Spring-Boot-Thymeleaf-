package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class PublicOrderController {

    private final HoaDonRepository hoaDonRepository;

    @GetMapping("/{ma}")
    public String viewPublicOrder(@PathVariable("ma") String ma,
                                  Model model,
                                  RedirectAttributes ra) {
        HoaDon order = hoaDonRepository.findByMa(ma)
                .orElse(null);

        if (order == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/"; // hoặc redirect tới trang nào bạn muốn
        }

        model.addAttribute("order", order);
        model.addAttribute("guestView", true);
        return "client/account/order-detail"; // chính là template bạn vừa gửi
    }

    @PostMapping("/cancel")
    public String cancelOrderPublic(
            @RequestParam("id") Integer id,
            @RequestParam("lyDo") String lyDo,
            RedirectAttributes ra
    ) {

        HoaDon order = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Chỉ cho hủy nếu trạng thái còn cho phép
        if (!"CHO_XAC_NHAN".equals(order.getTrangThai())
                && !"DANG_CHO_THANH_TOAN".equals(order.getTrangThai())) {

            ra.addFlashAttribute("errorMessage",
                    "Đơn hàng không thể hủy vì đã được xử lý. Vui lòng liên hệ shop để được hỗ trợ.");
            return "redirect:/user/orders/" + order.getMa();
        }

        // Ghi lại lý do hủy (tùy field trong entity của bạn)
        // Nếu entity có field lyDoHuy:
        // order.setLyDoHuy(lyDo);
        // Nếu không có, tạm ghi vào ghiChu:
        String oldNote = order.getGhiChu() != null ? order.getGhiChu() + " | " : "";
        order.setGhiChu(oldNote + "Khách hủy đơn: " + lyDo);

        order.setTrangThai("DA_HUY");
        hoaDonRepository.save(order);

        ra.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
        return "redirect:/user/orders/" + order.getMa();
    }
}
