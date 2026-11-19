package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.AvatarUploadSignatureResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CloudinaryAvatarClient {

    public static final String ALLOWED_FORMATS = "jpg,png,webp";
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String CLOUDINARY_API_BASE = "https://api.cloudinary.com/v1_1/";
    private final RestClient restClient;
    private final Clock clock;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String uploadPreset;

    public CloudinaryAvatarClient(
            Clock clock,
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret,
            @Value("${app.cloudinary.avatar-upload-preset:}") String uploadPreset
    ) {
        this.restClient = RestClient.create();
        this.clock = clock;
        this.cloudName = cloudName == null ? "" : cloudName.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.apiSecret = apiSecret == null ? "" : apiSecret.trim();
        this.uploadPreset = uploadPreset == null ? "" : uploadPreset.trim();
    }

    public AvatarUploadSignatureResponse createUploadSignature(Long userId) {
        requireConfiguration();
        String publicId = "avatar_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
        long timestamp = clock.instant().getEpochSecond();
        Map<String, String> parameters = Map.of(
                "allowed_formats", ALLOWED_FORMATS,
                "overwrite", "false",
                "public_id", publicId,
                "timestamp", Long.toString(timestamp),
                "upload_preset", uploadPreset
        );
        String signature = sign(parameters);

        return new AvatarUploadSignatureResponse(
                cloudName,
                apiKey,
                CLOUDINARY_API_BASE + cloudName + "/image/upload",
                timestamp,
                publicId,
                signature,
                uploadPreset,
                ALLOWED_FORMATS,
                MAX_FILE_SIZE
        );
    }

    public CloudinaryAsset getImage(String publicId) {
        requireConfiguration();
        String encodedPublicId = UriUtils.encodePathSegment(publicId, StandardCharsets.UTF_8);
        URI uri = URI.create(CLOUDINARY_API_BASE + cloudName
                + "/resources/image/upload/" + encodedPublicId);
        JsonNode response = get(uri);
        return new CloudinaryAsset(
                response.path("public_id").asString(""),
                response.path("resource_type").asString(""),
                response.path("type").asString(""),
                response.path("format").asString(""),
                response.path("bytes").asLong(-1),
                response.path("secure_url").asString("")
        );
    }

    public void deleteImage(String publicId) {
        requireConfiguration();
        long timestamp = clock.instant().getEpochSecond();
        Map<String, String> parameters = Map.of(
                "invalidate", "true",
                "public_id", publicId,
                "timestamp", Long.toString(timestamp)
        );
        String signature = sign(parameters);
        String body = parameters.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"))
                + "&api_key=" + encode(apiKey)
                + "&signature=" + encode(signature);

        JsonNode response = post(
                URI.create(CLOUDINARY_API_BASE + cloudName + "/image/destroy"),
                body
        );
        String result = response.path("result").asString("");
        if (!"ok".equals(result) && !"not found".equals(result)) {
            throw new CloudinaryApiException(502);
        }
    }

    public String getSecureUrlPrefix() {
        requireConfiguration();
        return "https://res.cloudinary.com/" + cloudName + "/image/upload/";
    }

    private JsonNode get(URI uri) {
        try {
            JsonNode response = restClient.get()
                    .uri(uri)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new CloudinaryApiException(502);
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new CloudinaryApiException(ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new CloudinaryApiException(502);
        }
    }

    private JsonNode post(URI uri, String body) {
        try {
            JsonNode response = restClient.post()
                    .uri(uri)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new CloudinaryApiException(502);
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new CloudinaryApiException(ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new CloudinaryApiException(502);
        }
    }

    private String sign(Map<String, String> parameters) {
        String payload = new TreeMap<>(parameters).entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest((payload + apiSecret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Không hỗ trợ thuật toán SHA-1.", ex);
        }
    }

    private void requireConfiguration() {
        if (cloudName.isBlank() || !cloudName.matches("[A-Za-z0-9_-]+")
                || apiKey.isBlank() || apiSecret.isBlank() || uploadPreset.isBlank()) {
            throw new CloudinaryApiException(0);
        }
    }

    private String encode(String value) {
        return UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
    }

    public record CloudinaryAsset(
            String publicId,
            String resourceType,
            String type,
            String format,
            long bytes,
            String secureUrl
    ) {
    }

    public static class CloudinaryApiException extends RuntimeException {
        private final int statusCode;

        public CloudinaryApiException(int statusCode) {
            super("Không thể hoàn tất yêu cầu Cloudinary.");
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
