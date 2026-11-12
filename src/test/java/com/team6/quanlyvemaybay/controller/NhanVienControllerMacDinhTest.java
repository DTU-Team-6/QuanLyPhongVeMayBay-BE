package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.service.NhanVienService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NhanVienController.class)
@AutoConfigureMockMvc(addFilters = false)
class NhanVienControllerMacDinhTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhanVienService service;

    @Test
    void endpointDuocDangKyMacDinh() throws Exception {
        mvc.perform(post("/api/v1/nhan-vien")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/nhan-vien/1")).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/nhan-vien/0")).andExpect(status().isBadRequest());
    }
}
