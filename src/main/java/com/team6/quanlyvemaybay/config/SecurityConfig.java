package com.team6.quanlyvemaybay.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            @Value("${app.auth.cookie-secure:false}") boolean cookieSecure
    ) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookiePath("/");
        csrfRepository.setCookieCustomizer(cookie -> cookie.sameSite("Lax").secure(cookieSecure));

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/csrf", "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/flights/**", "/api/v1/public/**",
                                "/api/v1/destinations", "/api/v1/destinations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/chuyen-bay/cua-toi/tra-cuu").permitAll()
                        .requestMatchers("/api/v1/nhan-vien", "/api/v1/nhan-vien/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/reports/**", "/api/v1/statistics/**",
                                "/api/v1/ve-may-bay/**", "/api/v1/tin-tuc/**",
                                "/api/v1/khach-hang", "/api/v1/khach-hang/**")
                        .hasAnyRole("ADMIN", "NHAN_VIEN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/destinations/**")
                        .hasAnyRole("ADMIN", "NHAN_VIEN")
                        .requestMatchers("/api/v1/dat-cho/**")
                        .hasAnyRole("ADMIN", "NHAN_VIEN", "KHACH_HANG")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                            String role = token.getClaimAsString("role");
                            List<SimpleGrantedAuthority> authorities = role == null
                                    ? List.of()
                                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));
                            return new JwtAuthenticationToken(token, authorities);
                        }))
                )
                .addFilterBefore(new CookieAccessTokenFilter(), BearerTokenAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeProblem(objectMapper, request, response, 401,
                                        "UNAUTHORIZED", "Chưa xác thực",
                                        "Cần đăng nhập để truy cập tài nguyên này."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeProblem(objectMapper, request, response, 403,
                                        "ACCESS_DENIED", "Không có quyền truy cập",
                                        "Yêu cầu bị từ chối hoặc thiếu CSRF token hợp lệ."))
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    private BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();
        return request -> {
            String headerToken = headerResolver.resolve(request);
            if (headerToken != null) {
                return headerToken;
            }
            return (String) request.getAttribute(CookieAccessTokenFilter.COOKIE_TOKEN_ATTRIBUTE);
        };
    }

    private static class CookieAccessTokenFilter extends OncePerRequestFilter {
        private static final String COOKIE_TOKEN_ATTRIBUTE = CookieAccessTokenFilter.class.getName() + ".token";

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                Arrays.stream(cookies)
                        .filter(cookie -> "access_token".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .filter(value -> !value.isBlank())
                        .findFirst()
                        .ifPresent(value -> request.setAttribute(COOKIE_TOKEN_ATTRIBUTE, value));
            }
            filterChain.doFilter(request, response);
        }
    }

    private void writeProblem(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String title,
            String detail
    ) throws java.io.IOException {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create("https://api.quanlyvemaybay.vn/errors/" + code.toLowerCase()));
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("details", List.of());
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
