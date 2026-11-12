package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.payment.CreatePaymentUrlRequest;
import com.team6.quanlyvemaybay.dto.payment.PaymentCallbackResponse;
import com.team6.quanlyvemaybay.dto.payment.PaymentUrlResponse;
import com.team6.quanlyvemaybay.dto.payment.TransactionHistoryDto;
import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.service.PayPalService;
import com.team6.quanlyvemaybay.service.ThanhToanService;
import com.team6.quanlyvemaybay.service.VnPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller xử lý tạo URL thanh toán và tiếp nhận callback từ cổng thanh toán (VNPay / PayPal).
 */
@RestController
@RequestMapping("/api/v1/thanh-toan")
@Tag(name = "Thanh toán", description = "Các API tạo URL thanh toán VNPay/PayPal, callback và lịch sử giao dịch")
public class ThanhToanController {

    private final ThanhToanService thanhToanService;
    private final VnPayService vnPayService;
    private final PayPalService payPalService;

    public ThanhToanController(ThanhToanService thanhToanService,
                               VnPayService vnPayService,
                               PayPalService payPalService) {
        this.thanhToanService = thanhToanService;
        this.vnPayService = vnPayService;
        this.payPalService = payPalService;
    }

    @Operation(summary = "Tạo URL thanh toán theo phương thức lựa chọn (VNPay hoặc PayPal)")
    @PostMapping("/tao-url")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createPaymentUrl(
            @Valid @RequestBody CreatePaymentUrlRequest request,
            HttpServletRequest servletRequest) {
        String clientIp = extractClientIp(servletRequest);
        PaymentUrlResponse response = thanhToanService.createPaymentUrl(request, clientIp);
        return ResponseEntity.ok(ApiResponse.ok(response, "Khởi tạo URL thanh toán thành công"));
    }

    @Operation(summary = "Tạo URL thanh toán VNPay Sandbox")
    @PostMapping("/vnpay/create-url")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createVnPayUrl(
            @Valid @RequestBody CreatePaymentUrlRequest request,
            HttpServletRequest servletRequest) {
        CreatePaymentUrlRequest vnpayReq = new CreatePaymentUrlRequest(
                request.datChoId(),
                PhuongThucThanhToan.VNPAY,
                request.bankCode(),
                request.returnUrl()
        );
        String clientIp = extractClientIp(servletRequest);
        PaymentUrlResponse response = thanhToanService.createPaymentUrl(vnpayReq, clientIp);
        return ResponseEntity.ok(ApiResponse.ok(response, "Khởi tạo URL thanh toán VNPay thành công"));
    }

    @Operation(summary = "Tiếp nhận callback kết quả thanh toán từ VNPay")
    @GetMapping("/vnpay/callback")
    public ResponseEntity<ApiResponse<PaymentCallbackResponse>> handleVnPayCallback(
            @RequestParam Map<String, String> queryParams) {
        PaymentCallbackResponse response = vnPayService.handleCallback(queryParams);
        return ResponseEntity.ok(ApiResponse.ok(response, response.thongBao()));
    }

    @Operation(summary = "Tạo Order thanh toán PayPal Sandbox")
    @PostMapping("/paypal/create-order")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createPayPalOrder(
            @Valid @RequestBody CreatePaymentUrlRequest request,
            HttpServletRequest servletRequest) {
        CreatePaymentUrlRequest ppReq = new CreatePaymentUrlRequest(
                request.datChoId(),
                PhuongThucThanhToan.PAYPAL,
                null,
                request.returnUrl()
        );
        String clientIp = extractClientIp(servletRequest);
        PaymentUrlResponse response = thanhToanService.createPaymentUrl(ppReq, clientIp);
        return ResponseEntity.ok(ApiResponse.ok(response, "Khởi tạo đơn thanh toán PayPal thành công"));
    }

    @Operation(summary = "Xác nhận và capture đơn thanh toán PayPal phía Backend")
    @PostMapping("/paypal/capture")
    public ResponseEntity<ApiResponse<PaymentCallbackResponse>> capturePayPalOrder(
            @RequestParam String orderId) {
        PaymentCallbackResponse response = payPalService.captureOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok(response, response.thongBao()));
    }

    @Operation(summary = "Xem lịch sử giao dịch vé máy bay (hỗ trợ lọc ngày, trạng thái, phân trang)")
    @GetMapping("/lich-su")
    public ResponseEntity<ApiResponse<PageResponse<TransactionHistoryDto>>> getTransactionHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) TrangThaiDatCho trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TransactionHistoryDto> history = thanhToanService.getHistory(tuNgay, denNgay, trangThai, page, size);
        return ResponseEntity.ok(ApiResponse.ok(history, "Lấy lịch sử giao dịch thành công"));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
