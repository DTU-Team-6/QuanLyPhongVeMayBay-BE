package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.AvatarUpdateResponse;
import com.team6.quanlyvemaybay.dto.auth.AvatarUploadSignatureResponse;
import com.team6.quanlyvemaybay.dto.auth.ChangePasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdatePersonalInfoRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdateAvatarRequest;
import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.service.AuthService;
import com.team6.quanlyvemaybay.service.CloudinaryAvatarService;
import com.team6.quanlyvemaybay.service.JwtTokenService;
import com.team6.quanlyvemaybay.service.PhienLamMoiService;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CloudinaryAvatarService cloudinaryAvatarService;
    private final PhienLamMoiService phienLamMoiService;
    private final boolean cookieSecure;
    private final Clock clock;

    public AuthController(
            AuthService authService,
            CloudinaryAvatarService cloudinaryAvatarService,
            PhienLamMoiService phienLamMoiService,
            @Value("${app.auth.cookie-secure:false}") boolean cookieSecure,
            Clock clock
    ) {
        this.authService = authService;
        this.cloudinaryAvatarService = cloudinaryAvatarService;
        this.phienLamMoiService = phienLamMoiService;
        this.cookieSecure = cookieSecure;
        this.clock = clock;
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
        PhienLamMoiService.KetQua phien = phienLamMoiService.tao(result.user().id(), Boolean.TRUE.equals(request.rememberMe()));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie(result.accessToken(), phien.ghiNho()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(phien).toString())
                .body(ApiResponse.ok(result.user(), "Đăng nhập thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = "refresh_token", required = false) String token) {
        PhienLamMoiService.KetQua phien = phienLamMoiService.lamMoi(token);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie(phien.accessToken(), phien.ghiNho()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(phien).toString())
                .build();
    }

    /**
     * Xoá cookie phiên hiện tại, kể cả khi access token đã hết hạn.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String token) {
        phienLamMoiService.thuHoi(token);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredAuthCookie("access_token").toString())
                .header(HttpHeaders.SET_COOKIE, expiredAuthCookie("refresh_token").toString())
                .build();
    }

    private ResponseCookie accessCookie(String token, boolean ghiNho) {
        var cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/");
        if (ghiNho) cookie.maxAge(JwtTokenService.ACCESS_TOKEN_TTL);
        return cookie.build();
    }

    private ResponseCookie refreshCookie(PhienLamMoiService.KetQua phien) {
        var cookie = ResponseCookie.from("refresh_token", phien.refreshToken())
                .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/");
        if (phien.ghiNho()) {
            cookie.maxAge(Duration.between(Instant.now(clock), phien.hetHan()));
        }
        return cookie.build();
    }

    private ResponseCookie expiredAuthCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PersonalInfoResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        PersonalInfoResponse user = authService.currentUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(user, "Lấy thông tin người dùng thành công"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<PersonalInfoResponse>> updatePersonalInfo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        PersonalInfoResponse user = authService.updatePersonalInfo(
                Long.valueOf(jwt.getSubject()),
                request
        );
        return ResponseEntity.ok(ApiResponse.ok(user, "Cập nhật thông tin cá nhân thành công"));
    }

    @PostMapping("/avatar/upload-signature")
    public ResponseEntity<ApiResponse<AvatarUploadSignatureResponse>> createAvatarUploadSignature(
            @AuthenticationPrincipal Jwt jwt
    ) {
        AvatarUploadSignatureResponse signature = cloudinaryAvatarService.createUploadSignature(
                Long.valueOf(jwt.getSubject())
        );
        return ResponseEntity.ok(ApiResponse.ok(signature, "Đã tạo thông tin tải ảnh đại diện"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<AvatarUpdateResponse>> updateAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAvatarRequest request
    ) {
        AvatarUpdateResponse result = cloudinaryAvatarService.updateAvatar(
                Long.valueOf(jwt.getSubject()),
                request.publicId()
        );
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật ảnh đại diện thành công"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đổi mật khẩu thành công"));
    }
}
