package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.ticket.SuaVeRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuaVeServiceTest {
    @Mock VeMayBayRepository repository;
    @InjectMocks SuaVeService service;

    private final SuaVeRequest request = new SuaVeRequest("Nguyen Van Moi", "ve-moi@example.com");

    @Test
    void chiSuaSnapshotCuaVeDuocChon() {
        DatCho datCho = new DatCho();
        datCho.setTrangThai(TrangThaiDatCho.CHO_THANH_TOAN);
        datCho.setEmailNguoiDat("don-hang@example.com");
        VeMayBay ve = ve(1L, datCho, TrangThaiVe.CHO_THANH_TOAN);
        VeMayBay veKhac = ve(2L, datCho, TrangThaiVe.CHO_THANH_TOAN);
        when(repository.findForTicketUpdate(1L)).thenReturn(Optional.of(ve));

        var result = service.suaVe(1L, request);

        assertEquals("Nguyen Van Moi", result.hoTenNguoiDi());
        assertEquals("ve-moi@example.com", result.emailThanhToan());
        assertEquals("Ten ve cu", veKhac.getHoTenNguoiDi());
        assertEquals("ve-cu@example.com", veKhac.getEmailThanhToan());
        assertEquals("don-hang@example.com", datCho.getEmailNguoiDat());
        verify(repository).saveAndFlush(ve);
    }

    @Test
    void veKhongTonTaiTra404() {
        when(repository.findForTicketUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.suaVe(9L, request));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void tuChoiVeDaThanhToanHoacDaHuy() {
        for (TrangThaiVe trangThai : new TrangThaiVe[]{TrangThaiVe.DA_THANH_TOAN, TrangThaiVe.DA_HUY}) {
            VeMayBay ve = ve(1L, datCho(TrangThaiDatCho.CHO_THANH_TOAN), trangThai);
            when(repository.findForTicketUpdate(1L)).thenReturn(Optional.of(ve));
            BusinessException ex = assertThrows(BusinessException.class, () -> service.suaVe(1L, request));
            assertEquals("TICKET_NOT_EDITABLE", ex.getCode());
        }
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void tuChoiDatChoDaThanhToanHoacDaHuy() {
        for (TrangThaiDatCho trangThai : new TrangThaiDatCho[]{TrangThaiDatCho.DA_THANH_TOAN, TrangThaiDatCho.DA_HUY}) {
            VeMayBay ve = ve(1L, datCho(trangThai), TrangThaiVe.CHO_THANH_TOAN);
            when(repository.findForTicketUpdate(1L)).thenReturn(Optional.of(ve));
            BusinessException ex = assertThrows(BusinessException.class, () -> service.suaVe(1L, request));
            assertEquals("TICKET_NOT_EDITABLE", ex.getCode());
        }
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void tenSauKhiCatKhoangTrangQuaNganBiTuChoi() {
        VeMayBay ve = ve(1L, datCho(TrangThaiDatCho.CHO_THANH_TOAN), TrangThaiVe.CHO_THANH_TOAN);
        when(repository.findForTicketUpdate(1L)).thenReturn(Optional.of(ve));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.suaVe(1L, new SuaVeRequest("      Minh      ", "ve-moi@example.com")));
        assertEquals("INVALID_TICKET_INFO", ex.getCode());
        verify(repository, never()).saveAndFlush(any());
    }

    private DatCho datCho(TrangThaiDatCho trangThai) {
        DatCho datCho = new DatCho();
        datCho.setTrangThai(trangThai);
        return datCho;
    }

    private VeMayBay ve(Long id, DatCho datCho, TrangThaiVe trangThai) {
        VeMayBay ve = new VeMayBay();
        ve.setId(id);
        ve.setMaVe("VE" + id);
        ve.setDatCho(datCho);
        ve.setTrangThai(trangThai);
        ve.setHoTenNguoiDi("Ten ve cu");
        ve.setEmailThanhToan("ve-cu@example.com");
        return ve;
    }
}
