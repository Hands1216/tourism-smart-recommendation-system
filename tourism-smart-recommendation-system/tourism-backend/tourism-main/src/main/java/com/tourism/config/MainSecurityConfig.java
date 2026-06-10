package com.tourism.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 *
 */
@Configuration
@EnableWebSecurity
public class MainSecurityConfig {

    /**
     * 密码编码器
     */
    @Bean(name = "mainPasswordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链 - 允许所有请求
     */
    @Bean(name = "mainSecurityFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("=== 配置SecurityFilterChain：允许所有请求 ===");

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                System.out.println("=== 配置授权规则：允许所有请求 ===");
                auth.anyRequest().permitAll();
            })
            .cors(cors -> cors.disable());

        System.out.println("=== SecurityFilterChain配置完成 ===");
        return http.build();
    }
}
