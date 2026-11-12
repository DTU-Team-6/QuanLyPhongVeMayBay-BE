package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.ticket.TieuChiTimVe;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.HanhKhachRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeMayBayServiceTest {
    @Mock VeMayBayRepository veMayBayRepository;
    @Mock HanhKhachRepository hanhKhachRepository;
    @InjectMocks VeMayBayService service;

    @Test
    void traTrangRongKhiKhongCoVe() {
        when(veMayBayRepository.timDanhSachVe(TrangThaiVe.DA_HUY, "", "", PageRequest.of(1, 10)))
                .thenReturn(Page.empty(PageRequest.of(1, 10)));

        var response = service.danhSachVe(TrangThaiVe.DA_HUY, null, null, 1, 10);

        assertEquals(0, response.content().size());
        verify(veMayBayRepository).timDanhSachVe(TrangThaiVe.DA_HUY, "", "", PageRequest.of(1, 10));
    }

    @Test
    void timKiemCanDongThoiTieuChiVaTuKhoa() {
        assertThrows(BusinessException.class,
                () -> service.danhSachVe(null, TieuChiTimVe.MA_VE, null, 0, 20));
        assertThrows(BusinessException.class,
                () -> service.danhSachVe(null, null, "VE001", 0, 20));
    }

    @Test
    void chuyenBoLocVaTuKhoaDaCatKhoangTrang() {
        when(veMayBayRepository.timDanhSachVe(TrangThaiVe.DA_THANH_TOAN, "MA_VE", "VE001",
                PageRequest.of(0, 20))).thenReturn(Page.empty(PageRequest.of(0, 20)));

        service.danhSachVe(TrangThaiVe.DA_THANH_TOAN, TieuChiTimVe.MA_VE, "  VE001  ", 0, 20);

        verify(veMayBayRepository).timDanhSachVe(TrangThaiVe.DA_THANH_TOAN, "MA_VE", "VE001",
                PageRequest.of(0, 20));
    }
}
