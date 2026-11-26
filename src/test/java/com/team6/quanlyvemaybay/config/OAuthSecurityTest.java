package com.team6.quanlyvemaybay.config;

import com.team6.quanlyvemaybay.controller.AuthController;
import com.team6.quanlyvemaybay.service.AuthService;
import com.team6.quanlyvemaybay.service.CloudinaryAvatarService;
import com.team6.quanlyvemaybay.service.JwtTokenService;
import com.team6.quanlyvemaybay.service.OAuthAccountService;
import com.team6.quanlyvemaybay.service.PhienLamMoiService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, OAuthClientConfig.class, OAuthLoginHandler.class})
@TestPropertySource(properties = {
        "app.oauth2.enabled=true",
        "app.frontend-url=http://localhost:5173",
        "GOOGLE_CLIENT_ID=test-id",
        "GOOGLE_CLIENT_SECRET=test-secret"
})
class OAuthSecurityTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private CloudinaryAvatarService cloudinaryAvatarService;
    @MockitoBean private PhienLamMoiService phienLamMoiService;
    @MockitoBean private OAuthAccountService accountService;
    @MockitoBean private JwtTokenService jwtTokenService;
    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private Clock clock;

    @Test
    void batDauOAuthKhongBiChanBoiCookieCu() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google").cookie(new Cookie("access_token", "expired")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("accounts.google.com")))
                .andExpect(header().string("Location", containsString("state=")));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void callbackKhongCoStateHopLeKhongDuocDangNhap() throws Exception {
        mockMvc.perform(get("/login/oauth2/code/google").param("code", "fake").param("state", "fake"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("error=OAUTH_FAILED")));
        verifyNoInteractions(accountService);
    }
}
