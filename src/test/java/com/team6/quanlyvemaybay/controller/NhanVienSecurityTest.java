package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.config.SecurityConfig;
import com.team6.quanlyvemaybay.service.NhanVienService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NhanVienController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
class NhanVienSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhanVienService service;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void nhanVienThuongKhongDuocQuanLyNhanVien() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(post("/api/v1/nhan-vien").cookie(new Cookie("access_token", "staff-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/nhan-vien/1").cookie(new Cookie("access_token", "staff-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/nhan-vien/1").cookie(new Cookie("access_token", "staff-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminQuaDuocLopPhanQuyen() throws Exception {
        mockToken("admin-token", "ADMIN");
        mvc.perform(post("/api/v1/nhan-vien").cookie(new Cookie("access_token", "admin-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminThieuCsrfBiTuChoi() throws Exception {
        mockToken("admin-token", "ADMIN");
        mvc.perform(post("/api/v1/nhan-vien")
                        .cookie(new Cookie("access_token", "admin-token"))
                        .contentType("application/json")
                        .content("""
                                {"maNhanVien":"NV1","hoTen":"Nguyen An","ngaySinh":"1990-01-01",
                                 "gioiTinh":"Nam","email":"an@example.test","matKhau":"Matkhau123",
                                 "soDienThoai":"0123456789","vaiTro":"NHAN_VIEN"}
                                """))
                .andExpect(status().isForbidden());
    }

    private void mockToken(String value, String role) {
        when(jwtDecoder.decode(value)).thenReturn(Jwt.withTokenValue(value)
                .header("alg", "HS256").subject("42").claim("role", role).build());
    }
}
