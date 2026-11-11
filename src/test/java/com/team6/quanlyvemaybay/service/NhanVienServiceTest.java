package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.nhanvien.TieuChiTimNhanVien;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.NhanVien;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NhanVienRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NhanVienServiceTest {
    @Test
    void timVaChuyenDoiNhanVien() {
        var repo = mock(NhanVienRepository.class);
        var nd = new NguoiDung();
        nd.setId(3L);
        nd.setHoTen("An");
        nd.setMatKhau("khong-duoc-tra-ve");
        var nv = new NhanVien();
        nv.setNguoiDung(nd);
        nv.setMaNhanVien("NV3");
        var pageable = PageRequest.of(0, 20);
        when(repo.timNhanVien("HO_TEN", "An", pageable)).thenReturn(new PageImpl<>(List.of(nv), pageable, 1));
        var result = new NhanVienService(repo).danhSach(TieuChiTimNhanVien.HO_TEN, " An ", 0, 20);
        assertThat(result.tongSo()).isEqualTo(1);
        assertThat(result.nhanVien().getFirst().maNhanVien()).isEqualTo("NV3");
        verify(repo).timNhanVien("HO_TEN", "An", pageable);
    }

    @Test
    void thieuTieuChiHoacTuKhoaThiBaoLoi() {
        var repo = mock(NhanVienRepository.class);
        var service = new NhanVienService(repo);
        assertThatThrownBy(() -> service.danhSach(null, "An", 0, 20)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.danhSach(TieuChiTimNhanVien.HO_TEN, " ", 0, 20)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(repo);
    }
}
