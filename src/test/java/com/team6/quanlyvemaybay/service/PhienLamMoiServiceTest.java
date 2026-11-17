package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.PhienLamMoi;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.PhienLamMoiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhienLamMoiServiceTest {
    private static final String TOKEN = "A".repeat(43);
    private static final Instant NOW = Instant.parse("2026-11-17T00:00:00Z");
    @Mock private PhienLamMoiRepository repository;
    @Mock private NguoiDungRepository nguoiDungRepository;
    @Mock private JwtTokenService jwtTokenService;
    private PhienLamMoiService service;

    @BeforeEach
    void setUp() {
        service = new PhienLamMoiService(repository, nguoiDungRepository, jwtTokenService,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void rotationCreatesNewTokenWithoutExtendingOriginalExpiry() {
        PhienLamMoi old = new PhienLamMoi(42L, "family", "hash", NOW.plusSeconds(60), true);
        NguoiDung user = new NguoiDung();
        user.setId(42L);
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(old));
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(jwtTokenService.createAccessToken(user)).thenReturn("access");

        var result = service.lamMoi(TOKEN);

        assertTrue(old.isDaSuDung());
        assertNotEquals(TOKEN, result.refreshToken());
        assertEquals(NOW.plusSeconds(60), result.hetHan());
        assertEquals("access", result.accessToken());
        ArgumentCaptor<PhienLamMoi> saved = ArgumentCaptor.forClass(PhienLamMoi.class);
        verify(repository).save(saved.capture());
        assertEquals("family", saved.getValue().getNhomPhien());
        assertTrue(saved.getValue().isGhiNho());
    }

    @Test
    void reuseRevokesTheEntireTokenFamily() {
        PhienLamMoi used = new PhienLamMoi(42L, "family", "hash", NOW.plusSeconds(60), true);
        used.danhDauDaSuDung();
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(used));

        BusinessException error = assertThrows(BusinessException.class, () -> service.lamMoi(TOKEN));

        assertEquals("REFRESH_TOKEN_INVALID", error.getCode());
        verify(repository).thuHoiNhom("family");
        verify(repository, never()).save(any());
    }

    @Test
    void expiredOrMalformedRefreshTokenIsRejected() {
        PhienLamMoi expired = new PhienLamMoi(42L, "family", "hash", NOW, false);
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(expired));

        assertThrows(BusinessException.class, () -> service.lamMoi(TOKEN));
        assertThrows(BusinessException.class, () -> service.lamMoi("invalid"));
        verify(repository, never()).save(any());
    }
}
