package com.shop.fperfume.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter

public class PageableObject<T> {

    private List<T> data;

    private Integer totalPage; // tong so phan tu /trang

    private Integer currentPage; // page hien tai

    public PageableObject(Page<T> page) {
        this.data = page.getContent();
        this.totalPage = page.getTotalPages();
        this.currentPage = page.getNumber();
    }

}
