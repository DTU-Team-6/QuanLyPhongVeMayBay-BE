package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.report.LoaiBaoCao;
import com.team6.quanlyvemaybay.dto.report.ReportFilterRequest;
import com.team6.quanlyvemaybay.dto.report.ReportItemDto;
import com.team6.quanlyvemaybay.dto.report.ReportResponse;
import com.team6.quanlyvemaybay.dto.report.ReportSummaryDto;
import com.team6.quanlyvemaybay.dto.report.StatisticsFilterRequest;
import com.team6.quanlyvemaybay.dto.report.StatisticsPeriodDto;
import com.team6.quanlyvemaybay.dto.report.StatisticsResponse;
import com.team6.quanlyvemaybay.dto.report.StatisticsSummaryDto;
import com.team6.quanlyvemaybay.dto.report.TieuChiThongKe;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.HangVeChuyenBayRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import com.team6.quanlyvemaybay.repository.projection.AirlineAggregationRow;
import com.team6.quanlyvemaybay.repository.projection.FlightOccupancyRow;
import com.team6.quanlyvemaybay.repository.projection.StaffAggregationRow;
import com.team6.quanlyvemaybay.repository.projection.StaffTicketCountRow;
import com.team6.quanlyvemaybay.repository.projection.TimeAggregationRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service xử lý truy vấn tổng hợp báo cáo doanh thu và thống kê chỉ số kinh doanh cho Admin/Nhân viên.
 */
@Service
@Transactional(readOnly = true)
public class BaoCaoService {

    private static final String DEFAULT_CURRENCY = "VND";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final DatChoRepository datChoRepository;
    private final VeMayBayRepository veMayBayRepository;
    private final HangVeChuyenBayRepository hangVeChuyenBayRepository;

    public BaoCaoService(
            DatChoRepository datChoRepository,
            VeMayBayRepository veMayBayRepository,
            HangVeChuyenBayRepository hangVeChuyenBayRepository
    ) {
        this.datChoRepository = datChoRepository;
        this.veMayBayRepository = veMayBayRepository;
        this.hangVeChuyenBayRepository = hangVeChuyenBayRepository;
    }

    /**
     * Tạo báo cáo chi tiết theo hãng, nhân viên hoặc thời gian.
     */
    public ReportResponse generateReport(ReportFilterRequest filter) {
        LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
        LocalDateTime endExclusive = filter.getEndDate().plusDays(1).atStartOfDay();
        TieuChiThongKe groupBy = filter.getGroupBy() != null ? filter.getGroupBy() : TieuChiThongKe.DAY;
        TrangThaiDatCho trangThaiDatCho = filter.getTrangThaiDatCho() != null
                ? filter.getTrangThaiDatCho()
                : TrangThaiDatCho.DA_THANH_TOAN;
        TrangThaiVe trangThaiVe = mapTicketStatus(trangThaiDatCho);
        String maHang = normalizeCode(filter.getMaHang());
        Long nhanVienId = filter.getNhanVienId();

        List<ReportItemDto> items = switch (filter.getReportType()) {
            case THEO_HANG -> buildAirlineReport(startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, maHang, nhanVienId);
            case THEO_NHAN_VIEN -> buildStaffReport(startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, nhanVienId, maHang);
            case THEO_THOI_GIAN -> buildTimeReport(startDateTime, endExclusive, groupBy, trangThaiDatCho, trangThaiVe, nhanVienId, maHang);
        };

        ReportSummaryDto summary = buildReportSummary(items);
        return new ReportResponse(
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getReportType(),
                groupBy,
                trangThaiDatCho,
                DEFAULT_CURRENCY,
                summary,
                items
        );
    }

