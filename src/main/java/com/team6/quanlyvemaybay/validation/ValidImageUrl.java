package com.team6.quanlyvemaybay.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Chỉ nhận đường dẫn ảnh cục bộ từ gốc hoặc URL HTTP(S) hợp lệ. */
@Documented
@Constraint(validatedBy = ImageUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageUrl {
    String message() default "Ảnh phải là đường dẫn /path hoặc URL http(s) hợp lệ, không chứa tài khoản hay đường dẫn quay lui";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
