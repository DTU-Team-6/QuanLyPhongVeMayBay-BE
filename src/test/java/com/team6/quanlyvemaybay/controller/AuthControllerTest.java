package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.ChangePasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdatePersonalInfoRequest;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private AuthService authService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        controller = new AuthController(authService, false);
    }

    @Test
    void csrfEndpointMaterializesTokenAndReturnsNoContent() {
        CsrfToken csrfToken = mock(CsrfToken.class);

        var response = controller.csrf(csrfToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(csrfToken).getToken();
    }

    @Test
    void registerReturnsCreatedUser() {
        RegisterRequest request = new RegisterRequest(
                "user@example.com", "password-123", "Nguyễn Văn A", null, null, null
        );
        AuthUserResponse user = new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG);
        when(authService.register(request)).thenReturn(user);

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    void loginSetsHttpOnlyAccessCookieAndDoesNotReturnTokenInBody() {
        LoginRequest request = new LoginRequest("user@example.com", "password-123");
        AuthUserResponse user = new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG);
        when(authService.login(request)).thenReturn(new AuthService.LoginResult(user, "signed-token"));

        var response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody().getData());
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(cookie.contains("access_token=signed-token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertTrue(cookie.contains("Max-Age=900"));
        assertFalse(response.getBody().toString().contains("signed-token"));
    }

    @Test
    void meReturnsPersonalInfoForAuthenticatedUser() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        PersonalInfoResponse profile = new PersonalInfoResponse(
                42L, "user@example.com", "Nguyễn Văn A", "+84901234567",
                LocalDate.of(1995, 4, 20), GioiTinh.Nam, "Đà Nẵng", null, VaiTro.KHACH_HANG
        );
        when(authService.currentUser(42L)).thenReturn(profile);

        var response = controller.me(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profile, response.getBody().getData());
    }

    @Test
    void updatePersonalInfoReturnsUpdatedProfile() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        UpdatePersonalInfoRequest request = new UpdatePersonalInfoRequest(
                "Nguyễn Văn B", null, null, GioiTinh.Khac, null
        );
        PersonalInfoResponse profile = new PersonalInfoResponse(
                42L, "user@example.com", "Nguyễn Văn B", null,
                null, GioiTinh.Khac, null, null, VaiTro.KHACH_HANG
        );
        when(authService.updatePersonalInfo(42L, request)).thenReturn(profile);

        var response = controller.updatePersonalInfo(jwt, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profile, response.getBody().getData());
    }

    @Test
    void changePasswordReturnsSuccessMessage() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");

        var response = controller.changePassword(jwt, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Đổi mật khẩu thành công", response.getBody().getMessage());
        verify(authService).changePassword(42L, request);
    }
}
