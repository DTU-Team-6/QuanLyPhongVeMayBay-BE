package com.team6.quanlyvemaybay.dto.auth;

import com.team6.quanlyvemaybay.entity.enums.VaiTro;

public record AuthUserResponse(
        Long id,
        String email,
        String hoTen,
        VaiTro vaiTro
) {
}
