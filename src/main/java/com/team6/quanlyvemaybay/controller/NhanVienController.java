package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.XoaNhanVienResponse;
import com.team6.quanlyvemaybay.service.NhanVienService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{id}")
    public ApiResponse<SuaNhanVienResponse> suaNhanVien(@PathVariable @Min(1) Long id,
                                                          @Valid @RequestBody SuaNhanVienRequest request) {
        return ApiResponse.ok(service.suaNhanVien(id, request), "Sửa thông tin nhân viên thành công");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<XoaNhanVienResponse> xoaNhanVien(@PathVariable @Min(1) Long id) {
        return ApiResponse.ok(service.xoaNhanVien(id), "Ngừng hoạt động nhân viên thành công");
    }
}
