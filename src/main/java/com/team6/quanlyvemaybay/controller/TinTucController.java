package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.news.CreateNewsRequest;
import com.team6.quanlyvemaybay.dto.news.NewsDetailDto;
import com.team6.quanlyvemaybay.dto.news.NewsItemDto;
import com.team6.quanlyvemaybay.dto.news.UpdateNewsRequest;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.service.TinTucService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controller cung cấp các API CRUD cho bảng Tin tức (phục vụ màn hình Quản trị tin tức & công khai).
 */
@RestController
@RequestMapping("/api/v1/tin-tuc")
@Tag(name = "Quản lý tin tức", description = "Các API CRUD (Danh sách, Chi tiết, Thêm, Sửa, Xóa) cho bài viết tin tức")
public class TinTucController {

    private static final Path NEWS_UPLOAD_DIR = Paths.get("uploads", "news").toAbsolutePath().normalize();
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final TinTucService tinTucService;

    public TinTucController(TinTucService tinTucService) {
        this.tinTucService = tinTucService;
    }

    @Operation(summary = "Lấy danh sách tin tức công khai (chỉ trả bài viết HIEN_THI)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NewsItemDto>>> getNewsList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chuyenMuc,
            @RequestParam(required = false) LoaiTinTuc loaiTin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        PageResponse<NewsItemDto> response = tinTucService.getPublicNewsList(keyword, chuyenMuc, loaiTin, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách tin tức thành công"));
    }

    @Operation(summary = "Lấy danh sách tin tức cho quản trị (hỗ trợ lọc cả bài viết ẩn)")
    @GetMapping("/quan-ly")
    public ResponseEntity<ApiResponse<PageResponse<NewsItemDto>>> getAdminNewsList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chuyenMuc,
            @RequestParam(required = false) LoaiTinTuc loaiTin,
            @RequestParam(required = false) TrangThaiTinTuc trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        PageResponse<NewsItemDto> response = tinTucService.getNewsList(keyword, chuyenMuc, loaiTin, trangThai, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách tin tức quản trị thành công"));
    }

    @Operation(summary = "Xem chi tiết một bài viết tin tức công khai theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsDetailDto>> getNewsDetail(@PathVariable Long id) {
        NewsDetailDto response = tinTucService.getPublicNewsDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết tin tức thành công"));
    }

    @Operation(summary = "Xem chi tiết bài viết tin tức cho quản trị (bao gồm bài ẩn)")
    @GetMapping("/quan-ly/{id}")
    public ResponseEntity<ApiResponse<NewsDetailDto>> getAdminNewsDetail(@PathVariable Long id) {
        NewsDetailDto response = tinTucService.getNewsDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết tin tức quản trị thành công"));
    }

    @Operation(summary = "Tải ảnh đại diện tin tức lên máy chủ")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadNewsImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "Vui lòng chọn tệp ảnh hợp lệ");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "Kích thước ảnh tối đa là 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "Chỉ chấp nhận ảnh định dạng JPG, PNG hoặc WebP");
        }
        String ext = switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Files.createDirectories(NEWS_UPLOAD_DIR);
            Path target = NEWS_UPLOAD_DIR.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_FAILED", "Không thể lưu ảnh tin tức lên máy chủ");
        }
        String url = "/api/v1/tin-tuc/images/" + filename;
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url, "filename", filename), "Tải ảnh tin tức thành công"));
    }

    @Operation(summary = "Phục vụ tệp ảnh tin tức đã tải lên")
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveNewsImage(@PathVariable String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Path filePath = NEWS_UPLOAD_DIR.resolve(filename).normalize();
            if (!filePath.startsWith(NEWS_UPLOAD_DIR) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok().contentType(mediaType).body(resource);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        }
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
