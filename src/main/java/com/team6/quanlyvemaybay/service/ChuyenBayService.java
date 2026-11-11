package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.TimeConfig;
import com.team6.quanlyvemaybay.dto.flight.AirlineSummaryDto;
import com.team6.quanlyvemaybay.dto.flight.AirportSummaryDto;
import com.team6.quanlyvemaybay.dto.flight.FlightTimeDto;
import com.team6.quanlyvemaybay.dto.flight.TopCheapFlightResponse;
import com.team6.quanlyvemaybay.dto.flight.FlightSearchResponse;
import com.team6.quanlyvemaybay.dto.flight.FlightDetailResponse;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.HangHangKhong;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiHang;
import com.team6.quanlyvemaybay.repository.HangVeChuyenBayRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Service xử lý nghiệp vụ truy vấn chuyến bay cho Trang chủ và tìm kiếm.
 */
@Service
@Transactional(readOnly = true)
public class ChuyenBayService {

    public List<FlightSearchResponse> search(String maDi, String maDen, java.time.LocalDate ngayDi,
                                              int soNguoiLon, int soTreEm, String maHang) {
        if (maDi.equalsIgnoreCase(maDen)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ROUTE", "Sân bay đi và đến phải khác nhau");
        }
        if (ngayDi.isBefore(java.time.LocalDate.now(clock))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_DATE", "Ngày bay không được ở quá khứ");
        }
        int soCho = soNguoiLon + soTreEm;
        return hangVeChuyenBayRepository.findForSearch(maDi.toUpperCase(Locale.ROOT), maDen.toUpperCase(Locale.ROOT),
                        ngayDi.atStartOfDay(), ngayDi.plusDays(1).atStartOfDay(),
                        TrangThaiChuyenBay.DUNG_GIO, TrangThaiHang.HOAT_DONG, soCho,
                        maHang == null ? null : maHang.toUpperCase(Locale.ROOT))
                .stream().filter(hv -> hv.getChuyenBay().getThoiGianKhoiHanh().isAfter(LocalDateTime.now(clock)))
                .map(this::toSearchResponse).toList();
    }

    public FlightDetailResponse detail(Long id) {
        List<HangVeChuyenBay> fares = hangVeChuyenBayRepository.findByFlightIdWithDetails(id);
        if (fares.isEmpty()) throw new ResourceNotFoundException("FLIGHT_NOT_FOUND", "Không tìm thấy chuyến bay");
        return new FlightDetailResponse(id, fares.stream().map(this::toSearchResponse).toList());
    }

    private FlightSearchResponse toSearchResponse(HangVeChuyenBay hv) {
        ChuyenBay cb = hv.getChuyenBay();
        SanBay di = cb.getSanBayDi();
        SanBay den = cb.getSanBayDen();
        // Schema hiện tại lưu giờ địa phương không kèm ZoneId; các chuyến nội địa dùng Asia/Ho_Chi_Minh.
        ZonedDateTime khoiHanh = cb.getThoiGianKhoiHanh().atZone(TimeConfig.DEFAULT_VN_ZONE);
        ZonedDateTime haCanh = cb.getThoiGianHaCanh().atZone(TimeConfig.DEFAULT_VN_ZONE);
        return new FlightSearchResponse(cb.getId(), hv.getId(), cb.getMaChuyenBay(),
                new AirlineSummaryDto(cb.getHangHangKhong().getMaHang(), cb.getHangHangKhong().getTenHang(), cb.getHangHangKhong().getLogoUrl()),
                toAirportSummary(di), toAirportSummary(den), toFlightTimeDto(khoiHanh), toFlightTimeDto(haCanh),
                Duration.between(khoiHanh, haCanh).toMinutes(), hv.getLoaiKhoang(), hv.getGiaBan(), hv.getThuePhi(),
                hv.getGiaBan().add(hv.getThuePhi()), DEFAULT_CURRENCY, hv.getSoGheTrong(),
                hv.getHanhLyXachTayKg(), hv.getHanhLyKyGuiKg());
    }

    private static final String DEFAULT_CURRENCY = "VND";
    private static final Map<String, ZoneId> AIRPORT_TIMEZONES = Map.ofEntries(
            Map.entry("BKK", ZoneId.of("Asia/Bangkok")),
            Map.entry("DMK", ZoneId.of("Asia/Bangkok")),
            Map.entry("SIN", ZoneId.of("Asia/Singapore")),
            Map.entry("KUL", ZoneId.of("Asia/Kuala_Lumpur")),
            Map.entry("ICN", ZoneId.of("Asia/Seoul")),
            Map.entry("GMP", ZoneId.of("Asia/Seoul")),
            Map.entry("NRT", ZoneId.of("Asia/Tokyo")),
            Map.entry("HND", ZoneId.of("Asia/Tokyo")),
            Map.entry("TPE", ZoneId.of("Asia/Taipei")),
            Map.entry("HKG", ZoneId.of("Asia/Hong_Kong")),
            Map.entry("SYD", ZoneId.of("Australia/Sydney")),
            Map.entry("MEL", ZoneId.of("Australia/Melbourne"))
    );

