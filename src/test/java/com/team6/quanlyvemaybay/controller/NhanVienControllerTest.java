package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.nhanvien.XoaNhanVienResponse;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.NhanVienService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NhanVienController.class, properties = "app.nhan-vien.allow-unsecured=true")
@Import(GlobalExceptionHandler.class)
class NhanVienControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhanVienService service;

    @Test
    void ngungHoatDongTraTrangThaiMoi() throws Exception {
        when(service.xoaNhanVien(1L)).thenReturn(new XoaNhanVienResponse(1L, "NV1", (byte) 0));
        mvc.perform(delete("/api/v1/nhan-vien/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maNhanVien").value("NV1"))
                .andExpect(jsonPath("$.data.trangThai").value(0));
    }

    @Test
    void idKhongHopLeTra400() throws Exception {
        mvc.perform(delete("/api/v1/nhan-vien/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
