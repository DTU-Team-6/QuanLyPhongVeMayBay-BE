package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.report.LoaiBaoCao;
import com.team6.quanlyvemaybay.dto.report.ReportFilterRequest;
import com.team6.quanlyvemaybay.dto.report.ReportResponse;
import com.team6.quanlyvemaybay.dto.report.StatisticsFilterRequest;
import com.team6.quanlyvemaybay.dto.report.StatisticsResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử đơn vị cho BaoCaoService (tổng hợp báo cáo và thống kê).
 */
@ExtendWith(MockitoExtension.class)
class BaoCaoServiceTest {

    @Mock
    private DatChoRepository datChoRepository;

    @Mock
    private VeMayBayRepository veMayBayRepository;

    @Mock
    private HangVeChuyenBayRepository hangVeChuyenBayRepository;

    private BaoCaoService baoCaoService;

    @BeforeEach
    void setUp() {
        baoCaoService = new BaoCaoService(datChoRepository, veMayBayRepository, hangVeChuyenBayRepository);
    }

    @Test
    void generateReport_byAirline_shouldComputeSummaryAndProfitAccurately() {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(LocalDate.of(2026, 11, 1));
        filter.setEndDate(LocalDate.of(2026, 11, 30));
        filter.setReportType(LoaiBaoCao.THEO_HANG);
        filter.setMaHang(" vn ");

        AirlineAggregationRow vnRow = new AirlineAggregationRow() {
            @Override public String getMaHang() { return "VN"; }
            @Override public String getTenHang() { return "Vietnam Airlines"; }
            @Override public Long getSoDonDatCho() { return 3L; }
            @Override public Long getSoVeBanRa() { return 5L; }
            @Override public BigDecimal getTongGiaGoc() { return new BigDecimal("5000000.00"); }
            @Override public BigDecimal getDoanhThu() { return new BigDecimal("6500000.00"); }
        };

        when(veMayBayRepository.aggregateByAirline(
                eq(LocalDateTime.of(2026, 11, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 12, 1, 0, 0)),
                eq(TrangThaiDatCho.DA_THANH_TOAN),
                eq(TrangThaiVe.DA_THANH_TOAN),
                eq("VN"),
                isNull()
        )).thenReturn(List.of(vnRow));

        ReportResponse response = baoCaoService.generateReport(filter);

        assertThat(response.tienTe()).isEqualTo("VND");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().loiNhuan()).isEqualByComparingTo("1500000.00");
        assertThat(response.summary().tongSoDonDatCho()).isEqualTo(3L);
        assertThat(response.summary().tongSoVe()).isEqualTo(5L);
        assertThat(response.summary().tongLoiNhuan()).isEqualByComparingTo("1500000.00");
    }

    @Test
    void generateReport_byStaff_shouldSupportBothStaffAndOnlineNullKey() {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(LocalDate.of(2026, 11, 1));
        filter.setEndDate(LocalDate.of(2026, 11, 10));
        filter.setReportType(LoaiBaoCao.THEO_NHAN_VIEN);

        StaffAggregationRow staffRow = new StaffAggregationRow() {
            @Override public Long getNhanVienId() { return 2L; }
            @Override public String getMaNhanVien() { return "NV1"; }
            @Override public String getHoTenNhanVien() { return "Nguyễn Văn Nhân Viên"; }
            @Override public Long getSoDonDatCho() { return 1L; }
            @Override public BigDecimal getTongGiaGoc() { return new BigDecimal("8956000.00"); }
            @Override public BigDecimal getDoanhThu() { return new BigDecimal("9156000.00"); }
        };
        StaffAggregationRow onlineRow = new StaffAggregationRow() {
            @Override public Long getNhanVienId() { return null; }
            @Override public String getMaNhanVien() { return "ONLINE"; }
            @Override public String getHoTenNhanVien() { return "Khách tự đặt Online"; }
            @Override public Long getSoDonDatCho() { return 1L; }
            @Override public BigDecimal getTongGiaGoc() { return new BigDecimal("2000000.00"); }
            @Override public BigDecimal getDoanhThu() { return new BigDecimal("2500000.00"); }
        };

        StaffTicketCountRow staffTickets = new StaffTicketCountRow() {
            @Override public Long getNhanVienId() { return 2L; }
            @Override public Long getSoVe() { return 2L; }
        };
        StaffTicketCountRow onlineTickets = new StaffTicketCountRow() {
            @Override public Long getNhanVienId() { return null; }
            @Override public Long getSoVe() { return 1L; }
        };

        when(datChoRepository.aggregateByStaff(any(), any(), any(), any(), any()))
                .thenReturn(List.of(staffRow, onlineRow));
        when(veMayBayRepository.countTicketsByStaff(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(staffTickets, onlineTickets));

        ReportResponse response = baoCaoService.generateReport(filter);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).soVeBanRa()).isEqualTo(2L);
        assertThat(response.items().get(1).key()).isEqualTo("ONLINE");
        assertThat(response.items().get(1).soVeBanRa()).isEqualTo(1L);
        assertThat(response.summary().tongSoDonDatCho()).isEqualTo(2L);
        assertThat(response.summary().tongSoVe()).isEqualTo(3L);
        assertThat(response.summary().tongLoiNhuan()).isEqualByComparingTo("700000.00");
    }

    @Test
    void generateReport_byMonth_shouldFormatPeriodKeys() {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(LocalDate.of(2026, 11, 1));
        filter.setEndDate(LocalDate.of(2026, 11, 30));
        filter.setReportType(LoaiBaoCao.THEO_THOI_GIAN);
        filter.setGroupBy(TieuChiThongKe.MONTH);

        TimeAggregationRow bookingRow = new TimeAggregationRow() {
            @Override public Integer getNam() { return 2026; }
            @Override public Integer getThang() { return 11; }
            @Override public Integer getNgay() { return null; }
            @Override public Long getSoDonDatCho() { return 2L; }
            @Override public Long getSoVeBanRa() { return null; }
            @Override public BigDecimal getTongGiaGoc() { return new BigDecimal("3000000.00"); }
            @Override public BigDecimal getDoanhThu() { return new BigDecimal("4000000.00"); }
        };
        TimeAggregationRow ticketRow = new TimeAggregationRow() {
            @Override public Integer getNam() { return 2026; }
            @Override public Integer getThang() { return 11; }
            @Override public Integer getNgay() { return null; }
            @Override public Long getSoDonDatCho() { return 2L; }
            @Override public Long getSoVeBanRa() { return 4L; }
            @Override public BigDecimal getTongGiaGoc() { return BigDecimal.ZERO; }
            @Override public BigDecimal getDoanhThu() { return BigDecimal.ZERO; }
        };

        when(datChoRepository.aggregateByMonth(any(), any(), any(), any(), any())).thenReturn(List.of(bookingRow));
        when(veMayBayRepository.aggregateTicketsByMonth(any(), any(), any(), any(), any(), any())).thenReturn(List.of(ticketRow));

        ReportResponse response = baoCaoService.generateReport(filter);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().key()).isEqualTo("2026-11");
        assertThat(response.items().getFirst().label()).isEqualTo("Tháng 2026-11");
        assertThat(response.items().getFirst().soVeBanRa()).isEqualTo(4L);
    }

    @Test
    void getStatistics_shouldMergeRevenueTicketsAndOccupancyRate() {
        StatisticsFilterRequest filter = new StatisticsFilterRequest();
        filter.setStartDate(LocalDate.of(2026, 11, 1));
        filter.setEndDate(LocalDate.of(2026, 11, 20));
        filter.setPeriod(TieuChiThongKe.DAY);

        TimeAggregationRow bookingDay9 = new TimeAggregationRow() {
            @Override public Integer getNam() { return 2026; }
            @Override public Integer getThang() { return 11; }
            @Override public Integer getNgay() { return 9; }
            @Override public Long getSoDonDatCho() { return 1L; }
            @Override public Long getSoVeBanRa() { return null; }
            @Override public BigDecimal getTongGiaGoc() { return new BigDecimal("8956000.00"); }
            @Override public BigDecimal getDoanhThu() { return new BigDecimal("9156000.00"); }
        };
        TimeAggregationRow ticketDay9 = new TimeAggregationRow() {
            @Override public Integer getNam() { return 2026; }
            @Override public Integer getThang() { return 11; }
            @Override public Integer getNgay() { return 9; }
            @Override public Long getSoDonDatCho() { return 1L; }
            @Override public Long getSoVeBanRa() { return 2L; }
            @Override public BigDecimal getTongGiaGoc() { return BigDecimal.ZERO; }
            @Override public BigDecimal getDoanhThu() { return BigDecimal.ZERO; }
        };
        FlightOccupancyRow flightDay15 = new FlightOccupancyRow() {
            @Override public Integer getNam() { return 2026; }
            @Override public Integer getThang() { return 11; }
            @Override public Integer getNgay() { return 15; }
            @Override public Long getSoChuyenBay() { return 2L; }
            @Override public Long getTongSoGhe() { return 350L; }
            @Override public Long getSoGheTrong() { return 333L; }
        };

        when(datChoRepository.aggregateByDay(any(), any(), any(), any(), any())).thenReturn(List.of(bookingDay9));
        when(veMayBayRepository.aggregateTicketsByDay(any(), any(), any(), any(), any(), any())).thenReturn(List.of(ticketDay9));
        when(hangVeChuyenBayRepository.aggregateOccupancyByDay(any(), any(), eq(TrangThaiChuyenBay.DA_HUY), isNull()))
                .thenReturn(List.of(flightDay15));

        StatisticsResponse response = baoCaoService.getStatistics(filter);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).period()).isEqualTo("2026-11-09");
        assertThat(response.items().get(0).doanhThu()).isEqualByComparingTo("9156000.00");
        assertThat(response.items().get(0).soVeBanRa()).isEqualTo(2L);
        assertThat(response.items().get(1).period()).isEqualTo("2026-11-15");
        assertThat(response.items().get(1).soGheDaLapDay()).isEqualTo(17L);
        assertThat(response.items().get(1).tyLeLapDayPhanTram()).isEqualByComparingTo("4.86");

        assertThat(response.summary().tongDoanhThu()).isEqualByComparingTo("9156000.00");
        assertThat(response.summary().tongLoiNhuan()).isEqualByComparingTo("200000.00");
        assertThat(response.summary().tongSoVeBanRa()).isEqualTo(2L);
        assertThat(response.summary().tyLeLapDayPhanTram()).isEqualByComparingTo("4.86");

        verify(hangVeChuyenBayRepository).aggregateOccupancyByDay(any(), any(), eq(TrangThaiChuyenBay.DA_HUY), isNull());
    }

    @Test
    void getStatistics_whenEmpty_shouldReturnZeroSummaryAndEmptyItems() {
        StatisticsFilterRequest filter = new StatisticsFilterRequest();
        filter.setStartDate(LocalDate.of(2025, 1, 1));
        filter.setEndDate(LocalDate.of(2025, 1, 31));
        filter.setPeriod(TieuChiThongKe.MONTH);
        filter.setMaHang("vn");

        when(veMayBayRepository.aggregateTicketsByMonth(any(), any(), any(), any(), any(), eq("VN")))
                .thenReturn(List.of());
        when(hangVeChuyenBayRepository.aggregateOccupancyByMonth(any(), any(), any(), eq("VN")))
                .thenReturn(List.of());

        StatisticsResponse response = baoCaoService.getStatistics(filter);

        assertThat(response.maHang()).isEqualTo("VN");
        assertThat(response.items()).isEmpty();
        assertThat(response.summary().tongDoanhThu()).isEqualByComparingTo("0");
        assertThat(response.summary().tyLeLapDayPhanTram()).isEqualByComparingTo("0.00");
    }
}
