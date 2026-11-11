package com.team6.quanlyvemaybay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private VnPayProperties vnpay = new VnPayProperties();
    private PayPalProperties paypal = new PayPalProperties();

    public VnPayProperties getVnpay() {
        return vnpay;
    }

    public void setVnpay(VnPayProperties vnpay) {
        this.vnpay = vnpay;
    }

    public PayPalProperties getPaypal() {
        return paypal;
    }

    public void setPaypal(PayPalProperties paypal) {
        this.paypal = paypal;
    }

    public static class VnPayProperties {
        private String tmnCode = "DTUTEAM6";
        private String hashSecret = "SECRETKEYVNPAYTEST1234567890ABCDEF";
        private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        private String returnUrl = "http://localhost:8080/api/v1/thanh-toan/vnpay/callback";

        public String getTmnCode() {
            return tmnCode;
        }

        public void setTmnCode(String tmnCode) {
            this.tmnCode = tmnCode;
        }

        public String getHashSecret() {
            return hashSecret;
        }

        public void setHashSecret(String hashSecret) {
            this.hashSecret = hashSecret;
        }

        public String getPayUrl() {
            return payUrl;
        }

        public void setPayUrl(String payUrl) {
            this.payUrl = payUrl;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }
    }

    public static class PayPalProperties {
        private String clientId = "test-paypal-client-id";
        private String clientSecret = "test-paypal-client-secret";
        private String mode = "sandbox";
        private String baseUrl = "https://api-m.sandbox.paypal.com";
        private BigDecimal exchangeRateUsd = new BigDecimal("25400");
        private String returnUrl = "http://localhost:8080/api/v1/thanh-toan/paypal/capture";
        private String cancelUrl = "http://localhost:5173/thanh-toan/that-bai";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public BigDecimal getExchangeRateUsd() {
            return exchangeRateUsd;
        }

        public void setExchangeRateUsd(BigDecimal exchangeRateUsd) {
            this.exchangeRateUsd = exchangeRateUsd;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }

        public String getCancelUrl() {
            return cancelUrl;
        }

        public void setCancelUrl(String cancelUrl) {
            this.cancelUrl = cancelUrl;
        }
    }
}
