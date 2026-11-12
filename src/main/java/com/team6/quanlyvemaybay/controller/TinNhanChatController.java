package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.chat.GuiTinNhanRequest;
import com.team6.quanlyvemaybay.dto.chat.HoiThoaiResponse;
import com.team6.quanlyvemaybay.dto.chat.TinNhanChatResponse;
import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.service.TinNhanChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class TinNhanChatController {
    private final TinNhanChatService service;

    public TinNhanChatController(TinNhanChatService service) {
        this.service = service;
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<TinNhanChatResponse>> khachGui(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody GuiTinNhanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "Gửi tin nhắn thành công",
                        service.khachGui(Long.valueOf(jwt.getSubject()), request)));
    }

    @GetMapping("/messages")
    public ApiResponse<PageResponse<TinNhanChatResponse>> lichSuCuaKhach(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) int trang,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int kichThuoc) {
        return ApiResponse.ok(service.lichSuCuaKhach(Long.valueOf(jwt.getSubject()), trang, kichThuoc),
                "Lấy lịch sử tin nhắn thành công");
    }

    @GetMapping("/conversations")
    public ApiResponse<PageResponse<HoiThoaiResponse>> danhSachHoiThoai(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) int trang,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int kichThuoc) {
        return ApiResponse.ok(service.danhSachHoiThoai(Long.valueOf(jwt.getSubject()), trang, kichThuoc),
                "Lấy danh sách hội thoại thành công");
    }

    @GetMapping("/conversations/{khachHangId}/messages")
    public ApiResponse<PageResponse<TinNhanChatResponse>> lichSuChoAdmin(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long khachHangId,
            @RequestParam(defaultValue = "0") @Min(0) int trang,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int kichThuoc) {
        return ApiResponse.ok(service.lichSuChoAdmin(Long.valueOf(jwt.getSubject()), khachHangId,
                trang, kichThuoc), "Lấy lịch sử hội thoại thành công");
    }

    @PostMapping("/conversations/{khachHangId}/messages")
    public ResponseEntity<ApiResponse<TinNhanChatResponse>> adminTraLoi(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long khachHangId,
            @Valid @RequestBody GuiTinNhanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "Gửi tin nhắn thành công",
                        service.adminTraLoi(Long.valueOf(jwt.getSubject()), khachHangId, request)));
    }
}
