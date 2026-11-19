package com.team6.quanlyvemaybay.dto.auth;

public record AvatarUploadSignatureResponse(
        String cloudName,
        String apiKey,
        String uploadUrl,
        long timestamp,
        String publicId,
        String signature,
        String uploadPreset,
        String allowedFormats,
        long maxFileSize
) {
}
