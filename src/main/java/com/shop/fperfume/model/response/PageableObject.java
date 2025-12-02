package com.shop.fperfume.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PageableObject<T> {

    private List<T> data;       // Danh sách dữ liệu
    private Integer totalPage;  // Tổng số trang
    private Integer currentPage; // Trang hiện tại (đếm từ 1)
    private Integer size;       // Số phần tử mỗi trang

    public PageableObject(Page<T> page) {
        this.data = page.getContent();
        this.totalPage = page.getTotalPages();
        this.currentPage = page.getNumber() + 1; // đếm từ 1
        this.size = page.getSize();
    }
}
