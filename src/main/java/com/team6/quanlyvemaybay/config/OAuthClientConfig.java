package com.team6.quanlyvemaybay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "app.oauth2.enabled", havingValue = "true")
public class OAuthClientConfig {
    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${GOOGLE_CLIENT_ID:}") String googleId,
            @Value("${GOOGLE_CLIENT_SECRET:}") String googleSecret,
            @Value("${FACEBOOK_CLIENT_ID:}") String facebookId,
            @Value("${FACEBOOK_CLIENT_SECRET:}") String facebookSecret
    ) {
        List<ClientRegistration> registrations = new ArrayList<>();
        add(registrations, CommonOAuth2Provider.GOOGLE, "google", googleId, googleSecret);
        add(registrations, CommonOAuth2Provider.FACEBOOK, "facebook", facebookId, facebookSecret);
        if (registrations.isEmpty()) {
            throw new IllegalStateException("Bật OAuth nhưng chưa cấu hình nhà cung cấp nào.");
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private void add(List<ClientRegistration> registrations, CommonOAuth2Provider provider,
            String name, String id, String secret) {
        if (id.isBlank() && secret.isBlank()) return;
        if (id.isBlank() || secret.isBlank()) {
            throw new IllegalStateException("Cấu hình OAuth " + name + " thiếu client ID hoặc secret.");
        }
        var builder = provider.getBuilder(name).clientId(id).clientSecret(secret);
        if (provider == CommonOAuth2Provider.FACEBOOK) {
            builder.authorizationUri("https://www.facebook.com/v27.0/dialog/oauth")
                    .tokenUri("https://graph.facebook.com/v27.0/oauth/access_token")
                    .userInfoUri("https://graph.facebook.com/v27.0/me?fields=id,name,email");
        }
        registrations.add(builder.build());
    }
}
