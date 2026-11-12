package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.CapNhatKhachHangRequest;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.dto.khachhang.TaoKhachHangRequest;
import com.team6.quanlyvemaybay.dto.khachhang.XoaKhachHangResponse;
import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KhachHangServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private KhachHangService khachHangService;

    @BeforeEach
    void setUp() {
        khachHangService = new KhachHangService(khachHangRepository, nguoiDungRepository, passwordEncoder);
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

    @Test
    void themMoiKhongMatKhauDungGiaTriMacDinhChoKhachVangLai() {
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase("khach@example.com")).thenReturn(false);
        when(nguoiDungRepository.saveAndFlush(any(NguoiDung.class))).thenAnswer(invocation -> {
            NguoiDung nguoiDung = invocation.getArgument(0);
            nguoiDung.setId(8L);
            return nguoiDung;
        });
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KhachHangResponse result = khachHangService.themMoi(taoRequest(null));

        var nguoiDungCaptor = org.mockito.ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).saveAndFlush(nguoiDungCaptor.capture());
        assertThat(nguoiDungCaptor.getValue().getVaiTro()).isEqualTo(VaiTro.KHACH_HANG);
        assertThat(nguoiDungCaptor.getValue().getMatKhau()).isNull();
        assertThat(nguoiDungCaptor.getValue().getTaiKhoanEmail()).isEqualTo("khach@example.com");
        assertThat(result.id()).isEqualTo(8L);
        assertThat(result.gioiTinh()).isEqualTo(GioiTinh.Khac);
        assertThat(result.quocTich()).isEqualTo("Việt Nam");
        assertThat(result.xepHang()).isEqualTo("Member");
        assertThat(result.diemTichLuy()).isZero();
    }

    @Test
    void themMoiBamMatKhauNeuDuocCungCap() {
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase("khach@example.com")).thenReturn(false);
        when(passwordEncoder.encode("MatKhau123")).thenReturn("$2a$12$hash");
        when(nguoiDungRepository.saveAndFlush(any(NguoiDung.class))).thenAnswer(invocation -> {
            NguoiDung nguoiDung = invocation.getArgument(0);
            nguoiDung.setId(8L);
            return nguoiDung;
        });
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        khachHangService.themMoi(taoRequest("MatKhau123"));

        var nguoiDungCaptor = org.mockito.ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).saveAndFlush(nguoiDungCaptor.capture());
        assertThat(nguoiDungCaptor.getValue().getMatKhau()).isEqualTo("$2a$12$hash");
        verify(passwordEncoder).encode("MatKhau123");
    }

    @Test
    void themMoiEmailDaTonTaiThiTraConflict() {
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase("khach@example.com")).thenReturn(true);

        assertThatThrownBy(() -> khachHangService.themMoi(taoRequest(null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getCode()).isEqualTo("EMAIL_ALREADY_REGISTERED");
                });
    }

    @Test
    void themMoiTuChoiMatKhauVuotQuaGioiHanBcryptTheoByte() {
        String password = "ế".repeat(25);
        assertThat(password.getBytes(StandardCharsets.UTF_8)).hasSizeGreaterThan(72);

        assertThatThrownBy(() -> khachHangService.themMoi(taoRequest(password)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Mật khẩu không được vượt quá 72 byte khi mã hóa.");
    }

    @Test
    void capNhatThayTheThongTinLienHeVaHoSo() {
        KhachHang khachHang = taoKhachHang(7L, (byte) 1);
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(7L)).thenReturn(Optional.of(khachHang));
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCaseAndIdNot("moi@example.com", 7L))
                .thenReturn(false);
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(nguoiDungRepository.saveAndFlush(any(NguoiDung.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KhachHangResponse result = khachHangService.capNhat(7L, new CapNhatKhachHangRequest(
                " Moi@Example.com ",
                "Nguyễn Văn B",
                "+84901112233",
                LocalDate.of(1990, 1, 2),
                GioiTinh.Nam,
                "Hà Nội",
                "CCCD123",
                "Việt Nam",
                "Silver",
                250
        ));

        assertThat(result.email()).isEqualTo("moi@example.com");
        assertThat(result.hoTen()).isEqualTo("Nguyễn Văn B");
        assertThat(result.cmndPassport()).isEqualTo("CCCD123");
        assertThat(result.xepHang()).isEqualTo("Silver");
        assertThat(result.diemTichLuy()).isEqualTo(250);
    }

    @Test
    void capNhatEmailDaDuocTaiKhoanKhacSuDungThiTraConflict() {
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(7L))
                .thenReturn(Optional.of(taoKhachHang(7L, (byte) 1)));
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCaseAndIdNot("other@example.com", 7L))
                .thenReturn(true);

        assertThatThrownBy(() -> khachHangService.capNhat(7L, new CapNhatKhachHangRequest(
                "other@example.com",
                "Nguyễn Văn B",
                null,
                null,
                GioiTinh.Khac,
                null,
                null,
                "Việt Nam",
                "Member",
                0
        )))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void capNhatKhachHangDaNgungHoatDongThiTraConflict() {
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(7L))
                .thenReturn(Optional.of(taoKhachHang(7L, (byte) 0)));

        assertThatThrownBy(() -> khachHangService.capNhat(7L, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo("CUSTOMER_ALREADY_INACTIVE"));
    }

    @Test
    void ngungHoatDongCapNhatTrangThaiMotLan() {
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(7L))
                .thenReturn(Optional.of(taoKhachHang(7L, (byte) 1)));
        when(nguoiDungRepository.ngungHoatDongNeuDangHoatDong(7L)).thenReturn(1);

        XoaKhachHangResponse result = khachHangService.ngungHoatDong(7L);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.trangThai()).isZero();
    }

    @Test
    void ngungHoatDongChiTraConflictKhiYeuCauDongThoiDaCapNhat() {
        when(khachHangRepository.findWithNguoiDungByNguoiDungId(7L))
                .thenReturn(Optional.of(taoKhachHang(7L, (byte) 1)));
        when(nguoiDungRepository.ngungHoatDongNeuDangHoatDong(7L)).thenReturn(0);

        assertThatThrownBy(() -> khachHangService.ngungHoatDong(7L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo("CUSTOMER_ALREADY_INACTIVE"));
    }

    private TaoKhachHangRequest taoRequest(String password) {
        return new TaoKhachHangRequest(
                "  Khach@Example.com ",
                password,
                " Nguyễn Văn A ",
                "+84901234567",
                LocalDate.of(1995, 4, 20),
                null,
                " Đà Nẵng ",
                "012345678901",
                null,
                null,
                null
        );
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
