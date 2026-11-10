package com.team6.quanlyvemaybay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Cấu hình CORS cho phép frontend (React/Vite) gọi API và gửi kèm cookie xác thực.
 * Hỗ trợ nhiều origin phân tách bởi dấu phẩy và không dùng wildcard '*' khi allowCredentials=true.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String DEFAULT_FRONTEND_URL = "http://localhost:5173";

    @Value("${app.frontend-url:" + DEFAULT_FRONTEND_URL + "}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = Arrays.stream((frontendUrl == null ? "" : frontendUrl).split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty() && !"*".equals(origin))
                .toArray(String[]::new);

        if (allowedOrigins.length == 0) {
            allowedOrigins = new String[]{DEFAULT_FRONTEND_URL};
        }

        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
