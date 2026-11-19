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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void search_shouldKeepDelayedFlightFromTopCheapAndExcludeCanceledStatus() {
        HangHangKhong airline = new HangHangKhong("VN", "Vietnam Airlines", null, TrangThaiHang.HOAT_DONG);
        ChuyenBay delayedFlight = new ChuyenBay();
        delayedFlight.setId(12L);
        delayedFlight.setMaChuyenBay("VN120");
        delayedFlight.setHangHangKhong(airline);
        delayedFlight.setSanBayDi(new SanBay("SGN", "Tân Sơn Nhất", "TP Hồ Chí Minh", "Việt Nam"));
        delayedFlight.setSanBayDen(new SanBay("HAN", "Nội Bài", "Hà Nội", "Việt Nam"));
        delayedFlight.setThoiGianKhoiHanh(LocalDateTime.of(2026, 11, 15, 14, 0));
        delayedFlight.setThoiGianHaCanh(LocalDateTime.of(2026, 11, 15, 16, 0));
        delayedFlight.setTrangThai(TrangThaiChuyenBay.DELAY);
        HangVeChuyenBay fare = new HangVeChuyenBay();
        fare.setId(24L);
        fare.setChuyenBay(delayedFlight);
        fare.setLoaiKhoang(LoaiKhoang.PHO_THONG);
        fare.setGiaBan(new BigDecimal("1000000"));
        fare.setThuePhi(new BigDecimal("200000"));
        fare.setSoGheTrong(20);
        when(hangVeChuyenBayRepository.findTopCheapestAvailableFlights(
                eq(TrangThaiChuyenBay.DA_HUY), eq(TrangThaiHang.HOAT_DONG), any(), any(Pageable.class)
        )).thenReturn(List.of(fare));
        when(hangVeChuyenBayRepository.findForSearch(
                "SGN", "HAN", LocalDateTime.of(2026, 11, 15, 0, 0), LocalDateTime.of(2026, 11, 16, 0, 0),
                TrangThaiChuyenBay.DA_HUY, TrangThaiHang.HOAT_DONG, 1, null
        )).thenReturn(List.of(fare));

        var topFlights = chuyenBayService.getTopCheapFlights(10);
        var searchResults = chuyenBayService.search("SGN", "HAN", java.time.LocalDate.of(2026, 11, 15), 1, 0, null);

        assertThat(topFlights).hasSize(1);
        assertThat(topFlights.getFirst().trangThai()).isEqualTo(TrangThaiChuyenBay.DELAY);
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.getFirst().chuyenBayId()).isEqualTo(topFlights.getFirst().chuyenBayId());
        assertThat(searchResults.getFirst().tongGiaVe()).isEqualByComparingTo(topFlights.getFirst().tongGiaVe());
        verify(hangVeChuyenBayRepository).findForSearch(
                "SGN", "HAN", LocalDateTime.of(2026, 11, 15, 0, 0), LocalDateTime.of(2026, 11, 16, 0, 0),
                TrangThaiChuyenBay.DA_HUY, TrangThaiHang.HOAT_DONG, 1, null
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

    @Test
    void getMinPriceByDateRange_shouldReturnLowestAvailableFareForEachFutureDate() {
        HangVeChuyenBay expensiveNov11 = fare(30L, LocalDateTime.of(2026, 11, 11, 8, 0), "900000", "150000");
        HangVeChuyenBay cheapNov11 = fare(31L, LocalDateTime.of(2026, 11, 11, 12, 0), "700000", "200000");
        HangVeChuyenBay fareOnNov12 = fare(32L, LocalDateTime.of(2026, 11, 12, 9, 0), "800000", "100000");
        HangVeChuyenBay alreadyDeparted = fare(33L, LocalDateTime.of(2026, 11, 10, 8, 30), "100000", "100000");
        when(hangVeChuyenBayRepository.findAvailableFaresForDateRange(
                "HPH", "SGN",
                LocalDateTime.of(2026, 11, 10, 0, 0), LocalDateTime.of(2026, 11, 13, 0, 0),
                TrangThaiChuyenBay.DA_HUY, TrangThaiHang.HOAT_DONG, 2
        )).thenReturn(List.of(expensiveNov11, cheapNov11, fareOnNov12, alreadyDeparted));

        var result = chuyenBayService.getMinPriceByDateRange(
                "hph", "sgn", java.time.LocalDate.of(2026, 11, 10), java.time.LocalDate.of(2026, 11, 12), 1, 1
        );

        assertThat(result).containsOnlyKeys("2026-11-11", "2026-11-12")
                .containsEntry("2026-11-11", new BigDecimal("900000"))
                .containsEntry("2026-11-12", new BigDecimal("900000"));
        verify(hangVeChuyenBayRepository).findAvailableFaresForDateRange(
                "HPH", "SGN",
                LocalDateTime.of(2026, 11, 10, 0, 0), LocalDateTime.of(2026, 11, 13, 0, 0),
                TrangThaiChuyenBay.DA_HUY, TrangThaiHang.HOAT_DONG, 2
        );
    }

    @Test
    void getMinPriceByDateRange_shouldRejectInvalidRoutesAndRanges() {
        assertThatThrownBy(() -> chuyenBayService.getMinPriceByDateRange(
                "HPH", "HPH", java.time.LocalDate.of(2026, 11, 10), java.time.LocalDate.of(2026, 11, 11), 1, 0
        )).isInstanceOf(com.team6.quanlyvemaybay.exception.BusinessException.class)
                .hasMessageContaining("phải khác nhau");

        assertThatThrownBy(() -> chuyenBayService.getMinPriceByDateRange(
                "HPH", "SGN", java.time.LocalDate.of(2026, 11, 12), java.time.LocalDate.of(2026, 11, 10), 1, 0
        )).isInstanceOf(com.team6.quanlyvemaybay.exception.BusinessException.class)
                .hasMessageContaining("Ngày bắt đầu");

        assertThatThrownBy(() -> chuyenBayService.getMinPriceByDateRange(
                "HPH", "SGN", java.time.LocalDate.of(2026, 11, 1), java.time.LocalDate.of(2026, 12, 5), 1, 0
        )).isInstanceOf(com.team6.quanlyvemaybay.exception.BusinessException.class)
                .hasMessageContaining("tối đa là 31 ngày");
    }

    private HangVeChuyenBay fare(Long id, LocalDateTime departure, String price, String tax) {
        ChuyenBay flight = new ChuyenBay();
        flight.setThoiGianKhoiHanh(departure);
        HangVeChuyenBay fare = new HangVeChuyenBay();
        fare.setId(id);
        fare.setChuyenBay(flight);
        fare.setGiaBan(new BigDecimal(price));
        fare.setThuePhi(new BigDecimal(tax));
        return fare;
    }
}
