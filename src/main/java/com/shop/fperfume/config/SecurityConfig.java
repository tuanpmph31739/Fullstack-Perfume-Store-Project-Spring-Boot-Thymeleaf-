package com.shop.fperfume.config;

import com.shop.fperfume.security.CustomSuccessHandler;
import com.shop.fperfume.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // === SỬA LỖI BẢO MẬT ===
                        // 1. Các trang công khai (ai cũng vào được)
                        .requestMatchers(
                                "/", "/home", "/thuong-hieu/**", "/san-pham/**", // Sửa: Thêm /san-pham/**
                                "/login", "/register", "/verify",
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()

                        // 2. Trang Admin (Chỉ ADMIN)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 3. Các trang còn lại (Giỏ hàng, Checkout)
                        // Yêu cầu phải đăng nhập (authenticated)
                     .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        // === SỬA LỖI CSRF ===
        // Xóa (hoặc comment) dòng .csrf(csrf -> csrf.disable());
        // CSRF sẽ tự động được BẬT
        // .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

//package com.shop.fperfume.config;
//
//import com.shop.fperfume.security.CustomSuccessHandler;
//import com.shop.fperfume.security.CustomUserDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // <-- Import
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//
//    @Autowired
//    private CustomSuccessHandler customSuccessHandler;
//
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // === THAY ĐỔI Ở ĐÂY ===
//                        // Cho phép TẤT CẢ request đi qua mà không cần đăng nhập
//                        .requestMatchers("/**").permitAll()
//                )
//                // Tắt trang login
//                .formLogin(AbstractHttpConfigurer::disable)
//                // Tắt logout
//                .logout(AbstractHttpConfigurer::disable)
//                // === BẮT BUỘC: Tắt CSRF ===
//                .csrf(AbstractHttpConfigurer::disable); // (Hoặc .csrf(csrf -> csrf.disable()))
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//}