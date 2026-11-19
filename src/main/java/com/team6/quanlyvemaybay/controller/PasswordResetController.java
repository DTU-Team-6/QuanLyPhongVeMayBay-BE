package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.auth.ForgotPasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.ResetPasswordRequest;
import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordResetController {
    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        service.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.ok(null,
                "Nếu tài khoản phù hợp tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi qua email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        service.reset(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đặt lại mật khẩu thành công."));
    }
}
