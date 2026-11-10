package com.team6.quanlyvemaybay.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Kiểm tra startDate <= endDate và gắn lỗi trực tiếp vào trường startDate khi không hợp lệ.
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeValidatable> {

    @Override
    public boolean isValid(DateRangeValidatable value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        LocalDate startDate = value.getStartDate();
        LocalDate endDate = value.getEndDate();
        if (startDate == null || endDate == null) {
            // Để @NotNull xử lý riêng trường bị thiếu
            return true;
        }
        if (!startDate.isAfter(endDate)) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("startDate")
                .addConstraintViolation();
        return false;
    }
}
