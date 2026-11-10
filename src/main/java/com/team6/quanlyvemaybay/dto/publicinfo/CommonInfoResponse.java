package com.team6.quanlyvemaybay.dto.publicinfo;

import java.util.List;

/**
 * DTO tổng hợp thông tin dùng chung cho Header, Footer và banner thông báo Trang chủ.
 */
public record CommonInfoResponse(
        String brandName,
        String slogan,
        String logoUrl,
        String copyright,
        ContactInfoDto contact,
        SocialLinksDto socialLinks,
        List<PolicyLinkDto> policyLinks,
        List<BannerAnnouncementDto> banners
) {
}
