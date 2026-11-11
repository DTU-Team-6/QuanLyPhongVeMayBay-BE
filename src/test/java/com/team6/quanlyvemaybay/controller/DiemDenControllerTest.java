package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.destination.DestinationResponse;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.service.DiemDenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử WebMvc cho các endpoint GET và POST /api/v1/destinations.
 */
@WebMvcTest(controllers = DiemDenController.class)
@Import(GlobalExceptionHandler.class)
class DiemDenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiemDenService diemDenService;

    @Test
    void getDestinations_shouldReturnPagedDestinations() throws Exception {
        DestinationResponse item = new DestinationResponse(
                1L,
                "Bà Nà Hills & Cầu Vàng",
                "DAD",
                "Sân bay Quốc tế Đà Nẵng",
                "Đà Nẵng",
                "Việt Nam",
                "Thánh địa du lịch nổi tiếng.",
                "/assets/destinations/bana.jpg"
        );
        PageResponse<DestinationResponse> pageResponse = new PageResponse<>(
                List.of(item), 0, 10, 1L, 1, true, true
        );
        when(diemDenService.getDestinations(eq("Bà Nà"), eq("DAD"), eq(0), eq(10))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/destinations")
                        .param("keyword", "Bà Nà")
                        .param("maSanBay", "DAD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách điểm đến thành công"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].tenDiemDen").value("Bà Nà Hills & Cầu Vàng"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getDestinations_shouldReturnEmptyMessageWhenNoData() throws Exception {
        PageResponse<DestinationResponse> emptyPage = new PageResponse<>(
                List.of(), 0, 10, 0L, 0, true, true
        );
        when(diemDenService.getDestinations(any(), any(), eq(0), eq(10))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/destinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Không tìm thấy điểm đến phù hợp"))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void getDestinations_shouldRejectInvalidPageOrSize() throws Exception {
        mockMvc.perform(get("/api/v1/destinations").param("page", "-1").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(2)));
    }

    @Test
    void createDestination_shouldReturnCreatedWhenPayloadValid() throws Exception {
        DestinationResponse created = new DestinationResponse(
                4L,
                "Cầu Rồng Đà Nẵng",
                "DAD",
                "Sân bay Quốc tế Đà Nẵng",
                "Đà Nẵng",
                "Việt Nam",
                "Biểu tượng nổi tiếng bên bờ sông Hàn.",
                "/assets/destinations/caurong.jpg"
        );
        when(diemDenService.createDestination(any())).thenReturn(created);

        String body = """
                {
                  "tenDiemDen": "Cầu Rồng Đà Nẵng",
                  "maSanBay": "DAD",
                  "moTa": "Biểu tượng nổi tiếng bên bờ sông Hàn.",
                  "hinhAnh": "/assets/destinations/caurong.jpg"
                }
                """;

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Thêm mới điểm đến thành công"))
                .andExpect(jsonPath("$.data.id").value(4))
                .andExpect(jsonPath("$.data.tenDiemDen").value("Cầu Rồng Đà Nẵng"))
                .andExpect(jsonPath("$.data.maSanBay").value("DAD"));
    }

    @Test
    void createDestination_shouldRejectWhenMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details[*].field", hasItem("tenDiemDen")))
                .andExpect(jsonPath("$.details[*].field", hasItem("maSanBay")));
    }

    @Test
    void createDestination_shouldRejectWhenNameExceedsMaxLength() throws Exception {
        String longName = "A".repeat(101);
        String body = """
                {
                  "tenDiemDen": "%s",
                  "maSanBay": "DAD"
                }
                """.formatted(longName);

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("tenDiemDen"));
    }

    @Test
    void createDestination_shouldReturnNotFoundWhenAirportDoesNotExist() throws Exception {
        when(diemDenService.createDestination(any()))
                .thenThrow(new ResourceNotFoundException("AIRPORT_NOT_FOUND", "Không tìm thấy sân bay với mã: XYZ"));

        String body = """
                {
                  "tenDiemDen": "Đảo Ngọc",
                  "maSanBay": "XYZ"
                }
                """;

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("AIRPORT_NOT_FOUND"));
    }

    @Test
    void createDestination_shouldReturnConflictWhenDestinationDuplicate() throws Exception {
        when(diemDenService.createDestination(any()))
                .thenThrow(new BusinessException(
                        HttpStatus.CONFLICT,
                        "DESTINATION_ALREADY_EXISTS",
                        "Điểm đến 'Bà Nà Hills & Cầu Vàng' đã tồn tại tại sân bay DAD",
                        List.of(new ErrorDetail("tenDiemDen", "DUPLICATE_DESTINATION", "Điểm đến đã tồn tại"))
                ));

        String body = """
                {
                  "tenDiemDen": "Bà Nà Hills & Cầu Vàng",
                  "maSanBay": "DAD"
                }
                """;

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DESTINATION_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.details[0].field").value("tenDiemDen"));
    }

    @Test
    void createDestination_shouldRejectMalformedJsonBody() throws Exception {
        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }
}
