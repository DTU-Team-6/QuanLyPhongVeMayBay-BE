package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.CommonInfoProperties;
import com.team6.quanlyvemaybay.dto.publicinfo.CommonInfoResponse;
import com.team6.quanlyvemaybay.entity.TinTuc;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import com.team6.quanlyvemaybay.repository.TinTucRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử đơn vị cho ThongTinChungService.
 */
@ExtendWith(MockitoExtension.class)
class ThongTinChungServiceTest {

    @Mock
    private TinTucRepository tinTucRepository;

    private CommonInfoProperties properties;
    private ThongTinChungService service;

    @BeforeEach
    void setUp() {
        properties = new CommonInfoProperties();
        properties.setBrandName("DTU Team 6 Airlines");
        properties.setSlogan("Bay an toàn");
        properties.setLogoUrl("/assets/logos/brand-logo.png");
        properties.setCopyright("© 2026 DTU Team 6");

        CommonInfoProperties.ContactProperties contact = new CommonInfoProperties.ContactProperties();
        contact.setHotline("1900 1234");
        contact.setEmail("support@dtuteam6airlines.vn");
        properties.setContact(contact);

        CommonInfoProperties.PolicyLinkProperties policy = new CommonInfoProperties.PolicyLinkProperties();
        policy.setTitle("Giới thiệu");
        policy.setUrl("/tin-tuc?loai=GIOI_THIEU");
        properties.setPolicyLinks(List.of(policy));

        service = new ThongTinChungService(properties, tinTucRepository);
    }

    @Test
    void getCommonInfo_shouldCombineStaticConfigAndActiveBanners() {
        TinTuc promo = new TinTuc();
        promo.setId(1L);
        promo.setLoaiTin(LoaiTinTuc.KHUYEN_MAI);
        promo.setTieuDe("Giảm 20% vé nội địa");
        promo.setAnhDaiDien("/assets/news/promo1.jpg");
        promo.setTrangThai(TrangThaiTinTuc.HIEN_THI);
        promo.setThoiGianTao(LocalDateTime.of(2026, 11, 1, 8, 0));

        when(tinTucRepository.findByTrangThaiAndLoaiTinInOrderByThoiGianTaoDesc(
                eq(TrangThaiTinTuc.HIEN_THI),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of(promo));

        CommonInfoResponse result = service.getCommonInfo();

        assertThat(result.brandName()).isEqualTo("DTU Team 6 Airlines");
        assertThat(result.contact().hotline()).isEqualTo("1900 1234");
        assertThat(result.policyLinks()).hasSize(1);
        assertThat(result.banners()).hasSize(1);
        assertThat(result.banners().getFirst().tieuDe()).isEqualTo("Giảm 20% vé nội địa");
        assertThat(result.banners().getFirst().thoiGianTao()).isEqualTo("2026-11-01T08:00:00+07:00");
    }

    @Test
    void getCommonInfo_shouldReturnEmptyBannersWhenNoActiveNews() {
        when(tinTucRepository.findByTrangThaiAndLoaiTinInOrderByThoiGianTaoDesc(
                eq(TrangThaiTinTuc.HIEN_THI),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());

        CommonInfoResponse result = service.getCommonInfo();

        assertThat(result.brandName()).isEqualTo("DTU Team 6 Airlines");
        assertThat(result.banners()).isEmpty();
    }
}
