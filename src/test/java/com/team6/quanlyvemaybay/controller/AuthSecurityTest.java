package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.AuthService;
import com.team6.quanlyvemaybay.service.ChuyenBayService;
import com.team6.quanlyvemaybay.service.PhienLamMoiService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, ChuyenBayController.class})
@Import({com.team6.quanlyvemaybay.config.SecurityConfig.class, GlobalExceptionHandler.class})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PhienLamMoiService phienLamMoiService;

    @MockitoBean
    private java.time.Clock clock;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ChuyenBayService chuyenBayService;

    @BeforeEach
    void setUp() {
        when(phienLamMoiService.tao(anyLong(), anyBoolean())).thenReturn(
                new PhienLamMoiService.KetQua("refresh-token", null, java.time.Instant.parse("2030-01-01T00:00:00Z"), false));
    }

    @Test
    void staleAccessCookieDoesNotBlockPublicFlightsOrCsrf() throws Exception {
        when(chuyenBayService.getTopCheapFlights(10)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/flights/top-cheap")
                        .cookie(new Cookie("access_token", "expired-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(get("/api/v1/auth/csrf")
                        .cookie(new Cookie("access_token", "expired-token")))
                .andExpect(status().isNoContent());
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void staleAccessCookieDoesNotBlockLoggingInAgain() throws Exception {
        when(authService.login(any())).thenReturn(new AuthService.LoginResult(
                new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG),
                "new-access-token"
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(new Cookie("access_token", "expired-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("access_token=new-access-token")));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void logoutClearsBothCookiesEvenWithExpiredToken() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("access_token", "expired-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isNoContent())
                .andReturn();

        org.assertj.core.api.Assertions.assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .hasSize(2)
                .allSatisfy(cookie -> org.assertj.core.api.Assertions.assertThat(cookie)
                        .contains("Max-Age=0", "Path=/", "HttpOnly", "SameSite=Lax"))
                .anySatisfy(cookie -> org.assertj.core.api.Assertions.assertThat(cookie).startsWith("access_token="))
                .anySatisfy(cookie -> org.assertj.core.api.Assertions.assertThat(cookie).startsWith("refresh_token="));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void logoutStillRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void refreshRequiresCsrfEvenWhenAccessTokenHasExpired() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("access_token", "expired-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshIgnoresExpiredAccessCookieAndRotatesCookies() throws Exception {
        when(phienLamMoiService.lamMoi("refresh-token")).thenReturn(
                new PhienLamMoiService.KetQua("new-refresh", "new-access",
                        java.time.Instant.parse("2030-01-01T00:00:00Z"), false));
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("access_token", "expired-token"),
                                new Cookie("refresh_token", "refresh-token"),
                                new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("access_token=new-access")));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void staleAccessCookieStillCannotReadPrivateProfile() throws Exception {
        when(jwtDecoder.decode("expired-token")).thenThrow(new BadJwtException("Token expired"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .cookie(new Cookie("access_token", "expired-token")))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void csrfEndpointSetsReadableCsrfCookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("XSRF-TOKEN=")));
    }

    @Test
    void postLoginWithoutCsrfTokenIsRejectedAsProblemJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password-123"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void loginWithCsrfTokenSetsHttpOnlyAccessCookie() throws Exception {
        when(authService.login(any())).thenReturn(new AuthService.LoginResult(
                new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG),
                "signed-access-token"
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("access_token=signed-access-token")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")));
    }

    @Test
    void customerTokenCannotAccessStaffReports() throws Exception {
        when(jwtDecoder.decode("customer-token")).thenReturn(Jwt.withTokenValue("customer-token")
                .header("alg", "HS256")
                .subject("42")
                .claim("role", VaiTro.KHACH_HANG.name())
                .build());

        mockMvc.perform(get("/api/v1/reports")
                        .cookie(new Cookie("access_token", "customer-token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void missingAccessTokenCannotReadCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void changePasswordRequiresAuthenticationAndCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"old-password","newPassword":"new-password"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void authenticatedUserCanUpdateOwnProfileWithCsrf() throws Exception {
        when(jwtDecoder.decode("customer-token")).thenReturn(Jwt.withTokenValue("customer-token")
                .header("alg", "HS256")
                .subject("42")
                .claim("role", VaiTro.KHACH_HANG.name())
                .build());
        when(authService.updatePersonalInfo(anyLong(), any())).thenReturn(
                new PersonalInfoResponse(
                        42L, "user@example.com", "Nguyễn Văn B", "+84901234567",
                        null, GioiTinh.Khac,
                        "Đà Nẵng", null, VaiTro.KHACH_HANG
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/me")
                        .cookie(new Cookie("access_token", "customer-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hoTen":"Nguyễn Văn B","soDienThoai":"+84901234567",
                                 "ngaySinh":null,"gioiTinh":"Khac","diaChi":"Đà Nẵng"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hoTen").value("Nguyễn Văn B"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    void updatePersonalInfoRequiresAuthenticationEvenWithCsrf() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/me")
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hoTen":"Nguyễn Văn B","soDienThoai":null,
                                 "ngaySinh":null,"gioiTinh":"Khac","diaChi":null}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void authenticatedUserCanChangePasswordWithCsrf() throws Exception {
        when(jwtDecoder.decode("customer-token")).thenReturn(Jwt.withTokenValue("customer-token")
                .header("alg", "HS256")
                .subject("42")
                .claim("role", VaiTro.KHACH_HANG.name())
                .build());

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .cookie(new Cookie("access_token", "customer-token"))
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"old-password","newPassword":"new-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));
    }
}
