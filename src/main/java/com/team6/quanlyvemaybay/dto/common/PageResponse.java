package com.team6.quanlyvemaybay.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Cấu trúc phân trang chuẩn cho các danh sách trả về của API.
 */
public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
) {
    public int page() {
        return pageNumber;
    }

    public int size() {
        return pageSize;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
