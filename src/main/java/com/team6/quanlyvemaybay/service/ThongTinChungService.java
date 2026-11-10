package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.CommonInfoProperties;
import com.team6.quanlyvemaybay.config.TimeConfig;
import com.team6.quanlyvemaybay.dto.publicinfo.BannerAnnouncementDto;
import com.team6.quanlyvemaybay.dto.publicinfo.CommonInfoResponse;
import com.team6.quanlyvemaybay.dto.publicinfo.ContactInfoDto;
import com.team6.quanlyvemaybay.dto.publicinfo.PolicyLinkDto;
import com.team6.quanlyvemaybay.dto.publicinfo.SocialLinksDto;
import com.team6.quanlyvemaybay.entity.TinTuc;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import com.team6.quanlyvemaybay.repository.TinTucRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service tổng hợp cấu hình hiển thị Header/Footer từ application.yml
 * và danh sách banner/thông báo động đang bật từ bảng tin_tuc.
 */
@Service
@Transactional(readOnly = true)
public class ThongTinChungService {

    private static final int DEFAULT_BANNER_LIMIT = 5;
    private static final List<LoaiTinTuc> HEADER_BANNER_TYPES = List.of(
            LoaiTinTuc.THONG_BAO,
            LoaiTinTuc.KHUYEN_MAI
    );

    private final CommonInfoProperties properties;
    private final TinTucRepository tinTucRepository;

    public ThongTinChungService(CommonInfoProperties properties, TinTucRepository tinTucRepository) {
        this.properties = properties;
        this.tinTucRepository = tinTucRepository;
    }

    /**
     * Trả về thông tin dùng chung cho Header/Footer cùng tối đa 5 banner/thông báo đang hiển thị.
     */
    public CommonInfoResponse getCommonInfo() {
        CommonInfoProperties.ContactProperties contactProps = properties.getContact();
        ContactInfoDto contact = new ContactInfoDto(
                contactProps.getHotline(),
                contactProps.getSupportPhone(),
                contactProps.getEmail(),
                contactProps.getAddress(),
                contactProps.getWorkingHours()
        );

        CommonInfoProperties.SocialLinksProperties socialProps = properties.getSocialLinks();
        SocialLinksDto socialLinks = new SocialLinksDto(
                socialProps.getFacebook(),
                socialProps.getZalo(),
                socialProps.getYoutube()
        );

        List<PolicyLinkDto> policyLinks = properties.getPolicyLinks().stream()
                .map(link -> new PolicyLinkDto(link.getTitle(), link.getUrl()))
                .toList();

        List<BannerAnnouncementDto> banners = tinTucRepository
                .findByTrangThaiAndLoaiTinInOrderByThoiGianTaoDesc(
                        TrangThaiTinTuc.HIEN_THI,
                        HEADER_BANNER_TYPES,
                        PageRequest.of(0, DEFAULT_BANNER_LIMIT)
                )
                .stream()
                .map(this::toBannerDto)
                .toList();

        return new CommonInfoResponse(
                properties.getBrandName(),
                properties.getSlogan(),
                properties.getLogoUrl(),
                properties.getCopyright(),
                contact,
                socialLinks,
                policyLinks,
                banners
        );
    }

    private BannerAnnouncementDto toBannerDto(TinTuc tinTuc) {
        String formattedTime = tinTuc.getThoiGianTao() != null
                ? tinTuc.getThoiGianTao().atZone(TimeConfig.DEFAULT_VN_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;
        return new BannerAnnouncementDto(
                tinTuc.getId(),
                tinTuc.getLoaiTin(),
                tinTuc.getTieuDe(),
                tinTuc.getAnhDaiDien(),
                formattedTime
        );
    }
}
