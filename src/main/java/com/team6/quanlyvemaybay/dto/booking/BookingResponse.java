package com.team6.quanlyvemaybay.dto.booking;
import java.math.BigDecimal;
public record BookingResponse(Long id, String maDatCho, BigDecimal tongGiaBan, String trangThai, int soHanhKhach) {}
