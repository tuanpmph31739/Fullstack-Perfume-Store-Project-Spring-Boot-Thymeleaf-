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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomSuccessHandler customSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        // ================== PUBLIC ==================
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify",

                                "/search/**",
                                "/api/search-suggest/**",

                                "/gioi-thieu/**",
                                "/lien-he/**",

                                "/403",
                                "/error",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/assets/**",

                                "/san-pham/**",
                                "/thuong-hieu/**",
                                "/cart/**",
                                "/checkout/**",
                                "/order/**",
                                "/user/orders/**",

                                "/payment/vnpay/return"
                        ).permitAll()

                        // ============ ADMIN + NHÂN VIÊN ============
                        // Nhân viên CHỈ được vào các module này:
                        .requestMatchers(
                                "/admin",
                                "/admin/",
                                "/admin/don-hang/**",
                                "/admin/hoa-don/**",
                                "/admin/khach-hang/**",
                                "/admin/ban-hang-tai-quay/**"
                        ).hasAnyRole("ADMIN", "NHANVIEN")

                        // ============== CHỈ ADMIN ==================
                        // Các URL admin còn lại CHỈ ADMIN được vào
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ============== USER ĐÃ ĐĂNG NHẬP ==========
                        .requestMatchers("/account/**").authenticated()

                        // Các request khác mặc định yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )

                // ============== LOGIN FORM ======================
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // ============== LOGOUT ==========================
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )

                // ============== 403 PAGE ========================
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )

                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
