package com.team6.quanlyvemaybay.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenServiceTest {

    @Test
    void accessTokenIsSignedAndContainsExpectedIdentityAndLifetime() {
        SecretKey key = new SecretKeySpec(
                "test-secret-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        Instant now = Instant.now().minusSeconds(1);
        JwtTokenService service = new JwtTokenService(
                new NimbusJwtEncoder(new ImmutableSecret<>(key)),
                Clock.fixed(now, ZoneOffset.UTC)
        );
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        NguoiDung user = new NguoiDung();
        user.setId(42L);
        user.setTaiKhoanEmail("user@example.com");
        user.setHoTen("Nguyễn Văn A");
        user.setVaiTro(VaiTro.KHACH_HANG);

        var decoded = decoder.decode(service.createAccessToken(user));

        assertEquals("42", decoded.getSubject());
        assertEquals("user@example.com", decoded.getClaimAsString("email"));
        assertEquals("KHACH_HANG", decoded.getClaimAsString("role"));
        assertEquals(now.plus(JwtTokenService.ACCESS_TOKEN_TTL).getEpochSecond(),
                decoded.getExpiresAt().getEpochSecond());
    }
}
