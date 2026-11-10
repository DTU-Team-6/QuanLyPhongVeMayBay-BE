package com.team6.quanlyvemaybay.dto.common;

/**
 * Chi tiết lỗi cho từng trường dữ liệu theo chuẩn mở rộng RFC 9457.
 */
public record ErrorDetail(
        String field,
        String code,
        String message
) {
}
