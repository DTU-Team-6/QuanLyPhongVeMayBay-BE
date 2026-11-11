package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.DanhSachNhanVienResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.TieuChiTimNhanVien;
import com.team6.quanlyvemaybay.service.NhanVienService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint chỉ bật để kiểm thử trong môi trường tin cậy khi chưa có phân quyền. */
@RestController
@RequestMapping("/api/v1/nhan-vien")
@ConditionalOnProperty(name = "app.nhan-vien.allow-unsecured", havingValue = "true")
public class NhanVienController {
    private final NhanVienService service;

    public NhanVienController(NhanVienService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<DanhSachNhanVienResponse> danhSach(
            @RequestParam(required = false) TieuChiTimNhanVien tieuChi,
            @RequestParam(required = false) @Size(max = 40) String tuKhoa,
            @RequestParam(defaultValue = "0") @Min(0) int trang,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int kichThuoc) {
        return ApiResponse.ok(service.danhSach(tieuChi, tuKhoa, trang, kichThuoc),
                "Lấy danh sách nhân viên thành công");
    }
}
