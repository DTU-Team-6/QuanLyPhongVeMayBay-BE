package com.team6.quanlyvemaybay.dto.auth;

public record AvatarUpdateResponse(
        PersonalInfoResponse profile,
        OldAvatarCleanupStatus oldAvatarCleanupStatus
) {
    public enum OldAvatarCleanupStatus {
        NOT_REQUIRED,
        DELETED,
        LEGACY_NOT_TRACKED,
        FAILED
    }
}