    /**
     * Thống kê tổng hợp doanh thu, số lượng vé bán ra và tỷ lệ lấp đầy chuyến bay theo kỳ.
     */
    public StatisticsResponse getStatistics(StatisticsFilterRequest filter) {
        LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
        LocalDateTime endExclusive = filter.getEndDate().plusDays(1).atStartOfDay();
        TieuChiThongKe periodType = filter.getPeriod() != null ? filter.getPeriod() : TieuChiThongKe.DAY;
        String maHang = normalizeCode(filter.getMaHang());

        Map<String, PeriodAccumulator> merged = new TreeMap<>();

        if (maHang == null) {
            List<TimeAggregationRow> bookingRows = queryBookingsByPeriod(
                    startDateTime, endExclusive, periodType, TrangThaiDatCho.DA_THANH_TOAN, null, null
            );
            for (TimeAggregationRow row : bookingRows) {
                String key = formatPeriodKey(row.getNam(), row.getThang(), row.getNgay(), periodType);
                PeriodAccumulator acc = merged.computeIfAbsent(key, ignored -> new PeriodAccumulator());
                acc.soDonDatCho += zeroIfNull(row.getSoDonDatCho());
                acc.tongGiaGoc = acc.tongGiaGoc.add(zeroIfNull(row.getTongGiaGoc()));
                acc.doanhThu = acc.doanhThu.add(zeroIfNull(row.getDoanhThu()));
            }

            List<TimeAggregationRow> ticketRows = queryTicketsByPeriod(
                    startDateTime, endExclusive, periodType, TrangThaiDatCho.DA_THANH_TOAN, TrangThaiVe.DA_THANH_TOAN, null, null
            );
            for (TimeAggregationRow row : ticketRows) {
                String key = formatPeriodKey(row.getNam(), row.getThang(), row.getNgay(), periodType);
                PeriodAccumulator acc = merged.computeIfAbsent(key, ignored -> new PeriodAccumulator());
                acc.soVeBanRa += zeroIfNull(row.getSoVeBanRa());
            }
        } else {
            List<TimeAggregationRow> ticketRows = queryTicketsByPeriod(
                    startDateTime, endExclusive, periodType, TrangThaiDatCho.DA_THANH_TOAN, TrangThaiVe.DA_THANH_TOAN, null, maHang
            );
            for (TimeAggregationRow row : ticketRows) {
                String key = formatPeriodKey(row.getNam(), row.getThang(), row.getNgay(), periodType);
                PeriodAccumulator acc = merged.computeIfAbsent(key, ignored -> new PeriodAccumulator());
                acc.soDonDatCho += zeroIfNull(row.getSoDonDatCho());
                acc.soVeBanRa += zeroIfNull(row.getSoVeBanRa());
                acc.tongGiaGoc = acc.tongGiaGoc.add(zeroIfNull(row.getTongGiaGoc()));
                acc.doanhThu = acc.doanhThu.add(zeroIfNull(row.getDoanhThu()));
            }
        }

        List<FlightOccupancyRow> occupancyRows = queryOccupancyByPeriod(startDateTime, endExclusive, periodType, maHang);
        for (FlightOccupancyRow row : occupancyRows) {
            String key = formatPeriodKey(row.getNam(), row.getThang(), row.getNgay(), periodType);
            PeriodAccumulator acc = merged.computeIfAbsent(key, ignored -> new PeriodAccumulator());
            long totalSeats = zeroIfNull(row.getTongSoGhe());
            long availableSeats = zeroIfNull(row.getSoGheTrong());
            acc.soChuyenBay += zeroIfNull(row.getSoChuyenBay());
            acc.tongSoGhe += totalSeats;
            acc.soGheDaLapDay += Math.max(0L, totalSeats - availableSeats);
        }

        List<StatisticsPeriodDto> items = merged.entrySet().stream()
                .map(entry -> toPeriodDto(entry.getKey(), entry.getValue()))
                .toList();

        StatisticsSummaryDto summary = buildStatisticsSummary(items);
        return new StatisticsResponse(
                filter.getStartDate(),
                filter.getEndDate(),
                periodType,
                maHang,
                DEFAULT_CURRENCY,
                summary,
                items
        );
    }

    private List<ReportItemDto> buildAirlineReport(
            LocalDateTime startDateTime,
            LocalDateTime endExclusive,
            TrangThaiDatCho trangThaiDatCho,
            TrangThaiVe trangThaiVe,
            String maHang,
            Long nhanVienId
    ) {
        List<AirlineAggregationRow> rows = veMayBayRepository.aggregateByAirline(
                startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, maHang, nhanVienId
        );
        return rows.stream()
                .map(row -> {
                    BigDecimal giaGoc = zeroIfNull(row.getTongGiaGoc());
                    BigDecimal doanhThu = zeroIfNull(row.getDoanhThu());
                    return new ReportItemDto(
                            row.getMaHang(),
                            row.getTenHang(),
                            zeroIfNull(row.getSoDonDatCho()),
                            zeroIfNull(row.getSoVeBanRa()),
                            giaGoc,
                            doanhThu,
                            doanhThu.subtract(giaGoc)
                    );
                })
                .toList();
    }

