package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienResponse;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.NhanVienService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NhanVienController.class, properties = "app.nhan-vien.allow-unsecured=true")
@Import(GlobalExceptionHandler.class)
class NhanVienControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhanVienService service;

    @Test
    void suaNhanVienKhongTraMatKhau() throws Exception {
        when(service.suaNhanVien(eq(1L), any(SuaNhanVienRequest.class))).thenReturn(new SuaNhanVienResponse(
                1L, "NV1", "Nguyen Van Moi", LocalDate.of(1990, 1, 1), GioiTinh.Nam,
                "moi@example.test", "0901234567", VaiTro.NHAN_VIEN, (byte) 1));
        mvc.perform(put("/api/v1/nhan-vien/1").contentType("application/json").content(validBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maNhanVien").value("NV1"))
                .andExpect(jsonPath("$.data.matKhau").doesNotExist());
    }

    @Test
    void emailSaiTraLoiValidation() throws Exception {
        mvc.perform(put("/api/v1/nhan-vien/1").contentType("application/json")
                        .content(validBody().replace("moi@example.test", "sai-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private String validBody() {
        return """
                {"hoTen":"Nguyen Van Moi","ngaySinh":"1990-01-01","gioiTinh":"Nam",
                 "email":"moi@example.test","soDienThoai":"0901234567","vaiTro":"NHAN_VIEN"}
                """;
    }
}
