package com.shop.fperfume.config;

import com.shop.fperfume.interceptor.CartInterceptor;
import com.shop.fperfume.interceptor.CurrentPathInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CurrentPathInterceptor currentPathInterceptor;

    @Autowired
    private CartInterceptor cartInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho phép truy cập thư mục uploads/ qua URL /images/uploads/**
        registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor)
                // Áp dụng cho TẤT CẢ các URL
                .addPathPatterns("/**")
                // TRỪ các file tĩnh (CSS, JS, Ảnh...) để tăng hiệu suất
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/assets.assets.compiled/**");
    }




}
