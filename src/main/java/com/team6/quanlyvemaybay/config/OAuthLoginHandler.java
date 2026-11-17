package com.team6.quanlyvemaybay.config;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.service.JwtTokenService;
import com.team6.quanlyvemaybay.service.OAuthAccountService;
import com.team6.quanlyvemaybay.service.PhienLamMoiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.oauth2.enabled", havingValue = "true")
public class OAuthLoginHandler {
    private final OAuthAccountService accountService;
    private final JwtTokenService jwtTokenService;
    private final PhienLamMoiService phienLamMoiService;
    private final boolean cookieSecure;
    private final String frontendUrl;

    public OAuthLoginHandler(OAuthAccountService accountService, JwtTokenService jwtTokenService,
            PhienLamMoiService phienLamMoiService,
            @Value("${app.auth.cookie-secure:false}") boolean cookieSecure,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.accountService = accountService;
        this.jwtTokenService = jwtTokenService;
        this.phienLamMoiService = phienLamMoiService;
        this.cookieSecure = cookieSecure;
        this.frontendUrl = frontendUrl.split(",")[0].trim();
    }

    public AuthenticationSuccessHandler success() {
        return this::onSuccess;
    }

    public AuthenticationFailureHandler failure() {
        return (request, response, error) -> redirect(response, "OAUTH_FAILED");
    }

    private void onSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String provider = oauth.getAuthorizedClientRegistrationId();
        var principal = oauth.getPrincipal();
        try {
            if ("google".equals(provider)
                    && (!(principal instanceof OidcUser oidcUser) || !Boolean.TRUE.equals(oidcUser.getEmailVerified()))) {
                redirect(response, "OAUTH_PROFILE_INVALID");
                return;
            }
            NguoiDung user = accountService.xacThuc(provider, principal.getName(),
                    principal.getAttribute("email"), principal.getAttribute("name"));
            String accessToken = jwtTokenService.createAccessToken(user);
            PhienLamMoiService.KetQua session = phienLamMoiService.tao(user.getId(), false);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie("access_token", accessToken).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, cookie("refresh_token", session.refreshToken()).toString());
            redirect(response, null);
        } catch (BusinessException error) {
            String code = Set.of("EMAIL_ALREADY_REGISTERED", "OAUTH_PROFILE_INVALID", "ACCOUNT_INACTIVE")
                    .contains(error.getCode()) ? error.getCode() : "OAUTH_FAILED";
            redirect(response, code);
        }
    }

    private ResponseCookie cookie(String name, String value) {
        return ResponseCookie.from(name, value).httpOnly(true).secure(cookieSecure)
                .sameSite("Lax").path("/").build();
    }

    private void redirect(HttpServletResponse response, String error) throws IOException {
        var target = UriComponentsBuilder.fromUriString(frontendUrl).replacePath("/auth/callback").replaceQuery(null);
        if (error != null) target.queryParam("error", error);
        response.sendRedirect(target.build().toUriString());
    }
}
