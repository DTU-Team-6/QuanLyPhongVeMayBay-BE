package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.ticket.TieuChiTimVe;
import com.team6.quanlyvemaybay.dto.ticket.VeDanhSachResponse;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.service.VeMayBayService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ve-may-bay")
public class VeMayBayController {
    private final VeMayBayService service;

    public VeMayBayController(VeMayBayService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<VeDanhSachResponse>> danhSachVe(
            @RequestParam(required = false) TrangThaiVe trangThai,
            @RequestParam(required = false) TieuChiTimVe tieuChi,
            @RequestParam(required = false) @Size(max = 40) String tuKhoa,
            @RequestParam(defaultValue = "0") @Min(0) int trang,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int kichThuoc) {
        return ApiResponse.ok(service.danhSachVe(trangThai, tieuChi, tuKhoa, trang, kichThuoc),
                "Lấy danh sách vé thành công");
    }
}
