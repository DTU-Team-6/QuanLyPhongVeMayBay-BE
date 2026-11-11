package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.nhanvien.DanhSachNhanVienResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.NhanVienResponse;
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
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NhanVienController.class, properties = "app.nhan-vien.allow-unsecured=true")
@Import(GlobalExceptionHandler.class)
class NhanVienControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhanVienService service;

    @Test
    void traDanhSachKhongLoMatKhau() throws Exception {
        var nv = new NhanVienResponse(1L, "NV1", "Nguyễn An", LocalDate.of(1990, 1, 1),
                GioiTinh.Nam, "an@example.com", "0123456789", VaiTro.NHAN_VIEN, (byte) 1);
        when(service.danhSach(null, null, 0, 20))
                .thenReturn(new DanhSachNhanVienResponse(List.of(nv), 0, 20, 1, 1));
        mvc.perform(get("/api/v1/nhan-vien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nhanVien[0].maNhanVien").value("NV1"))
                .andExpect(jsonPath("$.data.nhanVien[0].matKhau").doesNotExist())
                .andExpect(jsonPath("$.data.tongSo").value(1));
    }

    @Test
    void tuChoiDuLieuKhongHopLe() throws Exception {
        mvc.perform(get("/api/v1/nhan-vien").param("kichThuoc", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/nhan-vien").param("tieuChi", "KHONG_HOP_LE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
