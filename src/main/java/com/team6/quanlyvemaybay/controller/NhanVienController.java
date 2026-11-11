package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienResponse;
import com.team6.quanlyvemaybay.service.NhanVienService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Chỉ bật trong môi trường kiểm thử biệt lập khi chưa có phân quyền ADMIN. */
@RestController
@RequestMapping("/api/v1/nhan-vien")
@ConditionalOnProperty(name = "app.nhan-vien.allow-unsecured", havingValue = "true")
public class NhanVienController {
    private final NhanVienService service;

    public NhanVienController(NhanVienService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    public ApiResponse<SuaNhanVienResponse> suaNhanVien(@PathVariable @Min(1) Long id,
                                                          @Valid @RequestBody SuaNhanVienRequest request) {
        return ApiResponse.ok(service.suaNhanVien(id, request), "Sửa thông tin nhân viên thành công");
    }
}
