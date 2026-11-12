package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.CommonInfoProperties;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.HangHangKhong;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InVePdfServiceTest {
    private final VeMayBayRepository repository = mock(VeMayBayRepository.class);
    private final CommonInfoProperties commonInfo = new CommonInfoProperties();
    private final InVePdfService service = new InVePdfService(repository, commonInfo);

    @Test
    void pdfLaySnapshotRiengVaKhongInGiaGoc() throws Exception {
        VeMayBay ve = ve();
        when(repository.findById(1L)).thenReturn(Optional.of(ve));
        commonInfo.setBrandName("Phòng vé Đà Nẵng");

        byte[] bytes = service.xuatPdf(1L);

        assertTrue(bytes.length > 1000);
        try (var pdf = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(pdf);
            assertTrue(text.contains("Phòng vé Đà Nẵng"));
            assertTrue(text.contains("Nguyễn Văn Mới"));
            assertTrue(text.contains("ve-rieng@example.com"));
            assertTrue(text.contains("ABC123"));
            assertTrue(text.contains("VN123"));
            assertFalse(text.contains("9.999.999"));
            assertFalse(text.contains("ten-goc@example.com"));
        }
    }

    @Test
    void veKhongTonTaiTra404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertEquals("TICKET_NOT_FOUND", assertThrows(ResourceNotFoundException.class,
                () -> service.xuatPdf(99L)).getCode());
    }

    @Test
    void thieuSnapshotTra409() {
        VeMayBay ve = ve();
        ve.setHoTenNguoiDi(null);
        when(repository.findById(1L)).thenReturn(Optional.of(ve));
        assertEquals("TICKET_SNAPSHOT_MISSING", assertThrows(BusinessException.class,
                () -> service.xuatPdf(1L)).getCode());
    }

    @Test
    void kyTuDieuKhienTrongDuLieuCuKhongLamHongPdf() throws Exception {
        VeMayBay ve = ve();
        ve.getHangVe().getChuyenBay().getSanBayDi().setThanhPho("Đà\u0090 Nẵng");
        when(repository.findById(1L)).thenReturn(Optional.of(ve));
        try (var pdf = Loader.loadPDF(service.xuatPdf(1L))) {
            assertTrue(new PDFTextStripper().getText(pdf).contains("Nẵng"));
        }
    }

    private VeMayBay ve() {
        DatCho datCho = new DatCho();
        datCho.setMaDatCho("ABC123");
        datCho.setEmailNguoiDat("ten-goc@example.com");

        ChuyenBay chuyenBay = new ChuyenBay();
        chuyenBay.setMaChuyenBay("VN123");
        chuyenBay.setSanBayDi(new SanBay("DAD", "Đà Nẵng", "Đà Nẵng", "Việt Nam"));
        chuyenBay.setSanBayDen(new SanBay("HAN", "Nội Bài", "Hà Nội", "Việt Nam"));
        chuyenBay.setThoiGianKhoiHanh(LocalDateTime.of(2026, 12, 1, 8, 30));
        chuyenBay.setThoiGianHaCanh(LocalDateTime.of(2026, 12, 1, 9, 50));
        chuyenBay.setHangHangKhong(new HangHangKhong("VN", "Vietnam Airlines", null, null));

        HangVeChuyenBay hangVe = new HangVeChuyenBay();
        hangVe.setChuyenBay(chuyenBay);
        hangVe.setLoaiKhoang(LoaiKhoang.PHO_THONG);
        hangVe.setHanhLyXachTayKg(7);
        hangVe.setHanhLyKyGuiKg(20);

        VeMayBay ve = new VeMayBay();
        ve.setId(1L);
        ve.setMaVe("VE001");
        ve.setDatCho(datCho);
        ve.setHangVe(hangVe);
        ve.setHoTenNguoiDi("Nguyễn Văn Mới");
        ve.setEmailThanhToan("ve-rieng@example.com");
        ve.setGiaGoc(new BigDecimal("9999999"));
        ve.setGiaBan(new BigDecimal("1000000"));
        ve.setThuePhi(new BigDecimal("100000"));
        ve.setCuocHanhLy(BigDecimal.ZERO);
        ve.setTongGia(new BigDecimal("1100000"));
        return ve;
    }
}