    private final HangVeChuyenBayRepository hangVeChuyenBayRepository;
    private final Clock clock;

    public ChuyenBayService(HangVeChuyenBayRepository hangVeChuyenBayRepository, Clock clock) {
        this.hangVeChuyenBayRepository = hangVeChuyenBayRepository;
        this.clock = clock;
    }

    /**
     * Lấy danh sách tối đa {@code limit} chuyến bay giá rẻ nhất còn khả dụng và chưa cất cánh.
     * Mỗi chuyến bay chỉ xuất hiện 1 lần với hạng vé có tổng giá thấp nhất còn ghế trống.
     */
    public List<TopCheapFlightResponse> getTopCheapFlights(int limit) {
        LocalDateTime now = LocalDateTime.now(clock);
        int fetchSize = Math.min(limit * 2, 100);

        List<HangVeChuyenBay> candidates = hangVeChuyenBayRepository.findTopCheapestAvailableFlights(
                TrangThaiChuyenBay.DA_HUY,
                TrangThaiHang.HOAT_DONG,
                now,
                PageRequest.of(0, fetchSize)
        );

        if (candidates.isEmpty()) {
            return List.of();
        }

        Set<Long> seenFlightIds = new HashSet<>();
        return candidates.stream()
                .filter(hv -> seenFlightIds.add(hv.getChuyenBay().getId()))
                .limit(limit)
                .map(this::toTopCheapFlightResponse)
                .toList();
    }

    private TopCheapFlightResponse toTopCheapFlightResponse(HangVeChuyenBay hangVe) {
        ChuyenBay chuyenBay = hangVe.getChuyenBay();
        HangHangKhong hang = chuyenBay.getHangHangKhong();
        SanBay sanBayDi = chuyenBay.getSanBayDi();
        SanBay sanBayDen = chuyenBay.getSanBayDen();

        ZoneId depZone = resolveZoneId(sanBayDi);
        ZoneId arrZone = resolveZoneId(sanBayDen);

        ZonedDateTime depTime = chuyenBay.getThoiGianKhoiHanh().atZone(depZone);
        ZonedDateTime arrTime = chuyenBay.getThoiGianHaCanh().atZone(arrZone);
        long durationMinutes = Math.max(0L, Duration.between(depTime.toInstant(), arrTime.toInstant()).toMinutes());

        BigDecimal giaBan = hangVe.getGiaBan() != null ? hangVe.getGiaBan() : BigDecimal.ZERO;
        BigDecimal thuePhi = hangVe.getThuePhi() != null ? hangVe.getThuePhi() : BigDecimal.ZERO;
        BigDecimal tongGiaVe = giaBan.add(thuePhi);

        return new TopCheapFlightResponse(
                chuyenBay.getId(),
                chuyenBay.getMaChuyenBay(),
                new AirlineSummaryDto(hang.getMaHang(), hang.getTenHang(), hang.getLogoUrl()),
                toAirportSummary(sanBayDi),
                toAirportSummary(sanBayDen),
                toFlightTimeDto(depTime),
                toFlightTimeDto(arrTime),
                durationMinutes,
                hangVe.getLoaiKhoang(),
                giaBan,
                thuePhi,
                tongGiaVe,
                DEFAULT_CURRENCY,
                hangVe.getSoGheTrong(),
                hangVe.getHanhLyXachTayKg(),
                hangVe.getHanhLyKyGuiKg(),
                chuyenBay.getTrangThai()
        );
    }

    private AirportSummaryDto toAirportSummary(SanBay sanBay) {
        return new AirportSummaryDto(
                sanBay.getMaSanBay(),
                sanBay.getTenSanBay(),
                sanBay.getThanhPho(),
                sanBay.getQuocGia()
        );
    }

    private FlightTimeDto toFlightTimeDto(ZonedDateTime zonedDateTime) {
        return new FlightTimeDto(
                zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                zonedDateTime.getZone().getId()
        );
    }

    private ZoneId resolveZoneId(SanBay sanBay) {
        if (sanBay != null && sanBay.getMaSanBay() != null) {
            return AIRPORT_TIMEZONES.getOrDefault(
                    sanBay.getMaSanBay().toUpperCase(Locale.ROOT),
                    TimeConfig.DEFAULT_VN_ZONE
            );
        }
        return TimeConfig.DEFAULT_VN_ZONE;
    }
}
