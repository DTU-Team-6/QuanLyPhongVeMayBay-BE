package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.CapNhatKhachHangRequest;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.dto.khachhang.TaoKhachHangRequest;
import com.team6.quanlyvemaybay.dto.khachhang.XoaKhachHangResponse;
import com.team6.quanlyvemaybay.service.KhachHangService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<ApiResponse<KhachHangResponse>> themMoi(
            @Valid @RequestBody TaoKhachHangRequest request
    ) {
        KhachHangResponse created = khachHangService.themMoi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "Thêm khách hàng thành công", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<KhachHangResponse> capNhat(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody CapNhatKhachHangRequest request
    ) {
        return ApiResponse.ok(khachHangService.capNhat(id, request), "Cập nhật khách hàng thành công");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<XoaKhachHangResponse> ngungHoatDong(@PathVariable @Min(1) Long id) {
        return ApiResponse.ok(khachHangService.ngungHoatDong(id), "Ngừng hoạt động khách hàng thành công");
    }
}
