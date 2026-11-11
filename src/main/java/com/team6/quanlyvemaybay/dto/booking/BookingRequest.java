package com.team6.quanlyvemaybay.dto.booking;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.List;
public record BookingRequest(@NotNull Long hangVeId, @NotBlank @Email String emailNguoiDat,
 @NotBlank String soDienThoaiNguoiDat, @NotBlank String hoTenNguoiDat,
 @Min(1) int soNguoiLon, @Min(0) int soTreEm, @NotEmpty @Size(max=9) List<@Valid PassengerRequest> hanhKhach) {}
