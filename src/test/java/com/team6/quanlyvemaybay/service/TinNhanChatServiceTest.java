package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.chat.GuiTinNhanRequest;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.TinNhanChat;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.TinNhanChatRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TinNhanChatServiceTest {
    private final TinNhanChatRepository tinNhanRepository = mock(TinNhanChatRepository.class);
    private final NguoiDungRepository nguoiDungRepository = mock(NguoiDungRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T05:00:00Z"), ZoneOffset.UTC);
    private final TinNhanChatService service = new TinNhanChatService(tinNhanRepository, nguoiDungRepository, clock);

    @Test
    void khachGuiTinNhanDenAdminDangHoatDong() {
        NguoiDung khach = nguoiDung(3L, VaiTro.KHACH_HANG);
        NguoiDung admin = nguoiDung(1L, VaiTro.ADMIN);
        when(nguoiDungRepository.findById(3L)).thenReturn(Optional.of(khach));
        when(nguoiDungRepository.findFirstByVaiTroAndTrangThaiOrderByIdAsc(VaiTro.ADMIN, (byte) 1))
                .thenReturn(Optional.of(admin));
        when(tinNhanRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            TinNhanChat saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        var result = service.khachGui(3L, new GuiTinNhanRequest("  Chào Admin  "));

        assertEquals(3L, result.nguoiGuiId());
        assertEquals(1L, result.nguoiNhanId());
        assertEquals("Chào Admin", result.noiDung());
        assertEquals(Instant.parse("2026-11-12T05:00:00Z"), result.guiLuc());
    }

    @Test
    void khachKhongDocDuocDuongDanAdmin() {
        when(nguoiDungRepository.findById(3L)).thenReturn(Optional.of(nguoiDung(3L, VaiTro.KHACH_HANG)));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.lichSuChoAdmin(3L, 4L, 0, 20));
        assertEquals("ACCESS_DENIED", ex.getCode());
        verify(tinNhanRepository, never()).lichSu(any(), any(), any());
    }

    @Test
    void nhanVienKhongDuocGuiTinVoiVaiTroKhach() {
        when(nguoiDungRepository.findById(2L)).thenReturn(Optional.of(nguoiDung(2L, VaiTro.NHAN_VIEN)));
        assertEquals("ACCESS_DENIED", assertThrows(BusinessException.class,
                () -> service.khachGui(2L, new GuiTinNhanRequest("Chào"))).getCode());
    }

    @Test
    void adminKhongTraLoiChoTaiKhoanKhongPhaiKhachHang() {
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(nguoiDung(1L, VaiTro.ADMIN)));
        when(nguoiDungRepository.findById(2L)).thenReturn(Optional.of(nguoiDung(2L, VaiTro.NHAN_VIEN)));
        assertEquals("CUSTOMER_NOT_FOUND", assertThrows(ResourceNotFoundException.class,
                () -> service.adminTraLoi(1L, 2L, new GuiTinNhanRequest("Chào"))).getCode());
        verify(tinNhanRepository, never()).saveAndFlush(any());
    }

    private NguoiDung nguoiDung(Long id, VaiTro vaiTro) {
        NguoiDung user = new NguoiDung();
        user.setId(id);
        user.setVaiTro(vaiTro);
        user.setTrangThai((byte) 1);
        return user;
    }
}
