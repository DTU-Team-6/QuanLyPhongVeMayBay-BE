package com.team6.quanlyvemaybay.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record BookingRequest(
        @NotNull(message = "Mã hạng vé không được để trống")
        Long hangVeId,

        @NotBlank(message = "Email người đặt không được để trống")
        @Email(message = "Email người đặt không đúng định dạng")
        String emailNguoiDat,

        @NotBlank(message = "Số điện thoại người đặt không được để trống")
        String soDienThoaiNguoiDat,

        @NotBlank(message = "Họ tên người đặt không được để trống")
        String hoTenNguoiDat,

        @Min(value = 1, message = "Số người lớn tối thiểu là 1")
        int soNguoiLon,

        @Min(value = 0, message = "Số trẻ em không được âm")
        int soTreEm,

        @Min(value = 0, message = "Số em bé không được âm")
        Integer soEmBe,

        @NotEmpty(message = "Danh sách hành khách không được để trống")
        @Size(max = 9, message = "Tối đa 9 hành khách trong một lần đặt")
        List<@Valid PassengerRequest> hanhKhach
) {
    public int getEffectiveSoEmBe() {
        return soEmBe != null ? soEmBe : 0;
    }
}
