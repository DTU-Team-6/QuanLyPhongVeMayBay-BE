package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.booking.BookingDetailDto;
import com.team6.quanlyvemaybay.dto.booking.BookingRequest;
import com.team6.quanlyvemaybay.dto.booking.BookingResponse;
import com.team6.quanlyvemaybay.dto.booking.CancelBookingRequest;
import com.team6.quanlyvemaybay.dto.booking.CancelBookingResponse;
import com.team6.quanlyvemaybay.dto.booking.PendingPaymentScreenDto;
import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.service.DatChoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tiếp nhận yêu cầu đặt chỗ, xem danh sách vé chờ thanh toán, chi tiết đơn vé và hủy vé.
 */
@RestController
@RequestMapping("/api/v1/dat-cho")
@Tag(name = "Đặt chỗ & Vé", description = "Các API tạo đơn đặt vé, tra cứu vé chờ thanh toán, chi tiết đơn vé và hủy vé")
public class DatChoController {

    private final DatChoService service;

    public DatChoController(DatChoService service) {
        this.service = service;
    }

    @Operation(summary = "Tạo đặt chỗ mới và lưu thông tin hành khách")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = service.create(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Tạo đặt chỗ và lưu thông tin hành khách thành công"));
    }

    @Operation(summary = "Lấy dữ liệu cho màn hình 'Thanh toán vé' (danh sách vé chờ thanh toán & hạn giữ chỗ)")
    @GetMapping("/cho-thanh-toan")
    public ResponseEntity<ApiResponse<PendingPaymentScreenDto>> getPendingTickets(
            @RequestParam(required = false) String maDatCho) {
        PendingPaymentScreenDto response = service.getPendingPaymentScreen(maDatCho);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách vé chờ thanh toán thành công"));
    }

    @Operation(summary = "Xem chi tiết một đơn đặt vé theo mã PNR")
    @GetMapping("/{maDatCho}")
    public ResponseEntity<ApiResponse<BookingDetailDto>> getBookingDetail(@PathVariable String maDatCho) {
        BookingDetailDto response = service.getBookingDetail(maDatCho);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin chi tiết đơn đặt chỗ thành công"));
    }

    @Operation(summary = "Hủy đơn đặt chỗ hoặc hủy các vé được chọn (Nút [🗑 Hủy vé])")
    @PostMapping("/huy")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(
            @RequestBody CancelBookingRequest request) {
        CancelBookingResponse response = service.cancelBooking(request);
        return ResponseEntity.ok(ApiResponse.ok(response, response.thongBao()));
    }

    @Operation(summary = "Hủy đơn đặt chỗ nhanh theo mã PNR trong URL")
    @PostMapping("/{maDatCho}/huy")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBookingByCode(
            @PathVariable String maDatCho) {
        CancelBookingResponse response = service.cancelBooking(new CancelBookingRequest(maDatCho, null, "Khách hàng hủy trên giao diện"));
        return ResponseEntity.ok(ApiResponse.ok(response, response.thongBao()));
    }
}
