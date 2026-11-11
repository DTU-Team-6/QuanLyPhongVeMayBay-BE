package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KhachHangServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    private KhachHangService khachHangService;

    @BeforeEach
    void setUp() {
        khachHangService = new KhachHangService(khachHangRepository);
    }

    @Test
    void layDanhSachChuanHoaTuKhoaVaTraHoSoKhachHang() {
        KhachHang khachHang = taoKhachHang(7L, (byte) 0);
        Page<KhachHang> page = new PageImpl<>(List.of(khachHang), PageRequest.of(0, 10), 1);
        when(khachHangRepository.findWithFilters("nguyen van", PageRequest.of(0, 10))).thenReturn(page);

        PageResponse<KhachHangResponse> result = khachHangService.layDanhSach("  nguyen van  ", 0, 10);

        verify(khachHangRepository).findWithFilters("nguyen van", PageRequest.of(0, 10));
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(7L);
        assertThat(result.content().getFirst().email()).isEqualTo("khach@example.com");
        assertThat(result.content().getFirst().cmndPassport()).isEqualTo("012345678901");
        assertThat(result.content().getFirst().trangThai()).isZero();
    }

    @Test
    void layChiTietTraKhachHangTimThay() {
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(7L))
                .thenReturn(Optional.of(taoKhachHang(7L, (byte) 1)));

        KhachHangResponse result = khachHangService.layChiTiet(7L);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.hoTen()).isEqualTo("Nguyễn Văn A");
        assertThat(result.ngaySinh()).isEqualTo(LocalDate.of(1995, 4, 20));
        assertThat(result.gioiTinh()).isEqualTo(GioiTinh.Nam);
    }

    @Test
    void layChiTietKhongTimThayThiBaoLoi404() {
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> khachHangService.layChiTiet(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Không tìm thấy khách hàng.");
    }

    private KhachHang taoKhachHang(Long id, Byte trangThai) {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(id);
        nguoiDung.setTaiKhoanEmail("khach@example.com");
        nguoiDung.setHoTen("Nguyễn Văn A");
        nguoiDung.setSoDienThoai("+84901234567");
        nguoiDung.setNgaySinh(LocalDate.of(1995, 4, 20));
        nguoiDung.setGioiTinh(GioiTinh.Nam);
        nguoiDung.setDiaChi("Đà Nẵng");
        nguoiDung.setTrangThai(trangThai);

        KhachHang khachHang = new KhachHang();
        khachHang.setNguoiDungId(id);
        khachHang.setNguoiDung(nguoiDung);
        khachHang.setCmndPassport("012345678901");
        khachHang.setQuocTich("Việt Nam");
        khachHang.setXepHang("Member");
        khachHang.setDiemTichLuy(10);
        return khachHang;
    }
}
