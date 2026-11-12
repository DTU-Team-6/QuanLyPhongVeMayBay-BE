package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.config.SecurityConfig;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.XoaKhachHangResponse;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.KhachHangService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KhachHangController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class KhachHangControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KhachHangService khachHangService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void danhSachKhongDangNhapTra401() throws Exception {
        mockMvc.perform(get("/api/v1/khach-hang"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void khachHangKhongCoQuyenQuanLyTra403() throws Exception {
        when(jwtDecoder.decode("customer-token")).thenReturn(jwt("customer-token", VaiTro.KHACH_HANG));

        mockMvc.perform(get("/api/v1/khach-hang")
                        .cookie(new Cookie("access_token", "customer-token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void nhanVienCoTheTimDanhSachKhachHang() throws Exception {
        when(jwtDecoder.decode("staff-token")).thenReturn(jwt("staff-token", VaiTro.NHAN_VIEN));
        when(khachHangService.layDanhSach("a@example.com", 0, 10)).thenReturn(
                new PageResponse<>(
                        List.of(taoResponse()),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                )
        );

        mockMvc.perform(get("/api/v1/khach-hang")
                        .cookie(new Cookie("access_token", "staff-token"))
                        .param("keyword", "a@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("khach@example.com"))
                .andExpect(jsonPath("$.data.content[0].diemTichLuy").value(10));
    }

    @Test
    void adminCoTheXemChiTietKhachHang() throws Exception {
        when(jwtDecoder.decode("admin-token")).thenReturn(jwt("admin-token", VaiTro.ADMIN));
        when(khachHangService.layChiTiet(7L)).thenReturn(taoResponse());

        mockMvc.perform(get("/api/v1/khach-hang/7")
                        .cookie(new Cookie("access_token", "admin-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.hoTen").value("Nguyễn Văn A"));
    }

    @Test
    void nhanVienCoTheTaoKhachHangCoCsrf() throws Exception {
        when(jwtDecoder.decode("staff-token")).thenReturn(jwt("staff-token", VaiTro.NHAN_VIEN));
        when(khachHangService.themMoi(any())).thenReturn(taoResponse());

        mockMvc.perform(post("/api/v1/khach-hang")
                        .cookie(new Cookie("access_token", "staff-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"khach@example.com",
                                  "hoTen":"Nguyễn Văn A",
                                  "ngaySinh":"1995-04-20",
                                  "gioiTinh":"Nam",
                                  "quocTich":"Việt Nam"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("khach@example.com"));
    }

    @Test
    void taoKhachHangThieuCsrfTra403() throws Exception {
        when(jwtDecoder.decode("staff-token")).thenReturn(jwt("staff-token", VaiTro.NHAN_VIEN));

        mockMvc.perform(post("/api/v1/khach-hang")
                        .cookie(new Cookie("access_token", "staff-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"khach@example.com","hoTen":"Nguyễn Văn A"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminCoTheCapNhatHoSoKhachHangVoiCsrf() throws Exception {
        when(jwtDecoder.decode("admin-token")).thenReturn(jwt("admin-token", VaiTro.ADMIN));
        when(khachHangService.capNhat(eq(7L), any())).thenReturn(taoResponse());

        mockMvc.perform(put("/api/v1/khach-hang/7")
                        .cookie(new Cookie("access_token", "admin-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"khach@example.com",
                                  "hoTen":"Nguyễn Văn A",
                                  "gioiTinh":"Nam",
                                  "quocTich":"Việt Nam",
                                  "xepHang":"Member",
                                  "diemTichLuy":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.xepHang").value("Member"));
    }

    @Test
    void nhanVienCoTheNgungHoatDongKhachHangVoiCsrf() throws Exception {
        when(jwtDecoder.decode("staff-token")).thenReturn(jwt("staff-token", VaiTro.NHAN_VIEN));
        when(khachHangService.ngungHoatDong(7L)).thenReturn(new XoaKhachHangResponse(7L, (byte) 0));

        mockMvc.perform(delete("/api/v1/khach-hang/7")
                        .cookie(new Cookie("access_token", "staff-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trangThai").value(0));
    }

    @Test
    void thamSoPhanTrangKhongHopLeTra400() throws Exception {
        when(jwtDecoder.decode("staff-token")).thenReturn(jwt("staff-token", VaiTro.NHAN_VIEN));

        mockMvc.perform(get("/api/v1/khach-hang")
                        .cookie(new Cookie("access_token", "staff-token"))
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private Jwt jwt(String token, VaiTro vaiTro) {
        return Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .subject("42")
                .claim("role", vaiTro.name())
                .build();
    }

    private KhachHangResponse taoResponse() {
        return new KhachHangResponse(
                7L,
                "khach@example.com",
                "Nguyễn Văn A",
                "+84901234567",
                LocalDate.of(1995, 4, 20),
                GioiTinh.Nam,
                "Đà Nẵng",
                "012345678901",
                "Việt Nam",
                "Member",
                10,
                (byte) 1
        );
    }
}
