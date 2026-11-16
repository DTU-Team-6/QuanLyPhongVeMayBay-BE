package com.team6.quanlyvemaybay.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/** Kiểm tra cú pháp liên kết ảnh, không tải tài nguyên từ URL do người dùng nhập. */
public class ImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String image = value.trim();
        if (image.contains("\\") || image.codePoints().anyMatch(codePoint ->
                Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint) || Character.isISOControl(codePoint))) {
            return false;
        }
        try {
            URI uri = new URI(image);
            String path = uri.getPath();
            if (path != null && (path.contains("\\") || path.startsWith("//")
                    || path.codePoints().anyMatch(Character::isISOControl)
                    || Arrays.stream(path.split("/")).anyMatch(segment -> segment.equals(".") || segment.equals("..")))) {
                return false;
            }
            if (image.startsWith("/")) {
                return !image.startsWith("//") && uri.getRawAuthority() == null && uri.getScheme() == null;
            }
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null && uri.getPort() <= 65535;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
