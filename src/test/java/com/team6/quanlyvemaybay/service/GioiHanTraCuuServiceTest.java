package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GioiHanTraCuuServiceTest {
    @Test
    void lanThuMuoiMotCungIpBiGioiHan() {
        GioiHanTraCuuService service = new GioiHanTraCuuService(
                Clock.fixed(Instant.parse("2026-11-12T05:00:00Z"), ZoneOffset.UTC));
        for (int i = 0; i < 10; i++) {
            service.kiemTra("127.0.0.1");
        }
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.kiemTra("127.0.0.1"));
        assertEquals("LOOKUP_RATE_LIMITED", ex.getCode());
        assertEquals(429, ex.getStatus().value());
        service.kiemTra("127.0.0.2");
    }
}
