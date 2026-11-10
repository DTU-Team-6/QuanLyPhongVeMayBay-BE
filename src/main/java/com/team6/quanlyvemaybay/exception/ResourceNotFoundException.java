package com.team6.quanlyvemaybay.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên yêu cầu (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String code;

    public ResourceNotFoundException(String message) {
        super(message);
        this.code = "RESOURCE_NOT_FOUND";
    }

    public ResourceNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
