package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XoaVeServiceTest {
    @Mock VeMayBayRepository repository;
    @InjectMocks XoaVeService service;

    @Test
    void khongTimThayVeTra404() {
        when(repository.findForCancellation(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.xoaVe(1L));
    }

    @Test
    void chiHuyVeChuaThanhToan() {
        VeMayBay ve = ve(TrangThaiVe.CHO_THANH_TOAN, TrangThaiDatCho.CHO_THANH_TOAN);
        when(repository.findForCancellation(1L)).thenReturn(Optional.of(ve));

        var result = service.xoaVe(1L);

        assertEquals(TrangThaiVe.DA_HUY, result.trangThai());
        assertEquals(TrangThaiVe.DA_HUY, ve.getTrangThai());
        verify(repository).saveAndFlush(ve);
    }

    @Test
    void khongHuyLaiVeDaHuy() {
        VeMayBay ve = ve(TrangThaiVe.DA_HUY, TrangThaiDatCho.CHO_THANH_TOAN);
        when(repository.findForCancellation(1L)).thenReturn(Optional.of(ve));
        assertThrows(BusinessException.class, () -> service.xoaVe(1L));
        verify(repository, never()).saveAndFlush(ve);
    }

    @Test
    void khongHuyVeHoacDatChoDaThanhToan() {
        VeMayBay veDaTraTien = ve(TrangThaiVe.DA_THANH_TOAN, TrangThaiDatCho.DA_THANH_TOAN);
        when(repository.findForCancellation(1L)).thenReturn(Optional.of(veDaTraTien));
        assertThrows(BusinessException.class, () -> service.xoaVe(1L));
        verify(repository, never()).saveAndFlush(veDaTraTien);

        VeMayBay veTrongDatChoDaTraTien = ve(TrangThaiVe.CHO_THANH_TOAN, TrangThaiDatCho.DA_THANH_TOAN);
        when(repository.findForCancellation(2L)).thenReturn(Optional.of(veTrongDatChoDaTraTien));
        assertThrows(BusinessException.class, () -> service.xoaVe(2L));
        verify(repository, never()).saveAndFlush(veTrongDatChoDaTraTien);
    }

    private VeMayBay ve(TrangThaiVe trangThaiVe, TrangThaiDatCho trangThaiDatCho) {
        DatCho datCho = new DatCho();
        datCho.setTrangThai(trangThaiDatCho);
        VeMayBay ve = new VeMayBay();
        ve.setId(1L);
        ve.setMaVe("VE001");
        ve.setDatCho(datCho);
        ve.setTrangThai(trangThaiVe);
        return ve;
    }
}
