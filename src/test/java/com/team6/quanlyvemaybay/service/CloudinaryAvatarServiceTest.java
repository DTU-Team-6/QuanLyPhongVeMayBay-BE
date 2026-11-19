package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.AvatarUpdateResponse;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.service.CloudinaryAvatarClient.CloudinaryAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryAvatarServiceTest {

    @Mock
    private AuthService authService;
    @Mock
    private NguoiDungRepository nguoiDungRepository;
    @Mock
    private CloudinaryAvatarClient cloudinaryAvatarClient;

    private CloudinaryAvatarService service;

    @BeforeEach
    void setUp() {
        service = new CloudinaryAvatarService(authService, nguoiDungRepository, cloudinaryAvatarClient);
        when(authService.currentUser(42L)).thenReturn(new PersonalInfoResponse(
                42L, "user@example.com", "Nguyễn Văn A", null, null, GioiTinh.Khac,
                null, null, VaiTro.KHACH_HANG, null, "Việt Nam"
        ));
    }

    @Test
    void updateAvatarSavesVerifiedImageAndDeletesPreviousManagedImage() {
        String oldPublicId = "avatar_42_00000000000000000000000000000001";
        String newPublicId = "avatar_42_00000000000000000000000000000002";
        NguoiDung user = activeUser();
        user.setAnhDaiDien("https://res.cloudinary.com/demo/image/upload/" + oldPublicId);
        user.setAnhDaiDienPublicId(oldPublicId);
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(cloudinaryAvatarClient.getImage(newPublicId)).thenReturn(validImage(newPublicId));
        when(cloudinaryAvatarClient.getSecureUrlPrefix()).thenReturn("https://res.cloudinary.com/demo/image/upload/");
        when(nguoiDungRepository.saveAndFlush(user)).thenReturn(user);

        var result = service.updateAvatar(42L, newPublicId);

        assertEquals(newPublicId, user.getAnhDaiDienPublicId());
        assertEquals(validImage(newPublicId).secureUrl(), user.getAnhDaiDien());
        assertEquals(AvatarUpdateResponse.OldAvatarCleanupStatus.DELETED, result.oldAvatarCleanupStatus());
        verify(cloudinaryAvatarClient).deleteImage(oldPublicId);
    }

    @Test
    void updateAvatarRejectsPublicIdFromAnotherUser() {
        String anotherUsersPublicId = "avatar_7_00000000000000000000000000000002";

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateAvatar(42L, anotherUsersPublicId)
        );

        assertEquals("INVALID_AVATAR_REFERENCE", exception.getCode());
        verify(cloudinaryAvatarClient, never()).getImage(anotherUsersPublicId);
        verify(nguoiDungRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateAvatarRejectsUnsupportedCloudinaryFormat() {
        String publicId = "avatar_42_00000000000000000000000000000003";
        NguoiDung user = activeUser();
        when(cloudinaryAvatarClient.getImage(publicId)).thenReturn(new CloudinaryAsset(
                publicId, "image", "upload", "svg", 500, "https://res.cloudinary.com/demo/image/upload/" + publicId
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateAvatar(42L, publicId)
        );

        assertEquals("INVALID_AVATAR_ASSET", exception.getCode());
        verify(nguoiDungRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(cloudinaryAvatarClient).deleteImage(publicId);
    }

    @Test
    void updateAvatarReportsLegacyImageWithoutDeletingUnknownCloudinaryId() {
        String publicId = "avatar_42_00000000000000000000000000000004";
        NguoiDung user = activeUser();
        user.setAnhDaiDien("https://legacy.example.com/avatar.jpg");
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(cloudinaryAvatarClient.getImage(publicId)).thenReturn(validImage(publicId));
        when(cloudinaryAvatarClient.getSecureUrlPrefix()).thenReturn("https://res.cloudinary.com/demo/image/upload/");
        when(nguoiDungRepository.saveAndFlush(user)).thenReturn(user);

        var result = service.updateAvatar(42L, publicId);

        assertEquals(AvatarUpdateResponse.OldAvatarCleanupStatus.LEGACY_NOT_TRACKED,
                result.oldAvatarCleanupStatus());
        verify(cloudinaryAvatarClient, never()).deleteImage("legacy.example.com/avatar.jpg");
    }

    @Test
    void updateAvatarSurfacesCloudinaryCleanupFailure() {
        String oldPublicId = "avatar_42_00000000000000000000000000000005";
        String newPublicId = "avatar_42_00000000000000000000000000000006";
        NguoiDung user = activeUser();
        user.setAnhDaiDien("https://res.cloudinary.com/demo/image/upload/" + oldPublicId);
        user.setAnhDaiDienPublicId(oldPublicId);
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(cloudinaryAvatarClient.getImage(newPublicId)).thenReturn(validImage(newPublicId));
        when(cloudinaryAvatarClient.getSecureUrlPrefix()).thenReturn("https://res.cloudinary.com/demo/image/upload/");
        when(nguoiDungRepository.saveAndFlush(user)).thenReturn(user);
        org.mockito.Mockito.doThrow(new CloudinaryAvatarClient.CloudinaryApiException(503))
                .when(cloudinaryAvatarClient).deleteImage(oldPublicId);

        var result = service.updateAvatar(42L, newPublicId);

        assertEquals(AvatarUpdateResponse.OldAvatarCleanupStatus.FAILED, result.oldAvatarCleanupStatus());
        assertEquals(newPublicId, user.getAnhDaiDienPublicId());
    }

    private NguoiDung activeUser() {
        NguoiDung user = new NguoiDung();
        user.setId(42L);
        user.setTaiKhoanEmail("user@example.com");
        user.setHoTen("Nguyễn Văn A");
        user.setVaiTro(VaiTro.KHACH_HANG);
        user.setTrangThai((byte) 1);
        return user;
    }

    private CloudinaryAsset validImage(String publicId) {
        return new CloudinaryAsset(
                publicId, "image", "upload", "jpg", 1024,
                "https://res.cloudinary.com/demo/image/upload/v123456789/" + publicId + ".jpg"
        );
    }
}
