package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.PhienLamMoi;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.PhienLamMoiRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PhienLamMoiService {
    public static final Duration THOI_HAN = Duration.ofDays(7);
    private final PhienLamMoiRepository repository;
    private final NguoiDungRepository nguoiDungRepository;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public PhienLamMoiService(PhienLamMoiRepository repository, NguoiDungRepository nguoiDungRepository,
                             JwtTokenService jwtTokenService, Clock clock) {
        this.repository = repository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.jwtTokenService = jwtTokenService;
        this.clock = clock;
    }

    @Transactional
    public KetQua tao(Long nguoiDungId, boolean ghiNho) {
        return luu(nguoiDungId, UUID.randomUUID().toString(), Instant.now(clock).plus(THOI_HAN), ghiNho, null);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public KetQua lamMoi(String token) {
        PhienLamMoi phien = tim(token);
        if (phien.isDaSuDung()) {
            repository.thuHoiNhom(phien.getNhomPhien());
            throw khongHopLe();
        }
        if (phien.isDaThuHoi() || !phien.getHetHan().isAfter(Instant.now(clock))) {
            throw khongHopLe();
        }
        NguoiDung user = nguoiDungRepository.findById(phien.getNguoiDungId())
                .filter(nguoiDung -> Byte.valueOf((byte) 1).equals(nguoiDung.getTrangThai()))
                .orElseThrow(this::khongHopLe);
        phien.danhDauDaSuDung();
        return luu(phien.getNguoiDungId(), phien.getNhomPhien(), phien.getHetHan(),
                phien.isGhiNho(), jwtTokenService.createAccessToken(user));
    }

    @Transactional
    public void thuHoi(String token) {
        if (token == null || token.isBlank()) return;
        repository.findByTokenHashForUpdate(hash(token)).ifPresent(phien -> repository.thuHoiNhom(phien.getNhomPhien()));
    }

    @Transactional
    public void thuHoiNguoiDung(Long nguoiDungId) {
        repository.thuHoiNguoiDung(nguoiDungId);
    }

    private PhienLamMoi tim(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw khongHopLe();
        return repository.findByTokenHashForUpdate(hash(token)).orElseThrow(this::khongHopLe);
    }

    private KetQua luu(Long nguoiDungId, String nhom, Instant hetHan, boolean ghiNho, String accessToken) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new PhienLamMoi(nguoiDungId, nhom, hash(token), hetHan, ghiNho));
        return new KetQua(token, accessToken, hetHan, ghiNho);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Không hỗ trợ SHA-256.", exception);
        }
    }

    private BusinessException khongHopLe() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID",
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    }

    public record KetQua(String refreshToken, String accessToken, Instant hetHan, boolean ghiNho) {
    }
}
