package com.team6.quanlyvemaybay.dto.flight;

/**
 * Thông tin mốc thời gian bay theo chuẩn hợp đồng API:
 * dateTime biểu diễn giờ địa phương kèm offset ISO 8601 và timeZone theo chuẩn IANA.
 */
public record FlightTimeDto(
        String dateTime,
        String timeZone
) {
}
