package com.team6.quanlyvemaybay.validation;

import java.time.LocalDate;

/**
 * Giao diện chuẩn cho các DTO có cặp trường khoảng thời gian startDate - endDate.
 */
public interface DateRangeValidatable {

    LocalDate getStartDate();

    LocalDate getEndDate();
}
