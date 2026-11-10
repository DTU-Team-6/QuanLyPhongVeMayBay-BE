package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.publicinfo.CommonInfoResponse;
import com.team6.quanlyvemaybay.service.ThongTinChungService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cung cấp cấu hình và thông tin công khai dùng chung cho Header/Footer và Trang chủ.
 */
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Thông tin công khai", description = "API cấu hình Header, Footer và banner thông báo chung")
public class CongKhaiController {

    private final ThongTinChungService thongTinChungService;

    public CongKhaiController(ThongTinChungService thongTinChungService) {
        this.thongTinChungService = thongTinChungService;
    }

    @Operation(
            summary = "Lấy thông tin dùng chung (Header / Footer / Banners)",
            description = "Trả về hotline, email, địa chỉ, liên kết chính sách và các banner/thông báo đang hiển thị."
    )
    @GetMapping("/common-info")
    public ResponseEntity<ApiResponse<CommonInfoResponse>> getCommonInfo() {
        CommonInfoResponse commonInfo = thongTinChungService.getCommonInfo();
        return ResponseEntity.ok(ApiResponse.ok(commonInfo, "Lấy thông tin dùng chung thành công"));
    }
}
