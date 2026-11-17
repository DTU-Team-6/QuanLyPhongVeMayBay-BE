package com.team6.quanlyvemaybay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.payment.CreatePaymentUrlRequest;
import com.team6.quanlyvemaybay.dto.payment.PaymentCallbackResponse;
import com.team6.quanlyvemaybay.dto.payment.PaymentUrlResponse;
import com.team6.quanlyvemaybay.dto.payment.TransactionHistoryDto;
import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiThanhToan;
import com.team6.quanlyvemaybay.exception.GlobalExceptionHandler;
import com.team6.quanlyvemaybay.service.PayPalService;
import com.team6.quanlyvemaybay.service.ThanhToanService;
import com.team6.quanlyvemaybay.service.VnPayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ThanhToanController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ThanhToanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ThanhToanService thanhToanService;

    @MockitoBean
    private VnPayService vnPayService;

    @MockitoBean
    private PayPalService payPalService;

    @Test
    @DisplayName("Tạo URL thanh toán thành công trả về HTTP 200 và cấu trúc chuẩn")
    void createPaymentUrl_shouldReturnPaymentUrl() throws Exception {
        CreatePaymentUrlRequest request = new CreatePaymentUrlRequest(
                100L,
                PhuongThucThanhToan.VNPAY,
                "NCB",
                "http://localhost:5173/dat-ve/thanh-toan/ket-qua"
        );

        PaymentUrlResponse mockResponse = new PaymentUrlResponse(
                "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?token=123",
                "TXN123456",
                100L,
                "DC12345",
                new BigDecimal("1500000"),
                null,
                PhuongThucThanhToan.VNPAY
        );

        when(thanhToanService.createPaymentUrl(any(CreatePaymentUrlRequest.class), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/thanh-toan/tao-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.paymentUrl").value("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?token=123"))
                .andExpect(jsonPath("$.data.maGiaoDich").value("TXN123456"))
                .andExpect(jsonPath("$.data.datChoId").value(100))
                .andExpect(jsonPath("$.data.phuongThuc").value("VNPAY"));
    }

    @Test
    @DisplayName("Khởi tạo Order PayPal thành công trả về link phê duyệt PayPal")
    void createPayPalOrder_shouldReturnApprovalUrl() throws Exception {
        CreatePaymentUrlRequest request = new CreatePaymentUrlRequest(
                200L,
                PhuongThucThanhToan.PAYPAL,
                null,
                "http://localhost:5173/dat-ve/thanh-toan/ket-qua"
        );

        PaymentUrlResponse mockResponse = new PaymentUrlResponse(
                "https://www.sandbox.paypal.com/checkoutnow?token=8N0481314P236764N",
                "PP123456",
                200L,
                "DC20000",
                new BigDecimal("2540000"),
                new BigDecimal("100.00"),
                PhuongThucThanhToan.PAYPAL
        );

        when(thanhToanService.createPaymentUrl(any(CreatePaymentUrlRequest.class), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/thanh-toan/paypal/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.phuongThuc").value("PAYPAL"))
                .andExpect(jsonPath("$.data.soTienUsd").value(100.00))
                .andExpect(jsonPath("$.data.paymentUrl").value("https://www.sandbox.paypal.com/checkoutnow?token=8N0481314P236764N"));
    }

    @Test
    @DisplayName("Capture đơn PayPal thành công cập nhật trạng thái THANH_CONG")
    void capturePayPalOrder_shouldReturnSuccessCallback() throws Exception {
        PaymentCallbackResponse mockCallback = new PaymentCallbackResponse(
                "PP123456",
                "DC20000",
                new BigDecimal("2540000"),
                PhuongThucThanhToan.PAYPAL,
                TrangThaiThanhToan.THANH_CONG,
                "CAPTURE_8N0481314P236764N",
                "Thanh toán qua PayPal thành công",
                LocalDateTime.now()
        );

        when(payPalService.captureOrder("8N0481314P236764N")).thenReturn(mockCallback);

        mockMvc.perform(post("/api/v1/thanh-toan/paypal/capture")
                        .param("orderId", "8N0481314P236764N"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.trangThai").value("THANH_CONG"))
                .andExpect(jsonPath("$.data.phuongThuc").value("PAYPAL"))
                .andExpect(jsonPath("$.data.maGiaoDichDoiTac").value("CAPTURE_8N0481314P236764N"));
    }

    @Test
    @DisplayName("Lấy lịch sử giao dịch thành công trả về PageResponse")
    void getTransactionHistory_shouldReturnPageResponse() throws Exception {
        TransactionHistoryDto item = new TransactionHistoryDto(
                1L,
                "DC12345",
                "HAN - SGN",
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusDays(3),
                new BigDecimal("1500000"),
                "DA_THANH_TOAN",
                "VNPAY",
                "TXN123456"
        );

        PageResponse<TransactionHistoryDto> pageResponse = new PageResponse<>(
                List.of(item),
                0,
                10,
                1,
                1,
                true,
                true
        );

        when(thanhToanService.getHistory(any(), any(), any(), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/thanh-toan/lich-su")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].maDatCho").value("DC12345"))
                .andExpect(jsonPath("$.data.content[0].phuongThucThanhToan").value("VNPAY"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
