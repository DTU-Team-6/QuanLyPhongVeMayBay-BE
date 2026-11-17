package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.LienKetOAuth;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.LienKetOAuthRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class OAuthAccountService {
    private final LienKetOAuthRepository lienKetRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;

    public OAuthAccountService(LienKetOAuthRepository lienKetRepository,
            NguoiDungRepository nguoiDungRepository, KhachHangRepository khachHangRepository) {
        this.lienKetRepository = lienKetRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
    }

    @Transactional
    public NguoiDung xacThuc(String nhaCungCap, String maDinhDanh, String email, String hoTen) {
        if (nhaCungCap == null || !Set.of("google", "facebook").contains(nhaCungCap)
                || maDinhDanh == null || !maDinhDanh.matches("[\\x21-\\x7E]{1,255}")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OAUTH_PROFILE_INVALID",
                    "Không nhận được mã định danh hợp lệ từ nhà cung cấp.");
        }

        var lienKet = lienKetRepository.findByNhaCungCapAndMaDinhDanh(nhaCungCap, maDinhDanh);
        if (lienKet.isPresent()) {
            NguoiDung nguoiDung = lienKet.get().getNguoiDung();
            if (!Byte.valueOf((byte) 1).equals(nguoiDung.getTrangThai())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
                        "Tài khoản đã bị khóa hoặc chưa được kích hoạt.");
            }
            return nguoiDung;
        }

        if (email == null || email.isBlank() || email.length() > 100
                || hoTen == null || hoTen.isBlank() || hoTen.strip().length() > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OAUTH_PROFILE_INVALID",
                    "Nhà cung cấp chưa trả email hoặc họ tên hợp lệ.");
        }
        String emailChuan = email.strip().toLowerCase(Locale.ROOT);
        if (nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase(emailChuan)) {
            throw new BusinessException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                    "Email đã được đăng ký. Vui lòng sử dụng cách đăng nhập cũ.");
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setTaiKhoanEmail(emailChuan);
        nguoiDung.setHoTen(hoTen.strip());
        nguoiDung.setVaiTro(VaiTro.KHACH_HANG);
        nguoiDung = nguoiDungRepository.save(nguoiDung);

        KhachHang khachHang = new KhachHang();
        khachHang.setNguoiDung(nguoiDung);
        khachHangRepository.save(khachHang);
        lienKetRepository.save(new LienKetOAuth(nguoiDung, nhaCungCap, maDinhDanh));
        return nguoiDung;
    }
}
