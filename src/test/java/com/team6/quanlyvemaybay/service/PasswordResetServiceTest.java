package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.ResetPasswordRequest;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.XacThucToken;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.XacThucTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-19T00:00:00Z");
    private static final String TOKEN = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    @Mock private NguoiDungRepository users;
    @Mock private XacThucTokenRepository tokens;
    @Mock private PasswordEncoder encoder;
    @Mock private PhienLamMoiService sessions;
    @Mock private JavaMailSender mail;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, tokens, encoder, sessions, Optional.of(mail),
                Clock.fixed(NOW, ZoneOffset.UTC), "sender@example.com", "smtp.example.com",
                "https://example.com");
    }

    @Test
    void requestStoresHashAndSendsExpiringLink() throws Exception {
        NguoiDung user = user();
        when(users.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user));
        service.requestReset(" User@example.com ");

        ArgumentCaptor<XacThucToken> stored = ArgumentCaptor.forClass(XacThucToken.class);
        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(tokens).saveAndFlush(stored.capture());
        verify(mail).send(sent.capture());
        String text = sent.getValue().getText();
        String token = text.substring(text.indexOf('#') + 1).split("\\s")[0];
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        assertEquals(hash, stored.getValue().getTokenHash());
        assertEquals(NOW.plusSeconds(900), stored.getValue().getHetHan());
        assertTrue(text.contains("https://example.com/dat-lai-mat-khau#" + token));
        verify(tokens).thuHoiTheoNguoiDung(42L, XacThucToken.Loai.QUEN_MAT_KHAU);
    }

    @Test
    void nonexistentAndOAuthOnlyUsersAreNotSentMail() {
        when(users.findByEmailForUpdate("missing@example.com")).thenReturn(Optional.empty());
        NguoiDung oauth = user();
        oauth.setMatKhau(null);
        when(users.findByEmailForUpdate("oauth@example.com")).thenReturn(Optional.of(oauth));
        service.requestReset("missing@example.com");
        service.requestReset("oauth@example.com");
        verifyNoInteractions(tokens, mail);
    }

    @Test
    void mailFailureIsReported() {
        when(users.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user()));
        doThrow(new MailSendException("failed")).when(mail).send(any(SimpleMailMessage.class));
        BusinessException error = assertThrows(BusinessException.class, () -> service.requestReset("user@example.com"));
        assertEquals("MAIL_UNAVAILABLE", error.getCode());
    }

    @Test
    void validTokenChangesPasswordAndRevokesSessionsAndOtherLinks() {
        NguoiDung user = user();
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(
                new XacThucToken(42L, "hash", XacThucToken.Loai.QUEN_MAT_KHAU, NOW.plusSeconds(60))));
        when(users.findByIdForUpdate(42L)).thenReturn(Optional.of(user));
        when(tokens.suDungNeuHopLe(anyString(), eq(42L), eq(XacThucToken.Loai.QUEN_MAT_KHAU), eq(NOW))).thenReturn(1);
        when(encoder.encode("Newpass123")).thenReturn("new-hash");
        service.reset(new ResetPasswordRequest(TOKEN, "Newpass123"));
        assertEquals("new-hash", user.getMatKhau());
        verify(tokens).thuHoiTheoNguoiDung(42L, XacThucToken.Loai.QUEN_MAT_KHAU);
        verify(sessions).thuHoiNguoiDung(42L);
    }

    @Test
    void consumedOrExpiredTokenCannotChangePassword() {
        NguoiDung user = user();
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(
                new XacThucToken(42L, "hash", XacThucToken.Loai.QUEN_MAT_KHAU, NOW)));
        when(users.findByIdForUpdate(42L)).thenReturn(Optional.of(user));
        when(tokens.suDungNeuHopLe(anyString(), eq(42L), eq(XacThucToken.Loai.QUEN_MAT_KHAU), eq(NOW))).thenReturn(0);
        BusinessException error = assertThrows(BusinessException.class,
                () -> service.reset(new ResetPasswordRequest(TOKEN, "Newpass123")));
        assertEquals("PASSWORD_RESET_INVALID", error.getCode());
        verify(users, never()).save(any());
        verifyNoInteractions(sessions);
    }

    private NguoiDung user() {
        NguoiDung user = new NguoiDung();
        user.setId(42L);
        user.setTaiKhoanEmail("user@example.com");
        user.setMatKhau("old-hash");
        user.setTrangThai((byte) 1);
        return user;
    }
}
