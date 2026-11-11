package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.news.CreateNewsRequest;
import com.team6.quanlyvemaybay.dto.news.NewsDetailDto;
import com.team6.quanlyvemaybay.dto.news.NewsItemDto;
import com.team6.quanlyvemaybay.dto.news.UpdateNewsRequest;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import com.team6.quanlyvemaybay.service.TinTucService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller cung cấp các API CRUD cho bảng Tin tức (phục vụ màn hình Quản trị tin tức).
 */
@RestController
@RequestMapping("/api/v1/tin-tuc")
@Tag(name = "Quản lý tin tức", description = "Các API CRUD (Danh sách, Chi tiết, Thêm, Sửa, Xóa) cho bài viết tin tức")
public class TinTucController {

    private final TinTucService tinTucService;

    public TinTucController(TinTucService tinTucService) {
        this.tinTucService = tinTucService;
    }

    @Operation(summary = "Lấy danh sách tin tức (Hỗ trợ tìm kiếm từ khóa, lọc chuyên mục, phân trang)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NewsItemDto>>> getNewsList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chuyenMuc,
            @RequestParam(required = false) LoaiTinTuc loaiTin,
            @RequestParam(required = false) TrangThaiTinTuc trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        PageResponse<NewsItemDto> response = tinTucService.getNewsList(keyword, chuyenMuc, loaiTin, trangThai, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách tin tức thành công"));
    }

    @Operation(summary = "Xem chi tiết một bài viết tin tức theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsDetailDto>> getNewsDetail(@PathVariable Long id) {
        NewsDetailDto response = tinTucService.getNewsDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết tin tức thành công"));
    }

    @Operation(summary = "Thêm tin tức mới")
    @PostMapping
    public ResponseEntity<ApiResponse<NewsDetailDto>> createNews(
            @Valid @RequestBody CreateNewsRequest request) {
        NewsDetailDto response = tinTucService.createNews(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Thêm bài viết tin tức thành công"));
    }

    @Operation(summary = "Chỉnh sửa bài viết tin tức")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsDetailDto>> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNewsRequest request) {
        NewsDetailDto response = tinTucService.updateNews(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật bài viết tin tức thành công"));
    }

    @Operation(summary = "Xóa bài viết tin tức")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteNews(@PathVariable Long id) {
        String message = tinTucService.deleteNews(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("id", id, "message", message), message));
    }
}
