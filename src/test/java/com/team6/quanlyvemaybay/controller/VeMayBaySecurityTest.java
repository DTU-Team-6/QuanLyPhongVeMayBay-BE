package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.config.SecurityConfig;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.service.VeMayBayService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VeMayBayController.class)
@Import(SecurityConfig.class)
class VeMayBaySecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean VeMayBayService service;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void nguoiChuaDangNhapKhongXemDuocDanhSachVe() throws Exception {
        mvc.perform(get("/api/v1/ve-may-bay")).andExpect(status().isUnauthorized());
    }

    @Test
    void khachHangKhongXemDuocDanhSachVe() throws Exception {
        mockToken("customer-token", "KHACH_HANG");
        mvc.perform(get("/api/v1/ve-may-bay").cookie(new Cookie("access_token", "customer-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void nhanVienXemDuocDanhSachVeRong() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        when(service.danhSachVe(null, null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));
        mvc.perform(get("/api/v1/ve-may-bay").cookie(new Cookie("access_token", "staff-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    private void mockToken(String value, String role) {
        when(jwtDecoder.decode(value)).thenReturn(Jwt.withTokenValue(value)
                .header("alg", "HS256").subject("42").claim("role", role).build());
    }
}
