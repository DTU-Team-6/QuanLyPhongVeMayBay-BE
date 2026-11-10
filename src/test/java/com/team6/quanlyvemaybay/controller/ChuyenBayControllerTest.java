package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.flight.AirlineSummaryDto;
import com.team6.quanlyvemaybay.dto.flight.AirportSummaryDto;
import com.team6.quanlyvemaybay.dto.flight.FlightTimeDto;
import com.team6.quanlyvemaybay.dto.flight.TopCheapFlightResponse;
import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.ChuyenBayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.CannotCreateTransactionException;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử WebMvc cho API GET /api/v1/flights/top-cheap.
 */
@WebMvcTest(controllers = ChuyenBayController.class)
@Import(GlobalExceptionHandler.class)
class ChuyenBayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChuyenBayService chuyenBayService;

    @Test
    void getTopCheapFlights_shouldReturnStandardResponseAndHideGiaGoc() throws Exception {
        TopCheapFlightResponse flight = new TopCheapFlightResponse(
                1L,
                "VN236",
                new AirlineSummaryDto("VN", "Vietnam Airlines", "/assets/logos/vietnam-airlines.png"),
                new AirportSummaryDto("SGN", "Sân bay Quốc tế Tân Sơn Nhất", "TP Hồ Chí Minh", "Việt Nam"),
                new AirportSummaryDto("HAN", "Sân bay Quốc tế Nội Bài", "Hà Nội", "Việt Nam"),
                new FlightTimeDto("2026-11-15T05:00:00+07:00", "Asia/Ho_Chi_Minh"),
                new FlightTimeDto("2026-11-15T07:00:00+07:00", "Asia/Ho_Chi_Minh"),
                120L,
                LoaiKhoang.PHO_THONG,
                new BigDecimal("1109000.00"),
                new BigDecimal("681000.00"),
                new BigDecimal("1790000.00"),
                "VND",
                145,
                10,
                23,
                TrangThaiChuyenBay.DUNG_GIO
        );
        when(chuyenBayService.getTopCheapFlights(10)).thenReturn(List.of(flight));

        mockMvc.perform(get("/api/v1/flights/top-cheap"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách top chuyến bay giá rẻ thành công"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].maChuyenBay").value("VN236"))
                .andExpect(jsonPath("$.data[0].tongGiaVe").value(1790000.00))
                .andExpect(jsonPath("$.data[0].tienTe").value("VND"))
                .andExpect(jsonPath("$.data[0].thoiGianKhoiHanh.timeZone").value("Asia/Ho_Chi_Minh"))
                .andExpect(jsonPath("$.data[0].giaGoc").doesNotExist());
    }

    @Test
    void getTopCheapFlights_shouldReturnEmptyDataWhenNoFlightsFound() throws Exception {
        when(chuyenBayService.getTopCheapFlights(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/flights/top-cheap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Hiện không có chuyến bay khả dụng"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getTopCheapFlights_shouldReturnRfc9457WhenLimitOutOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/flights/top-cheap").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("limit"));
    }

    @Test
    void getTopCheapFlights_shouldReturnRfc9457WhenLimitExceedsMax() throws Exception {
        mockMvc.perform(get("/api/v1/flights/top-cheap").param("limit", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("limit"));
    }

    @Test
    void getTopCheapFlights_shouldReturnTypeMismatchWhenLimitNotNumber() throws Exception {
        mockMvc.perform(get("/api/v1/flights/top-cheap").param("limit", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.details[0].field").value("limit"))
                .andExpect(jsonPath("$.details[0].code").value("TYPE_MISMATCH"));
    }

    @Test
    void unknownEndpoint_shouldReturnEndpointNotFoundRfc9457() throws Exception {
        mockMvc.perform(get("/api/v1/flights/khong-ton-tai"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    void getTopCheapFlights_shouldReturnRfc9457WhenDatabaseUnavailable() throws Exception {
        when(chuyenBayService.getTopCheapFlights(10))
                .thenThrow(new DataAccessResourceFailureException("Connection refused"));

        mockMvc.perform(get("/api/v1/flights/top-cheap"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    void getTopCheapFlights_shouldReturnDatabaseErrorWhenTransactionFails() throws Exception {
        when(chuyenBayService.getTopCheapFlights(10))
                .thenThrow(new CannotCreateTransactionException("Unable to acquire JDBC Connection"));

        mockMvc.perform(get("/api/v1/flights/top-cheap"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }
}
