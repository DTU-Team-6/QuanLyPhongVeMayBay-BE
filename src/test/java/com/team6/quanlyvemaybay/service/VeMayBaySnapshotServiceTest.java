package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeMayBaySnapshotServiceTest {
    @Mock VeMayBayRepository repository;
    @InjectMocks VeMayBayService service;

    @Test
    void danhSachHienThiTenSnapshotCuaTungVe() {
        DatCho datCho = new DatCho();
        datCho.setMaDatCho("PNR001");
        SanBay sanBayDi = new SanBay();
        sanBayDi.setMaSanBay("DAD");
        SanBay sanBayDen = new SanBay();
        sanBayDen.setMaSanBay("SGN");
        ChuyenBay chuyenBay = new ChuyenBay();
        chuyenBay.setMaChuyenBay("VN001");
        chuyenBay.setSanBayDi(sanBayDi);
        chuyenBay.setSanBayDen(sanBayDen);
        chuyenBay.setThoiGianKhoiHanh(LocalDateTime.of(2026, 12, 1, 8, 0));
        HangVeChuyenBay hangVe = new HangVeChuyenBay();
        hangVe.setChuyenBay(chuyenBay);
        VeMayBay ve = new VeMayBay();
        ve.setId(1L);
        ve.setMaVe("VE001");
        ve.setDatCho(datCho);
        ve.setHanhKhachId(7L);
        ve.setHangVe(hangVe);
        ve.setHoTenNguoiDi("Tên riêng của vé");
        ve.setTongGia(new BigDecimal("900000"));
        ve.setTrangThai(TrangThaiVe.CHO_THANH_TOAN);
        when(repository.timDanhSachVe(null, "", "", PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(ve), PageRequest.of(0, 20), 1));

        var result = service.danhSachVe(null, null, null, 0, 20);

        assertEquals("Tên riêng của vé", result.content().getFirst().tenNguoiDi());
    }
}
