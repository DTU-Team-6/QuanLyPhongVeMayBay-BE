package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.ResetPasswordRequest;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.XacThucToken;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.XacThucTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final XacThucToken.Loai RESET = XacThucToken.Loai.QUEN_MAT_KHAU;
    private final NguoiDungRepository nguoiDungRepository;
    private final XacThucTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhienLamMoiService phienLamMoiService;
    private final Optional<JavaMailSender> mailSender;
    private final Clock clock;
    private final String mailFrom;
    private final String mailHost;
    private final String frontendUrl;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(NguoiDungRepository nguoiDungRepository, XacThucTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder, PhienLamMoiService phienLamMoiService,
                                Optional<JavaMailSender> mailSender, Clock clock,
                                @Value("${app.auth.mail-from:}") String mailFrom,
                                @Value("${spring.mail.host:}") String mailHost,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.phienLamMoiService = phienLamMoiService;
        this.mailSender = mailSender;
        this.clock = clock;
        this.mailFrom = mailFrom;
        this.mailHost = mailHost;
        this.frontendUrl = frontendUrl.split(",")[0].trim().replaceAll("/+$", "");
    }

    @Transactional
    public void requestReset(String email) {
        if (mailHost.isBlank() || mailFrom.isBlank() || mailSender.isEmpty()) {
            throw mailUnavailable();
        }
        NguoiDung user = nguoiDungRepository.findByEmailForUpdate(email.strip().toLowerCase(Locale.ROOT))
                .orElse(null);
        if (user == null || user.getMatKhau() == null || user.getMatKhau().isBlank()
                || !Byte.valueOf((byte) 1).equals(user.getTrangThai())) {
            return;
        }

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenRepository.thuHoiTheoNguoiDung(user.getId(), RESET);
        tokenRepository.saveAndFlush(new XacThucToken(user.getId(), hash(token), RESET,
                Instant.now(clock).plus(Duration.ofMinutes(15))));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getTaiKhoanEmail());
        message.setSubject("Đặt lại mật khẩu Vân Việt");
        message.setText("Bạn đã yêu cầu đặt lại mật khẩu. Liên kết chỉ dùng một lần và hết hạn sau 15 phút:\n\n"
                + frontendUrl + "/dat-lai-mat-khau#" + token
                + "\n\nNếu bạn không yêu cầu, hãy bỏ qua email này.");
        try {
            mailSender.orElseThrow(this::mailUnavailable).send(message);
        } catch (MailException exception) {
            log.error("Không thể gửi email khôi phục mật khẩu: {}", exception.getClass().getSimpleName());
            throw mailUnavailable();
        }
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_LONG",
                    "Mật khẩu không được vượt quá 72 byte khi mã hóa.");
        }
        String tokenHash = hash(request.token());
        XacThucToken initial = tokenRepository.findByTokenHash(tokenHash).orElseThrow(this::invalidToken);
        NguoiDung user = nguoiDungRepository.findByIdForUpdate(initial.getUserId()).orElseThrow(this::invalidToken);
        if (!Byte.valueOf((byte) 1).equals(user.getTrangThai())
                || user.getMatKhau() == null || user.getMatKhau().isBlank()) {
            throw invalidToken();
        }
        if (tokenRepository.suDungNeuHopLe(tokenHash, user.getId(), RESET, Instant.now(clock)) != 1) {
            throw invalidToken();
        }
        user.setMatKhau(passwordEncoder.encode(request.newPassword()));
        nguoiDungRepository.save(user);
        tokenRepository.thuHoiTheoNguoiDung(user.getId(), RESET);
        phienLamMoiService.thuHoiNguoiDung(user.getId());
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Không hỗ trợ SHA-256.", exception);
        }
    }

    private BusinessException invalidToken() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_INVALID",
                "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
    }

    private BusinessException mailUnavailable() {
        return new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "MAIL_UNAVAILABLE",
                "Không thể gửi email lúc này. Vui lòng thử lại sau.");
    }
}
