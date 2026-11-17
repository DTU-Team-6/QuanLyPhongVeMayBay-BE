package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.config.SecurityConfig;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.destination.DestinationResponse;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.DiemDenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiemDenController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DiemDenSecurityTest {
    private static final String BODY = """
            {"tenDiemDen":"Cầu Rồng", "maSanBay":"DAD"}
            """;
    @Autowired private MockMvc mockMvc;
    @MockitoBean private DiemDenService diemDenService;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void listAndAirportCatalogArePublic() throws Exception {
        when(diemDenService.getDestinations(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0L, 0, true, true));
        when(diemDenService.getAirports()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/destinations")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/destinations/airports")).andExpect(status().isOk());
    }

    @Test
    void createRequiresLoginEvenWithCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/destinations")
                        .cookie(new Cookie("XSRF-TOKEN", "csrf-value")).header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(diemDenService);
    }

    @Test
    void customerCannotCreateEvenWithCsrf() throws Exception {
        when(jwtDecoder.decode("customer-token")).thenReturn(token("customer-token", "KHACH_HANG"));
        mockMvc.perform(post("/api/v1/destinations")
                        .cookie(new Cookie("access_token", "customer-token"), new Cookie("XSRF-TOKEN", "csrf-value"))
                        .header("X-XSRF-TOKEN", "csrf-value")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        verifyNoInteractions(diemDenService);
    }

    @Test
    void staffAndAdminCanCreateWithCsrf() throws Exception {
        when(diemDenService.createDestination(any())).thenReturn(new DestinationResponse(
                1L, "Cầu Rồng", "DAD", "Sân bay Đà Nẵng", "Đà Nẵng", "Việt Nam", null, null));
        for (String role : List.of("NHAN_VIEN", "ADMIN")) {
            String accessToken = role + "-token";
            when(jwtDecoder.decode(accessToken)).thenReturn(token(accessToken, role));
            mockMvc.perform(post("/api/v1/destinations")
                            .cookie(new Cookie("access_token", accessToken), new Cookie("XSRF-TOKEN", "csrf-value"))
                            .header("X-XSRF-TOKEN", "csrf-value")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Test
    void createStillRejectsStaffWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/destinations")
                        .cookie(new Cookie("access_token", "staff-token"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        verifyNoInteractions(diemDenService);
    }

    private Jwt token(String value, String role) {
        return Jwt.withTokenValue(value).header("alg", "HS256").subject("2").claim("role", role).build();
    }
}
