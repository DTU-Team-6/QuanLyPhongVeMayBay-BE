package com.team6.quanlyvemaybay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Ánh xạ cấu hình thông tin dùng chung (Header/Footer) từ application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "app.common-info")
public class CommonInfoProperties {

    private String brandName;
    private String slogan;
    private String logoUrl;
    private String copyright;
    private ContactProperties contact = new ContactProperties();
    private SocialLinksProperties socialLinks = new SocialLinksProperties();
    private List<PolicyLinkProperties> policyLinks = new ArrayList<>();

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public ContactProperties getContact() {
        return contact;
    }

    public void setContact(ContactProperties contact) {
        this.contact = contact;
    }

    public SocialLinksProperties getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(SocialLinksProperties socialLinks) {
        this.socialLinks = socialLinks;
    }

    public List<PolicyLinkProperties> getPolicyLinks() {
        return policyLinks;
    }

    public void setPolicyLinks(List<PolicyLinkProperties> policyLinks) {
        this.policyLinks = policyLinks;
    }

    public static class ContactProperties {
        private String hotline;
        private String supportPhone;
        private String email;
        private String address;
        private String workingHours;

        public String getHotline() {
            return hotline;
        }

        public void setHotline(String hotline) {
            this.hotline = hotline;
        }

        public String getSupportPhone() {
            return supportPhone;
        }

        public void setSupportPhone(String supportPhone) {
            this.supportPhone = supportPhone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getWorkingHours() {
            return workingHours;
        }

        public void setWorkingHours(String workingHours) {
            this.workingHours = workingHours;
        }
    }

    public static class SocialLinksProperties {
        private String facebook;
        private String zalo;
        private String youtube;

        public String getFacebook() {
            return facebook;
        }

        public void setFacebook(String facebook) {
            this.facebook = facebook;
        }

        public String getZalo() {
            return zalo;
        }

        public void setZalo(String zalo) {
            this.zalo = zalo;
        }

        public String getYoutube() {
            return youtube;
        }

        public void setYoutube(String youtube) {
            this.youtube = youtube;
        }
    }

    public static class PolicyLinkProperties {
        private String title;
        private String url;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
