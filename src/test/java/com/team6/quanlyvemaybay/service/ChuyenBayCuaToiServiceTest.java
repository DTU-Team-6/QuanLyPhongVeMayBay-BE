package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.flight.TraCuuChuyenBayCuaToiRequest;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChuyenBayCuaToiServiceTest {
    private final DatChoRepository datChoRepository = mock(DatChoRepository.class);
    private final VeMayBayRepository veMayBayRepository = mock(VeMayBayRepository.class);
    private final GioiHanTraCuuService gioiHan = mock(GioiHanTraCuuService.class);
    private final ChuyenBayCuaToiService service = new ChuyenBayCuaToiService(
            datChoRepository, veMayBayRepository, gioiHan);

    @Test
    void maEmailVaSoDienThoaiSaiTraCungMotLoi() {
        DatCho datCho = datCho();
        when(datChoRepository.findByMaDatCho("ABC123")).thenReturn(Optional.of(datCho));
        when(datChoRepository.findByMaDatCho("SAI123")).thenReturn(Optional.empty());

        ResourceNotFoundException saiMa = assertThrows(ResourceNotFoundException.class,
                () -> service.traCuu(new TraCuuChuyenBayCuaToiRequest(
                        "SAI123", "nguoidat@example.com", "0912345678"), "127.0.0.1"));
        ResourceNotFoundException saiEmail = assertThrows(ResourceNotFoundException.class,
                () -> service.traCuu(new TraCuuChuyenBayCuaToiRequest(
                        "ABC123", "sai@example.com", "0912345678"), "127.0.0.1"));
        ResourceNotFoundException saiSo = assertThrows(ResourceNotFoundException.class,
                () -> service.traCuu(new TraCuuChuyenBayCuaToiRequest(
                        "ABC123", "nguoidat@example.com", "0900000000"), "127.0.0.1"));

        assertEquals(saiMa.getCode(), saiEmail.getCode());
        assertEquals(saiMa.getCode(), saiSo.getCode());
        assertEquals(saiMa.getMessage(), saiEmail.getMessage());
        assertEquals(saiMa.getMessage(), saiSo.getMessage());
    }

    @Test
    void dungBaThongTinCuaCungDatChoTraDanhSachChuyenBay() {
        DatCho datCho = datCho();
        when(datChoRepository.findByMaDatCho("ABC123")).thenReturn(Optional.of(datCho));
        when(veMayBayRepository.findByDatChoId(1L)).thenReturn(List.of());

        var result = service.traCuu(new TraCuuChuyenBayCuaToiRequest(
                " abc123 ", "NGUOIDAT@example.com", "0912 345 678"), "127.0.0.1");

        assertEquals(List.of(), result.chuyenBay());
    }

    private DatCho datCho() {
        DatCho datCho = new DatCho();
        datCho.setId(1L);
        datCho.setMaDatCho("ABC123");
        datCho.setEmailNguoiDat("nguoidat@example.com");
        datCho.setSoDienThoaiNguoiDat("0912345678");
        return datCho;
    }
}
