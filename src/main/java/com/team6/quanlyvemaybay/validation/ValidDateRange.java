package com.team6.quanlyvemaybay.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ràng buộc kiểm tra tính hợp lệ của khoảng thời gian (startDate <= endDate).
 */
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
