package com.team6.quanlyvemaybay.config;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.service.JwtTokenService;
import com.team6.quanlyvemaybay.service.OAuthAccountService;
import com.team6.quanlyvemaybay.service.PhienLamMoiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginHandlerTest {
    @Mock private OAuthAccountService accountService;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private PhienLamMoiService phienLamMoiService;
    private OAuthLoginHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuthLoginHandler(accountService, jwtTokenService, phienLamMoiService,
                false, "http://localhost:5173");
    }

    @Test
    void facebookCapCookieHttpOnlyVaChuyenVeFe() throws Exception {
        NguoiDung user = new NguoiDung();
        user.setId(42L);
        when(accountService.xacThuc("facebook", "123", "test@example.com", "Tên")).thenReturn(user);
        when(jwtTokenService.createAccessToken(user)).thenReturn("access-value");
        when(phienLamMoiService.tao(42L, false))
                .thenReturn(new PhienLamMoiService.KetQua("refresh-value", null, Instant.now(), false));
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.success().onAuthenticationSuccess(new MockHttpServletRequest(), response, facebook());
        assertEquals("http://localhost:5173/auth/callback", response.getRedirectedUrl());
        assertEquals(2, response.getHeaders("Set-Cookie").size());
        assertTrue(response.getHeaders("Set-Cookie").stream()
                .allMatch(cookie -> cookie.contains("HttpOnly") && cookie.contains("SameSite=Lax")
                        && !cookie.contains("Max-Age")));
    }

    @Test
    void emailTrungBaoLoiKhongCapCookie() throws Exception {
        when(accountService.xacThuc("facebook", "123", "test@example.com", "Tên"))
                .thenThrow(new BusinessException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "trùng"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.success().onAuthenticationSuccess(new MockHttpServletRequest(), response, facebook());
        assertEquals("http://localhost:5173/auth/callback?error=EMAIL_ALREADY_REGISTERED", response.getRedirectedUrl());
        assertTrue(response.getHeaders("Set-Cookie").isEmpty());
        verifyNoInteractions(phienLamMoiService);
    }

    private OAuth2AuthenticationToken facebook() {
        var principal = new DefaultOAuth2User(AuthorityUtils.createAuthorityList("ROLE_USER"),
                Map.of("id", "123", "email", "test@example.com", "name", "Tên"), "id");
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "facebook");
    }
}
