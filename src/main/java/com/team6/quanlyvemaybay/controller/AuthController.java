package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.service.AuthService;
import com.team6.quanlyvemaybay.service.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.cookie-secure:false}") boolean cookieSecure
    ) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthUserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthUserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "Đăng ký thành công", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthUserResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthService.LoginResult result = authService.login(request);
        ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(JwtTokenService.ACCESS_TOKEN_TTL)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(ApiResponse.ok(result.user(), "Đăng nhập thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        AuthUserResponse user = authService.currentUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(user, "Lấy thông tin người dùng thành công"));
    }
}
