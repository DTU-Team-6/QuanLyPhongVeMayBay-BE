package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.publicinfo.BannerAnnouncementDto;
import com.team6.quanlyvemaybay.dto.publicinfo.CommonInfoResponse;
import com.team6.quanlyvemaybay.dto.publicinfo.ContactInfoDto;
import com.team6.quanlyvemaybay.dto.publicinfo.PolicyLinkDto;
import com.team6.quanlyvemaybay.dto.publicinfo.SocialLinksDto;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.ThongTinChungService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử WebMvc cho API GET /api/v1/public/common-info.
 */
@WebMvcTest(controllers = CongKhaiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CongKhaiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThongTinChungService thongTinChungService;

    @Test
    void getCommonInfo_shouldReturnStandardResponseWithHeaderFooterAndBanners() throws Exception {
        CommonInfoResponse response = new CommonInfoResponse(
                "DTU Team 6 Airlines",
                "Bay an toàn",
                "/assets/logos/brand-logo.png",
                "© 2026 DTU Team 6",
                new ContactInfoDto("1900 1234", "0236 3827 111", "support@dtuteam6airlines.vn", "Đà Nẵng", "07:00 - 22:00"),
                new SocialLinksDto("https://facebook.com", "https://zalo.me", "https://youtube.com"),
                List.of(new PolicyLinkDto("Điều khoản sử dụng", "/dieu-khoan-su-dung")),
                List.of(new BannerAnnouncementDto(1L, LoaiTinTuc.KHUYEN_MAI, "Chào hè rực rỡ", "/assets/news/promo1.jpg", "2026-11-01T08:00:00+07:00"))
        );
        when(thongTinChungService.getCommonInfo()).thenReturn(response);

        mockMvc.perform(get("/api/v1/public/common-info"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy thông tin dùng chung thành công"))
                .andExpect(jsonPath("$.data.brandName").value("DTU Team 6 Airlines"))
                .andExpect(jsonPath("$.data.contact.hotline").value("1900 1234"))
                .andExpect(jsonPath("$.data.policyLinks", hasSize(1)))
                .andExpect(jsonPath("$.data.banners", hasSize(1)))
                .andExpect(jsonPath("$.data.banners[0].tieuDe").value("Chào hè rực rỡ"));
    }

    @Test
    void getCommonInfo_shouldReturnEmptyBannersWhenNoDynamicNews() throws Exception {
        CommonInfoResponse response = new CommonInfoResponse(
                "DTU Team 6 Airlines",
                "Bay an toàn",
                "/assets/logos/brand-logo.png",
                "© 2026 DTU Team 6",
                new ContactInfoDto("1900 1234", "0236 3827 111", "support@dtuteam6airlines.vn", "Đà Nẵng", "07:00 - 22:00"),
                new SocialLinksDto("https://facebook.com", "https://zalo.me", "https://youtube.com"),
                List.of(),
                List.of()
        );
        when(thongTinChungService.getCommonInfo()).thenReturn(response);

        mockMvc.perform(get("/api/v1/public/common-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.banners", hasSize(0)));
    }

    @Test
    void getCommonInfo_shouldReturnRfc9457WhenDatabaseUnavailable() throws Exception {
        when(thongTinChungService.getCommonInfo())
                .thenThrow(new DataAccessResourceFailureException("Connection refused"));

        mockMvc.perform(get("/api/v1/public/common-info"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }
}