    private List<ReportItemDto> buildStaffReport(
            LocalDateTime startDateTime,
            LocalDateTime endExclusive,
            TrangThaiDatCho trangThaiDatCho,
            TrangThaiVe trangThaiVe,
            Long nhanVienId,
            String maHang
    ) {
        List<StaffAggregationRow> staffRows = datChoRepository.aggregateByStaff(
                startDateTime, endExclusive, trangThaiDatCho, nhanVienId, maHang
        );
        if (staffRows.isEmpty()) {
            return List.of();
        }

        List<StaffTicketCountRow> ticketRows = veMayBayRepository.countTicketsByStaff(
                startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, nhanVienId, maHang
        );
        // HashMap hỗ trợ key null đối với đơn do khách tự đặt Online (nhan_vien_id IS NULL)
        Map<Long, Long> ticketCounts = new HashMap<>();
        for (StaffTicketCountRow ticketRow : ticketRows) {
            ticketCounts.put(ticketRow.getNhanVienId(), zeroIfNull(ticketRow.getSoVe()));
        }

        return staffRows.stream()
                .map(row -> {
                    BigDecimal giaGoc = zeroIfNull(row.getTongGiaGoc());
                    BigDecimal doanhThu = zeroIfNull(row.getDoanhThu());
                    return new ReportItemDto(
                            row.getMaNhanVien(),
                            row.getHoTenNhanVien(),
                            zeroIfNull(row.getSoDonDatCho()),
                            ticketCounts.getOrDefault(row.getNhanVienId(), 0L),
                            giaGoc,
                            doanhThu,
                            doanhThu.subtract(giaGoc)
                    );
                })
                .toList();
    }

    private List<ReportItemDto> buildTimeReport(
            LocalDateTime startDateTime,
            LocalDateTime endExclusive,
            TieuChiThongKe groupBy,
            TrangThaiDatCho trangThaiDatCho,
            TrangThaiVe trangThaiVe,
            Long nhanVienId,
            String maHang
    ) {
        List<TimeAggregationRow> bookingRows = queryBookingsByPeriod(
                startDateTime, endExclusive, groupBy, trangThaiDatCho, nhanVienId, maHang
        );
        if (bookingRows.isEmpty()) {
            return List.of();
        }

        List<TimeAggregationRow> ticketRows = queryTicketsByPeriod(
                startDateTime, endExclusive, groupBy, trangThaiDatCho, trangThaiVe, nhanVienId, maHang
        );
        Map<String, Long> ticketCountsByPeriod = new HashMap<>();
        for (TimeAggregationRow row : ticketRows) {
            String key = formatPeriodKey(row.getNam(), row.getThang(), row.getNgay(), groupBy);
            ticketCountsByPeriod.put(key, zeroIfNull(row.getSoVeBanRa()));
        }

        return bookingRows.stream()
                .map(row -> {
                    String key = formatPeriodKey(row.getNam(), row.getThang(), row.getNgay(), groupBy);
                    BigDecimal giaGoc = zeroIfNull(row.getTongGiaGoc());
                    BigDecimal doanhThu = zeroIfNull(row.getDoanhThu());
                    return new ReportItemDto(
                            key,
                            formatPeriodLabel(key, groupBy),
                            zeroIfNull(row.getSoDonDatCho()),
                            ticketCountsByPeriod.getOrDefault(key, 0L),
                            giaGoc,
                            doanhThu,
                            doanhThu.subtract(giaGoc)
                    );
                })
                .toList();
    }

    private List<TimeAggregationRow> queryBookingsByPeriod(
            LocalDateTime startDateTime,
            LocalDateTime endExclusive,
            TieuChiThongKe period,
            TrangThaiDatCho trangThaiDatCho,
            Long nhanVienId,
            String maHang
    ) {
        return switch (period) {
            case DAY -> datChoRepository.aggregateByDay(startDateTime, endExclusive, trangThaiDatCho, nhanVienId, maHang);
            case MONTH -> datChoRepository.aggregateByMonth(startDateTime, endExclusive, trangThaiDatCho, nhanVienId, maHang);
            case YEAR -> datChoRepository.aggregateByYear(startDateTime, endExclusive, trangThaiDatCho, nhanVienId, maHang);
        };
    }

    private List<TimeAggregationRow> queryTicketsByPeriod(
            LocalDateTime startDateTime,
            LocalDateTime endExclusive,
            TieuChiThongKe period,
            TrangThaiDatCho trangThaiDatCho,
            TrangThaiVe trangThaiVe,
            Long nhanVienId,
            String maHang
    ) {
        return switch (period) {
            case DAY -> veMayBayRepository.aggregateTicketsByDay(startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, nhanVienId, maHang);
            case MONTH -> veMayBayRepository.aggregateTicketsByMonth(startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, nhanVienId, maHang);
            case YEAR -> veMayBayRepository.aggregateTicketsByYear(startDateTime, endExclusive, trangThaiDatCho, trangThaiVe, nhanVienId, maHang);
        };
    }

