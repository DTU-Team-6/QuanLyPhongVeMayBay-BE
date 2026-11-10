package com.team6.quanlyvemaybay.dto.publicinfo;

/**
 * Thông tin liên hệ phòng vé hiển thị trên Header/Footer.
 */
public record ContactInfoDto(
        String hotline,
        String supportPhone,
        String email,
        String address,
        String workingHours
) {
}
