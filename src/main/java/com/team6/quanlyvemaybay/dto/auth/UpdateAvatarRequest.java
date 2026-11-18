package com.team6.quanlyvemaybay.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateAvatarRequest(
        @NotBlank(message = "Public ID ảnh không được để trống")
        @Pattern(regexp = "^avatar_[0-9]+_[0-9a-f]{32}$", message = "Public ID ảnh không hợp lệ")
        String publicId
) {
}
