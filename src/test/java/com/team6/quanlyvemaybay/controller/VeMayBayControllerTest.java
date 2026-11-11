package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.ticket.VeDanhSachResponse;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.VeMayBayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VeMayBayController.class, properties = "app.ve.allow-unsecured=true")
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class VeMayBayControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean VeMayBayService service;

    @Test
    void traDanhSachKhongLoGiaGoc() throws Exception {
        var ve = new VeDanhSachResponse(1L, "VE001", "Nguyen Test", "TESTPNR1", "TT101",
                "AAA", "BBB", LocalDate.of(2026, 12, 1), new BigDecimal("900000.00"),
                TrangThaiVe.DA_THANH_TOAN);
        when(service.danhSachVe(null, null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(ve), 0, 20, 1, 1, true, true));
        mvc.perform(get("/api/v1/ve-may-bay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].tongGia").value(900000))
                .andExpect(jsonPath("$.data.content[0].giaGoc").doesNotExist());
    }

    @Test
    void thamSoSaiTra400() throws Exception {
        mvc.perform(get("/api/v1/ve-may-bay").param("kichThuoc", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
