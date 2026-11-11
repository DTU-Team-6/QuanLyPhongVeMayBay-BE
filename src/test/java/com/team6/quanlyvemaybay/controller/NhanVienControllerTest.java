package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NhanVienController.class, properties = "app.nhan-vien.allow-unsecured=true")
@Import(GlobalExceptionHandler.class)
class NhanVienControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhanVienService service;

    @Test
    void taoNhanVienKhongTraMatKhau() throws Exception {
        when(service.themNhanVien(any(ThemNhanVienRequest.class))).thenReturn(new ThemNhanVienResponse(
                1L, "NV1", "Nguyen An", LocalDate.of(1990, 1, 1), GioiTinh.Nam,
                "an@example.test", "0123456789", VaiTro.NHAN_VIEN, (byte) 1));
        mvc.perform(post("/api/v1/nhan-vien").contentType("application/json").content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/nhan-vien/1"))
                .andExpect(jsonPath("$.data.maNhanVien").value("NV1"))
                .andExpect(jsonPath("$.data.matKhau").doesNotExist());
    }

    @Test
    void tuChoiThongTinKhongHopLe() throws Exception {
        mvc.perform(post("/api/v1/nhan-vien").contentType("application/json")
                        .content(validBody().replace("an@example.test", "khong-phai-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private String validBody() {
        return """
                {"maNhanVien":"NV1","hoTen":"Nguyen An","ngaySinh":"1990-01-01",
                 "gioiTinh":"Nam","email":"an@example.test","matKhau":"Matkhau123",
                 "soDienThoai":"0123456789","vaiTro":"NHAN_VIEN"}
                """;
    }
}
