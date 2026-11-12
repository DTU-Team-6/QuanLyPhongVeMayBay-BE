package com.team6.quanlyvemaybay.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuiTinNhanRequest(
        @NotBlank(message = "Nội dung tin nhắn không được để trống")
        @Size(max = 2000, message = "Nội dung tin nhắn không vượt quá 2000 ký tự")
        String noiDung
) {
}
