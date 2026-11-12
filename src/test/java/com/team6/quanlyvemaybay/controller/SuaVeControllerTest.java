package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.config.SecurityConfig;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeRequest;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeResponse;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.service.SuaVeService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SuaVeController.class)
@Import(SecurityConfig.class)
class SuaVeControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean SuaVeService service;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void nhanVienSuaVeHopLe() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        SuaVeRequest request = new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com");
        when(service.suaVe(1L, request)).thenReturn(
                new SuaVeResponse(1L, "VE001", "Nguyen Van Moi", "ve-moi@example.com", TrangThaiVe.CHO_THANH_TOAN));

        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hoTenNguoiDi").value("Nguyen Van Moi"))
                .andExpect(jsonPath("$.data.giaGoc").doesNotExist());
    }

    @Test
    void khachHangKhongDuocSuaVe() throws Exception {
        mockToken("customer-token", "KHACH_HANG");
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com")))
                        .cookie(new Cookie("access_token", "customer-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isForbidden());
    }

    @Test
    void thieuCsrfBiTuChoi() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com")))
                        .cookie(new Cookie("access_token", "staff-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void emailKhongHopLeTra400() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuaVeRequest("Nguyen Van Moi", "email-sai")))
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void tenQuaNganTra400() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuaVeRequest("An", "ve-moi@example.com")))
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void veKhongTonTaiTra404() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        SuaVeRequest request = new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com");
        when(service.suaVe(99L, request)).thenThrow(new ResourceNotFoundException("TICKET_NOT_FOUND", "Không tìm thấy vé."));
        mvc.perform(put("/api/v1/ve-may-bay/99").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));
    }

    @Test
    void veKhongDuocSuaTra409() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        SuaVeRequest request = new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com");
        when(service.suaVe(1L, request)).thenThrow(new BusinessException(
                HttpStatus.CONFLICT, "TICKET_NOT_EDITABLE", "Vé đã thanh toán hoặc đã hủy không được sửa."));
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_EDITABLE"));
    }

    @Test
    void chuaDangNhapTra401() throws Exception {
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com")))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void idKhongHopLeTra400() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(put("/api/v1/ve-may-bay/0").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com")))
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void jsonSaiDinhDangTra400() throws Exception {
        mockToken("staff-token", "NHAN_VIEN");
        mvc.perform(put("/api/v1/ve-may-bay/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{")
                        .cookie(new Cookie("access_token", "staff-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    private void mockToken(String value, String role) {
        when(jwtDecoder.decode(value)).thenReturn(Jwt.withTokenValue(value)
                .header("alg", "HS256").subject("42").claim("role", role).build());
    }
}