    private List<FlightOccupancyRow> queryOccupancyByPeriod(
            LocalDateTime startDateTime,
            LocalDateTime endExclusive,
            TieuChiThongKe period,
            String maHang
    ) {
        return switch (period) {
            case DAY -> hangVeChuyenBayRepository.aggregateOccupancyByDay(startDateTime, endExclusive, TrangThaiChuyenBay.DA_HUY, maHang);
            case MONTH -> hangVeChuyenBayRepository.aggregateOccupancyByMonth(startDateTime, endExclusive, TrangThaiChuyenBay.DA_HUY, maHang);
            case YEAR -> hangVeChuyenBayRepository.aggregateOccupancyByYear(startDateTime, endExclusive, TrangThaiChuyenBay.DA_HUY, maHang);
        };
    }

    private ReportSummaryDto buildReportSummary(List<ReportItemDto> items) {
        long totalBookings = 0L;
        long totalTickets = 0L;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (ReportItemDto item : items) {
            totalBookings += item.soDonDatCho();
            totalTickets += item.soVeBanRa();
            totalCost = totalCost.add(item.tongGiaGoc());
            totalRevenue = totalRevenue.add(item.doanhThu());
        }

        return new ReportSummaryDto(
                totalBookings,
                totalTickets,
                totalCost,
                totalRevenue,
                totalRevenue.subtract(totalCost)
        );
    }

    private StatisticsPeriodDto toPeriodDto(String periodKey, PeriodAccumulator acc) {
        return new StatisticsPeriodDto(
                periodKey,
                acc.soDonDatCho,
                acc.soVeBanRa,
                acc.tongGiaGoc,
                acc.doanhThu,
                acc.doanhThu.subtract(acc.tongGiaGoc),
                acc.soChuyenBay,
                acc.tongSoGhe,
                acc.soGheDaLapDay,
                calculateOccupancyRate(acc.soGheDaLapDay, acc.tongSoGhe)
        );
    }

    private StatisticsSummaryDto buildStatisticsSummary(List<StatisticsPeriodDto> items) {
        long totalBookings = 0L;
        long totalTickets = 0L;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalFlights = 0L;
        long totalSeats = 0L;
        long totalBookedSeats = 0L;

        for (StatisticsPeriodDto item : items) {
            totalBookings += item.soDonDatCho();
            totalTickets += item.soVeBanRa();
            totalCost = totalCost.add(item.tongGiaGoc());
            totalRevenue = totalRevenue.add(item.doanhThu());
            totalFlights += item.soChuyenBay();
            totalSeats += item.tongSoGhe();
            totalBookedSeats += item.soGheDaLapDay();
        }

        return new StatisticsSummaryDto(
                totalBookings,
                totalTickets,
                totalCost,
                totalRevenue,
                totalRevenue.subtract(totalCost),
                totalFlights,
                totalSeats,
                totalBookedSeats,
                calculateOccupancyRate(totalBookedSeats, totalSeats)
        );
    }

    private BigDecimal calculateOccupancyRate(long occupiedSeats, long totalSeats) {
        if (totalSeats <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(occupiedSeats)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP);
    }

    private String formatPeriodKey(Integer year, Integer month, Integer day, TieuChiThongKe period) {
        int y = year != null ? year : 0;
        return switch (period) {
            case DAY -> String.format("%04d-%02d-%02d", y, month != null ? month : 1, day != null ? day : 1);
            case MONTH -> String.format("%04d-%02d", y, month != null ? month : 1);
            case YEAR -> String.format("%04d", y);
        };
    }

    private String formatPeriodLabel(String key, TieuChiThongKe period) {
        return switch (period) {
            case DAY -> "Ngày " + key;
            case MONTH -> "Tháng " + key;
            case YEAR -> "Năm " + key;
        };
    }

    private TrangThaiVe mapTicketStatus(TrangThaiDatCho status) {
        return switch (status) {
            case CHO_THANH_TOAN -> TrangThaiVe.CHO_THANH_TOAN;
            case DA_THANH_TOAN -> TrangThaiVe.DA_THANH_TOAN;
            case DA_HUY -> TrangThaiVe.DA_HUY;
        };
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private long zeroIfNull(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static final class PeriodAccumulator {
        private long soDonDatCho;
        private long soVeBanRa;
        private BigDecimal tongGiaGoc = BigDecimal.ZERO;
        private BigDecimal doanhThu = BigDecimal.ZERO;
        private long soChuyenBay;
        private long tongSoGhe;
        private long soGheDaLapDay;
    }
}
