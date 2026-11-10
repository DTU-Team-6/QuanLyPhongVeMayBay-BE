package com.team6.quanlyvemaybay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình tài liệu OpenAPI / Swagger UI cho hệ thống Quản lý phòng bán vé máy bay.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quanLyVeMayBayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Hệ thống Quản lý Phòng Vé Máy Bay - DTU Team 6")
                        .version("v1.0")
                        .description("RESTful API phục vụ trang chủ, thành phần dùng chung (Header/Footer) và danh sách Top 10 chuyến bay giá rẻ.")
                        .contact(new Contact()
                                .name("DTU Team 6")
                                .email("support@dtuteam6airlines.vn")));
    }
}
