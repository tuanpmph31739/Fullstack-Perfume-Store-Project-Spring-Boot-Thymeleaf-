package com.shop.fperfume.config;

import com.shop.fperfume.security.CustomSuccessHandler;
import com.shop.fperfume.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomSuccessHandler customSuccessHandler) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/gioi-thieu/**",
                                "/san-pham/**",
                                "/thuong-hieu/**",
                                "/cart/**",
                                "/checkout/**",
                                "/order/**",
                                "/assets/**",
                                "/user/orders/**"   // ✅ khách vãng lai xem / huỷ đơn
                        ).permitAll()


                        .requestMatchers("/admin/**")
                        .hasAnyRole("ADMIN", "NHANVIEN")

                        // Khu tài khoản user (phải đăng nhập)
                        .requestMatchers("/account/**")
                        .authenticated()

                        // Các request khác mặc định yêu cầu đăng nhập
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

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}