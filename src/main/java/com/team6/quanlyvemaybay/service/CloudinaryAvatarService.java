package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.AvatarUpdateResponse;
import com.team6.quanlyvemaybay.dto.auth.AvatarUploadSignatureResponse;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.service.CloudinaryAvatarClient.CloudinaryApiException;
import com.team6.quanlyvemaybay.service.CloudinaryAvatarClient.CloudinaryAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.Locale;

@Service
public class CloudinaryAvatarService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryAvatarService.class);
    private static final Set<String> ALLOWED_FORMATS = Set.of("jpg", "jpeg", "png", "webp");
    private static final String AVATAR_PUBLIC_ID_PREFIX = "avatar_";

    private final AuthService authService;
    private final NguoiDungRepository nguoiDungRepository;
    private final CloudinaryAvatarClient cloudinaryAvatarClient;

    public CloudinaryAvatarService(
            AuthService authService,
            NguoiDungRepository nguoiDungRepository,
            CloudinaryAvatarClient cloudinaryAvatarClient
    ) {
        this.authService = authService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.cloudinaryAvatarClient = cloudinaryAvatarClient;
    }

    public AvatarUploadSignatureResponse createUploadSignature(Long userId) {
        authService.currentUser(userId);
        try {
            return cloudinaryAvatarClient.createUploadSignature(userId);
        } catch (CloudinaryApiException ex) {
            throw cloudinaryUnavailable();
        }
    }

    public AvatarUpdateResponse updateAvatar(Long userId, String publicId) {
        authService.currentUser(userId);
        if (!isOwnedPublicId(userId, publicId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AVATAR_REFERENCE",
                    "Ảnh đại diện không thuộc phiên tải lên của tài khoản.");
        }

        CloudinaryAsset asset;
        try {
            asset = cloudinaryAvatarClient.getImage(publicId);
        } catch (CloudinaryApiException ex) {
            if (ex.getStatusCode() == 404) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "AVATAR_ASSET_NOT_FOUND",
                        "Không tìm thấy ảnh vừa tải lên trên Cloudinary.");
            }
            throw cloudinaryUnavailable();
        }

        try {
            validateAsset(publicId, asset);
        } catch (BusinessException ex) {
            deleteUnassociatedAsset(publicId);
            throw ex;
        }
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                        "Phiên đăng nhập không hợp lệ."));
        String oldAvatar = nguoiDung.getAnhDaiDien();
        String oldPublicId = nguoiDung.getAnhDaiDienPublicId();
        nguoiDung.setAnhDaiDien(asset.secureUrl());
        nguoiDung.setAnhDaiDienPublicId(asset.publicId());

        try {
            nguoiDungRepository.saveAndFlush(nguoiDung);
        } catch (DataAccessException ex) {
            deleteUnassociatedAsset(publicId);
            throw ex;
        }

        AvatarUpdateResponse.OldAvatarCleanupStatus cleanupStatus =
                cleanupPreviousAvatar(userId, oldAvatar, oldPublicId, publicId);
        return new AvatarUpdateResponse(authService.currentUser(userId), cleanupStatus);
    }

    private void validateAsset(String expectedPublicId, CloudinaryAsset asset) {
        if (asset == null
                || asset.format() == null
                || asset.secureUrl() == null
                || !expectedPublicId.equals(asset.publicId())
                || !"image".equals(asset.resourceType())
                || !"upload".equals(asset.type())
                || !ALLOWED_FORMATS.contains(asset.format().toLowerCase(Locale.ROOT))
                || asset.bytes() <= 0
                || asset.bytes() > CloudinaryAvatarClient.MAX_FILE_SIZE
                || asset.secureUrl().length() > 255
                || !asset.secureUrl().startsWith(cloudinaryAvatarClient.getSecureUrlPrefix())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AVATAR_ASSET",
                    "Ảnh phải là JPEG, PNG hoặc WebP và không vượt quá 5 MB.");
        }
    }

    private AvatarUpdateResponse.OldAvatarCleanupStatus cleanupPreviousAvatar(
            Long userId,
            String oldAvatar,
            String oldPublicId,
            String newPublicId
    ) {
        if (oldAvatar == null || oldAvatar.isBlank() || newPublicId.equals(oldPublicId)) {
            return AvatarUpdateResponse.OldAvatarCleanupStatus.NOT_REQUIRED;
        }
        if (oldPublicId == null || !isOwnedPublicId(userId, oldPublicId)) {
            return AvatarUpdateResponse.OldAvatarCleanupStatus.LEGACY_NOT_TRACKED;
        }

        try {
            cloudinaryAvatarClient.deleteImage(oldPublicId);
            return AvatarUpdateResponse.OldAvatarCleanupStatus.DELETED;
        } catch (CloudinaryApiException ex) {
            log.warn("Không thể xóa avatar Cloudinary cũ của người dùng {}.", userId);
            return AvatarUpdateResponse.OldAvatarCleanupStatus.FAILED;
        }
    }

    private void deleteUnassociatedAsset(String publicId) {
        try {
            cloudinaryAvatarClient.deleteImage(publicId);
        } catch (CloudinaryApiException ex) {
            log.warn("Không thể dọn ảnh Cloudinary chưa gắn với hồ sơ.");
        }
    }

    private boolean isOwnedPublicId(Long userId, String publicId) {
        return publicId != null
                && publicId.matches("^" + AVATAR_PUBLIC_ID_PREFIX + userId + "_[0-9a-f]{32}$");
    }

    private BusinessException cloudinaryUnavailable() {
        return new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "CLOUDINARY_UNAVAILABLE",
                "Dịch vụ lưu trữ ảnh hiện không khả dụng. Vui lòng thử lại sau.");
    }
}
