package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.report.LoaiBaoCao;
import com.team6.quanlyvemaybay.dto.report.ReportItemDto;
import com.team6.quanlyvemaybay.dto.report.ReportResponse;
import com.team6.quanlyvemaybay.dto.report.ReportSummaryDto;
import com.team6.quanlyvemaybay.dto.report.TieuChiThongKe;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.BaoCaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử WebMvc cho các endpoint báo cáo POST /api/v1/reports và GET /api/v1/reports/generate.
 */
@WebMvcTest(controllers = BaoCaoController.class)
@Import(GlobalExceptionHandler.class)
class BaoCaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BaoCaoService baoCaoService;

    @Test
    void createReport_shouldReturnStandardReportResponse() throws Exception {
        ReportResponse report = new ReportResponse(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                LoaiBaoCao.THEO_HANG,
                TieuChiThongKe.DAY,
                TrangThaiDatCho.DA_THANH_TOAN,
                "VND",
                new ReportSummaryDto(1L, 2L, new BigDecimal("8956000.00"), new BigDecimal("9156000.00"), new BigDecimal("200000.00")),
                List.of(new ReportItemDto("BL", "Jetstar Pacific", 1L, 2L, new BigDecimal("8956000.00"), new BigDecimal("9156000.00"), new BigDecimal("200000.00")))
        );
        when(baoCaoService.generateReport(any())).thenReturn(report);

        String body = """
                {
                  "startDate": "2026-11-01",
                  "endDate": "2026-11-30",
                  "reportType": "THEO_HANG",
                  "groupBy": "DAY"
                }
                """;

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Tạo báo cáo thành công"))
                .andExpect(jsonPath("$.data.summary.tongDoanhThu").value(9156000.00))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].key").value("BL"));
    }

    @Test
    void generateReportByQuery_shouldReturnEmptyItemsWhenNoData() throws Exception {
        ReportResponse emptyReport = new ReportResponse(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                LoaiBaoCao.THEO_NHAN_VIEN,
                TieuChiThongKe.DAY,
                TrangThaiDatCho.DA_THANH_TOAN,
                "VND",
                new ReportSummaryDto(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                List.of()
        );
        when(baoCaoService.generateReport(any())).thenReturn(emptyReport);

        mockMvc.perform(get("/api/v1/reports/generate")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("reportType", "THEO_NHAN_VIEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Không có dữ liệu báo cáo trong khoảng thời gian đã chọn"))
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }

    @Test
    void createReport_shouldRejectWhenStartDateAfterEndDate() throws Exception {
        String invalidRangeBody = """
                {
                  "startDate": "2026-11-20",
                  "endDate": "2026-11-10",
                  "reportType": "THEO_HANG"
                }
                """;

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRangeBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("startDate"))
                .andExpect(jsonPath("$.details[0].message").value("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc"));
    }

    @Test
    void createReport_shouldRejectWhenMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(3)))
                .andExpect(jsonPath("$.details[*].field", hasItem("startDate")))
                .andExpect(jsonPath("$.details[*].field", hasItem("endDate")))
                .andExpect(jsonPath("$.details[*].field", hasItem("reportType")));
    }

    @Test
    void createReport_shouldRejectWhenInvalidJsonOrEnumFormat() throws Exception {
        String badJson = """
                {
                  "startDate": "2026-11-01",
                  "endDate": "2026-11-30",
                  "reportType": "KHONG_HOP_LE"
                }
                """;

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void generateReportByQuery_shouldRejectWhenStartDateAfterEndDate() throws Exception {
        mockMvc.perform(get("/api/v1/reports/generate")
                        .param("startDate", "2026-12-01")
                        .param("endDate", "2026-11-01")
                        .param("reportType", "THEO_THOI_GIAN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("startDate"));
    }
}
