package com.team6.quanlyvemaybay.dto.chat;

import com.team6.quanlyvemaybay.entity.enums.VaiTro;

import java.time.Instant;

public record TinNhanChatResponse(
        Long id,
        Long nguoiGuiId,
        VaiTro nguoiGuiVaiTro,
        Long nguoiNhanId,
        String noiDung,
        Instant guiLuc
) {
}
