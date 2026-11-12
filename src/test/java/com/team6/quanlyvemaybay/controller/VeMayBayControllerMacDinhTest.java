package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.service.VeMayBayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VeMayBayController.class)
@AutoConfigureMockMvc(addFilters = false)
class VeMayBayControllerMacDinhTest {
    @Autowired MockMvc mvc;
    @MockitoBean VeMayBayService service;

    @Test
    void endpointTatMacDinh() throws Exception {
        mvc.perform(get("/api/v1/ve-may-bay")).andExpect(status().isNotFound());
    }
}
