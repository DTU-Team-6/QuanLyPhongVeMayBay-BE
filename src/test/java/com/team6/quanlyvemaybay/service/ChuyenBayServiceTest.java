package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.flight.TopCheapFlightResponse;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.HangHangKhong;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiHang;
import com.team6.quanlyvemaybay.repository.HangVeChuyenBayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử đơn vị cho ChuyenBayService: lọc trùng chuyến bay, định dạng thời gian và dữ liệu rỗng.
 */
@ExtendWith(MockitoExtension.class)
class ChuyenBayServiceTest {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private HangVeChuyenBayRepository hangVeChuyenBayRepository;

    private ChuyenBayService chuyenBayService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-10T03:00:00Z"), VN_ZONE);
        chuyenBayService = new ChuyenBayService(hangVeChuyenBayRepository, fixedClock);
    }

    @Test
    void getTopCheapFlights_shouldReturnDeduplicatedFlightsWithOffsetTime() {
        HangHangKhong vn = new HangHangKhong("VN", "Vietnam Airlines", "/assets/vn.png", TrangThaiHang.HOAT_DONG);
        SanBay sgn = new SanBay("SGN", "Tân Sơn Nhất", "TP Hồ Chí Minh", "Việt Nam");
        SanBay han = new SanBay("HAN", "Nội Bài", "Hà Nội", "Việt Nam");

        ChuyenBay cb1 = new ChuyenBay();
        cb1.setId(1L);
        cb1.setMaChuyenBay("VN236");
        cb1.setHangHangKhong(vn);
        cb1.setSanBayDi(sgn);
        cb1.setSanBayDen(han);
        cb1.setThoiGianKhoiHanh(LocalDateTime.of(2026, 11, 15, 5, 0));
        cb1.setThoiGianHaCanh(LocalDateTime.of(2026, 11, 15, 7, 10));
        cb1.setTrangThai(TrangThaiChuyenBay.DUNG_GIO);

        HangVeChuyenBay hv1 = new HangVeChuyenBay();
        hv1.setId(10L);
        hv1.setChuyenBay(cb1);
        hv1.setLoaiKhoang(LoaiKhoang.PHO_THONG);
        hv1.setGiaGoc(new BigDecimal("900000.00"));
        hv1.setGiaBan(new BigDecimal("1109000.00"));
        hv1.setThuePhi(new BigDecimal("681000.00"));
        hv1.setHanhLyXachTayKg(10);
        hv1.setHanhLyKyGuiKg(23);
        hv1.setSoGheTrong(145);

        // Bản ghi trùng chuyến bay 1 (ví dụ 2 hạng vé đồng giá) phải bị loại bỏ khi deduplicate
        HangVeChuyenBay hv1Duplicate = new HangVeChuyenBay();
        hv1Duplicate.setId(11L);
        hv1Duplicate.setChuyenBay(cb1);
        hv1Duplicate.setLoaiKhoang(LoaiKhoang.THUONG_GIA);
        hv1Duplicate.setGiaGoc(new BigDecimal("900000.00"));
        hv1Duplicate.setGiaBan(new BigDecimal("1109000.00"));
        hv1Duplicate.setThuePhi(new BigDecimal("681000.00"));
        hv1Duplicate.setSoGheTrong(10);

        when(hangVeChuyenBayRepository.findTopCheapestAvailableFlights(
                eq(TrangThaiChuyenBay.DA_HUY),
                eq(TrangThaiHang.HOAT_DONG),
                eq(LocalDateTime.of(2026, 11, 10, 10, 0)),
                any(Pageable.class)
        )).thenReturn(List.of(hv1, hv1Duplicate));

        List<TopCheapFlightResponse> result = chuyenBayService.getTopCheapFlights(10);

        assertThat(result).hasSize(1);
        TopCheapFlightResponse flight = result.getFirst();
        assertThat(flight.chuyenBayId()).isEqualTo(1L);
        assertThat(flight.maChuyenBay()).isEqualTo("VN236");
        assertThat(flight.tongGiaVe()).isEqualByComparingTo("1790000.00");
        assertThat(flight.tienTe()).isEqualTo("VND");
        assertThat(flight.thoiLuongBayPhut()).isEqualTo(130L);
        assertThat(flight.thoiGianKhoiHanh().dateTime()).isEqualTo("2026-11-15T05:00:00+07:00");
        assertThat(flight.thoiGianKhoiHanh().timeZone()).isEqualTo("Asia/Ho_Chi_Minh");

        verify(hangVeChuyenBayRepository).findTopCheapestAvailableFlights(
                eq(TrangThaiChuyenBay.DA_HUY),
                eq(TrangThaiHang.HOAT_DONG),
                eq(LocalDateTime.of(2026, 11, 10, 10, 0)),
                any(Pageable.class)
        );
    }

    @Test
    void getTopCheapFlights_shouldReturnEmptyListWhenNoFlightsAvailable() {
        when(hangVeChuyenBayRepository.findTopCheapestAvailableFlights(
                any(), any(), any(), any(Pageable.class)
        )).thenReturn(List.of());

        List<TopCheapFlightResponse> result = chuyenBayService.getTopCheapFlights(10);

        assertThat(result).isEmpty();
    }
}
