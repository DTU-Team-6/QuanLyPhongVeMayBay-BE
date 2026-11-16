package com.team6.quanlyvemaybay.validation;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ImageUrlValidatorTest {
    private final ImageUrlValidator validator = new ImageUrlValidator();

    @Test
    void acceptsOptionalLocalAndHttpImages() {
        for (String image : Arrays.asList(null, "", "  ", "/assets/destinations/cau-rong.jpg",
                " /assets/ảnh.jpg ", "https://images.example.com/a%20b.jpg?v=1#preview", "http://localhost:8080/a.png")) {
            assertThat(validator.isValid(image, null)).as("Ảnh hợp lệ: %s", image).isTrue();
        }
    }

    @Test
    void rejectsUnsafeOrMalformedLocations() {
        for (String image : List.of("javascript:alert(1)", "data:image/png;base64,abc", "//evil.example/a.jpg",
                "/\\evil.example/a.jpg", "https://user:password@example.com/a.jpg", "https:///missing-host.jpg",
                "/a/../b.jpg", "/a/%2e%2e/b.jpg", "/a/%2E/b.jpg", "/%2fexample.com/a.jpg",
                "/a%5cb.jpg", "/a%0ab.jpg", "https://example.com:70000/a.jpg", "https://example.com/a b.jpg")) {
            assertThat(validator.isValid(image, null)).as("Ảnh không hợp lệ: %s", image).isFalse();
        }
    }
}
