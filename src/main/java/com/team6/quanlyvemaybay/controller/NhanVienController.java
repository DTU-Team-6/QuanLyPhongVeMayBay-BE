package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienResponse;
import com.team6.quanlyvemaybay.service.NhanVienService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;

/** Chỉ bật trong môi trường kiểm thử biệt lập khi chưa có phân quyền ADMIN. */
@RestController
@RequestMapping("/api/v1/nhan-vien")
@ConditionalOnProperty(name = "app.nhan-vien.allow-unsecured", havingValue = "true")
public class NhanVienController {
    private final NhanVienService service;

    public NhanVienController(NhanVienService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ThemNhanVienResponse>> themNhanVien(@Valid @RequestBody ThemNhanVienRequest request) {
        ThemNhanVienResponse result = service.themNhanVien(request);
        return ResponseEntity.created(URI.create("/api/v1/nhan-vien/" + result.id()))
                .body(ApiResponse.of(HttpStatus.CREATED, "Thêm nhân viên thành công", result));
    }
}
