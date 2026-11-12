package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GioiHanTraCuuService {
    private static final int SO_LAN_TOI_DA = 10;
    private static final Duration KHOANG_THOI_GIAN = Duration.ofMinutes(15);
    private static final int SO_IP_TOI_DA = 10_000;

    private final ConcurrentHashMap<String, LuotTraCuu> luotTheoIp = new ConcurrentHashMap<>();
    private final Clock clock;

    public GioiHanTraCuuService(Clock clock) {
        this.clock = clock;
    }

    public void kiemTra(String ip) {
        Instant bayGio = clock.instant();
        if (luotTheoIp.size() >= SO_IP_TOI_DA) {
            luotTheoIp.entrySet().removeIf(entry -> !bayGio.isBefore(entry.getValue().hetHan()));
        }
        if (luotTheoIp.size() >= SO_IP_TOI_DA && !luotTheoIp.containsKey(ip)) {
            tuChoi();
        }
        LuotTraCuu luot = luotTheoIp.compute(ip, (key, hienTai) -> {
            if (hienTai == null || !bayGio.isBefore(hienTai.hetHan())) {
                return new LuotTraCuu(1, bayGio.plus(KHOANG_THOI_GIAN));
            }
            return new LuotTraCuu(Math.min(hienTai.soLan() + 1, SO_LAN_TOI_DA + 1), hienTai.hetHan());
        });
        if (luot.soLan() > SO_LAN_TOI_DA) {
            tuChoi();
        }
    }

    private void tuChoi() {
        throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "LOOKUP_RATE_LIMITED",
                "Đã vượt quá số lần tra cứu. Vui lòng thử lại sau 15 phút.");
    }

    private record LuotTraCuu(int soLan, Instant hetHan) {
    }
}
