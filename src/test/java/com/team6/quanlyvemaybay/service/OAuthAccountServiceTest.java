package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.LienKetOAuth;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.LienKetOAuthRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthAccountServiceTest {
    @Mock private LienKetOAuthRepository lienKetRepository;
    @Mock private NguoiDungRepository nguoiDungRepository;
    @Mock private KhachHangRepository khachHangRepository;
    private OAuthAccountService service;

    @BeforeEach
    void setUp() {
        service = new OAuthAccountService(lienKetRepository, nguoiDungRepository, khachHangRepository);
    }

    @Test
    void taoKhachHangMoiVaLienKetTheoMaDinhDanh() {
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));
        NguoiDung user = service.xacThuc("google", "123", " User@Example.com ", " Nguyễn A ");
        assertEquals("user@example.com", user.getTaiKhoanEmail());
        assertEquals("Nguyễn A", user.getHoTen());
        assertEquals(VaiTro.KHACH_HANG, user.getVaiTro());
        assertNull(user.getMatKhau());
        verify(khachHangRepository).save(any(KhachHang.class));
        verify(lienKetRepository).save(any(LienKetOAuth.class));
    }

    @Test
    void emailTrungKhongTuDongLienKet() {
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase("user@example.com")).thenReturn(true);
        assertThrows(BusinessException.class,
                () -> service.xacThuc("facebook", "123", "user@example.com", "Tên"));
        verify(lienKetRepository, never()).save(any());
        verify(nguoiDungRepository, never()).save(any());
    }

    @Test
    void lienKetCuKhongPhuThuocEmailHienTaiVaKhongChoPhepTaiKhoanBiKhoa() {
        NguoiDung user = new NguoiDung();
        when(lienKetRepository.findByNhaCungCapAndMaDinhDanh("google", "123"))
                .thenReturn(Optional.of(new LienKetOAuth(user, "google", "123")));
        assertSame(user, service.xacThuc("google", "123", null, null));
        user.setTrangThai((byte) 0);
        assertThrows(BusinessException.class, () -> service.xacThuc("google", "123", null, null));
    }

    @Test
    void tuChoiHoSoThieuEmailHoacMaDinhDanh() {
        assertThrows(BusinessException.class, () -> service.xacThuc("facebook", "123", null, "Tên"));
        assertThrows(BusinessException.class, () -> service.xacThuc("facebook", null, "a@example.com", "Tên"));
        verify(nguoiDungRepository, never()).save(any());
    }
}
