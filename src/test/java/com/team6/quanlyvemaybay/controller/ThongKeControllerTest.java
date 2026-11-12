package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.report.StatisticsPeriodDto;
import com.team6.quanlyvemaybay.dto.report.StatisticsResponse;
import com.team6.quanlyvemaybay.dto.report.StatisticsSummaryDto;
import com.team6.quanlyvemaybay.dto.report.TieuChiThongKe;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.BaoCaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử WebMvc cho API thống kê tổng hợp GET /api/v1/statistics.
 */
@WebMvcTest(controllers = ThongKeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ThongKeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BaoCaoService baoCaoService;

    @Test
    void getStatistics_shouldReturnStandardResponse() throws Exception {
        StatisticsResponse response = new StatisticsResponse(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                TieuChiThongKe.DAY,
                null,
                "VND",
                new StatisticsSummaryDto(1L, 2L, new BigDecimal("8956000.00"), new BigDecimal("9156000.00"),
                        new BigDecimal("200000.00"), 5L, 770L, 43L, new BigDecimal("5.58")),
                List.of(new StatisticsPeriodDto("2026-11-09", 1L, 2L, new BigDecimal("8956000.00"),
                        new BigDecimal("9156000.00"), new BigDecimal("200000.00"), 0L, 0L, 0L, new BigDecimal("0.00")))
        );
        when(baoCaoService.getStatistics(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/statistics")
                        .param("startDate", "2026-11-01")
                        .param("endDate", "2026-11-30")
                        .param("period", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy dữ liệu thống kê thành công"))
                .andExpect(jsonPath("$.data.summary.tongDoanhThu").value(9156000.00))
                .andExpect(jsonPath("$.data.summary.tyLeLapDayPhanTram").value(5.58))
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    void getStatistics_shouldRejectWhenStartDateAfterEndDate() throws Exception {
        mockMvc.perform(get("/api/v1/statistics")
                        .param("startDate", "2026-11-30")
                        .param("endDate", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("startDate"));
    }

    @Test
    void getStatistics_shouldRejectWhenMissingRequiredDates() throws Exception {
        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details[*].field", hasItem("startDate")))
                .andExpect(jsonPath("$.details[*].field", hasItem("endDate")));
    }

    @Test
    void getStatistics_shouldRejectWhenDateFormatInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/statistics")
                        .param("startDate", "10-11-2026")
                        .param("endDate", "2026-11-30"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("startDate"))
                .andExpect(jsonPath("$.details[0].code").value("TYPE_MISMATCH"));
    }
}
