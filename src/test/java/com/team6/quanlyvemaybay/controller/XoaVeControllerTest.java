package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.config.SecurityConfig;
import com.team6.quanlyvemaybay.dto.ticket.XoaVeResponse;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.service.XoaVeService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = XoaVeController.class)
@Import(SecurityConfig.class)
class XoaVeControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean XoaVeService service;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void khachHangKhongDuocHuyVe() throws Exception {
        mockToken("customer-token", "KHACH_HANG");
        mvc.perform(delete("/api/v1/ve-may-bay/1")
                        .cookie(new Cookie("access_token", "customer-token"),
                                new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nhanVienHuyVeChuaThanhToan() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        when(service.xoaVe(1L)).thenReturn(new XoaVeResponse(1L, "VE001", TrangThaiVe.DA_HUY));
        mvc.perform(delete("/api/v1/ve-may-bay/1")
                        .cookie(new Cookie("access_token", "staff-token"),
                                new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trangThai").value("DA_HUY"));
    }

    @Test
    void thieuCsrfBiTuChoi() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(delete("/api/v1/ve-may-bay/1").cookie(new Cookie("access_token", "staff-token")))
                .andExpect(status().isForbidden());
    }

    private void mockToken(String value, String role) {
        when(jwtDecoder.decode(value)).thenReturn(Jwt.withTokenValue(value)
                .header("alg", "HS256").subject("42").claim("role", role).build());
    }
}
