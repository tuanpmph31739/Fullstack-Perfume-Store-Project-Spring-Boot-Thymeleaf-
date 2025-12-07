package com.shop.fperfume.controller.client;

import com.shop.fperfume.entity.HoaDon;
import com.shop.fperfume.repository.HoaDonRepository;
import com.shop.fperfume.service.client.HoaDonClientService;
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
    private final HoaDonClientService hoaDonClientService;  // ⭐ thêm

    @GetMapping("/{ma}")
    public String viewPublicOrder(@PathVariable("ma") String ma,
                                  Model model,
                                  RedirectAttributes ra) {
        HoaDon order = hoaDonRepository.findByMa(ma)
                .orElse(null);

        if (order == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/";
        }

        model.addAttribute("order", order);
        model.addAttribute("guestView", true);
        return "client/account/order-detail";
    }

    @PostMapping("/cancel")
    public String cancelOrderPublic(
            @RequestParam("id") Integer id,
            @RequestParam("lyDo") String lyDo,
            RedirectAttributes ra
    ) {
        try {
            HoaDon order = hoaDonClientService.cancelOrderGuest(id, lyDo);

            ra.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
            return "redirect:/user/orders/" + order.getMa();

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());

            String ma = hoaDonRepository.findById(id)
                    .map(HoaDon::getMa)
                    .orElse("");
            return ma.isEmpty()
                    ? "redirect:/"
                    : "redirect:/user/orders/" + ma;
        }
    }
}
