package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.service.KhachHangService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/khach-hang")
public class KhachHangController {

    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    @GetMapping
    public ApiResponse<PageResponse<KhachHangResponse>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        PageResponse<KhachHangResponse> result = khachHangService.layDanhSach(keyword, page, size);
        return ApiResponse.ok(result, "Lấy danh sách khách hàng thành công");
    }

    @GetMapping("/{id}")
    public ApiResponse<KhachHangResponse> layChiTiet(@PathVariable @Min(1) Long id) {
        return ApiResponse.ok(khachHangService.layChiTiet(id), "Lấy thông tin khách hàng thành công");
    }
}
