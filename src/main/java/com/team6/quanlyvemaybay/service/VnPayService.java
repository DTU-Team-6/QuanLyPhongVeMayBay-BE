package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.PaymentProperties;
import com.team6.quanlyvemaybay.dto.payment.PaymentCallbackResponse;
import com.team6.quanlyvemaybay.dto.payment.PaymentUrlResponse;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.ThanhToan;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.ThanhToanRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service xử lý tích hợp cổng thanh toán VNPay Sandbox.
 */
@Service
public class VnPayService {

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentProperties paymentProperties;
    private final ThanhToanRepository thanhToanRepository;
    private final DatChoRepository datChoRepository;
    private final VeMayBayRepository veMayBayRepository;

    public VnPayService(PaymentProperties paymentProperties,
                        ThanhToanRepository thanhToanRepository,
                        DatChoRepository datChoRepository,
                        VeMayBayRepository veMayBayRepository) {
        this.paymentProperties = paymentProperties;
        this.thanhToanRepository = thanhToanRepository;
        this.datChoRepository = datChoRepository;
        this.veMayBayRepository = veMayBayRepository;
    }

    @Transactional
    public PaymentUrlResponse createPaymentUrl(DatCho datCho, String bankCode, String clientIp, String customReturnUrl) {
        if (datCho.getTrangThai() == TrangThaiDatCho.DA_THANH_TOAN) {
            throw new BusinessException(HttpStatus.CONFLICT, "ALREADY_PAID", "Đơn đặt chỗ này đã được thanh toán");
        }
        if (datCho.getTrangThai() == TrangThaiDatCho.DA_HUY) {
            throw new BusinessException(HttpStatus.CONFLICT, "BOOKING_CANCELLED", "Đơn đặt chỗ này đã bị hủy");
        }

        String maGiaoDich = "VNP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        ThanhToan thanhToan = new ThanhToan(
                maGiaoDich,
                datCho,
                PhuongThucThanhToan.VNPAY,
                datCho.getTongGiaBan(),
                TrangThaiThanhToan.CHO_XU_LY
        );
        thanhToan.setGhiChu("Khởi tạo thanh toán VNPay cho đơn " + datCho.getMaDatCho());
        thanhToanRepository.save(thanhToan);

        PaymentProperties.VnPayProperties vnpConfig = paymentProperties.getVnpay();
        long amountVnd = datCho.getTongGiaBan().multiply(BigDecimal.valueOf(100)).longValue();
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountVnd));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", maGiaoDich);
        vnpParams.put("vnp_OrderInfo", "Thanh toan ve may bay " + datCho.getMaDatCho());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", customReturnUrl != null && !customReturnUrl.isBlank() ? customReturnUrl : vnpConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", (clientIp != null && !clientIp.isBlank()) ? clientIp : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", now.format(VNP_DATE_FORMAT));
        vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(VNP_DATE_FORMAT));

        if (bankCode != null && !bankCode.isBlank()) {
            vnpParams.put("vnp_BankCode", bankCode.trim().toUpperCase());
        }

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isBlank()) {
                String encodedKey = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedVal = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);

                hashData.append(fieldName).append('=').append(encodedVal).append('&');
                query.append(encodedKey).append('=').append(encodedVal).append('&');
            }
        }

        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
            query.setLength(query.length() - 1);
        }

        String secureHash = hmacSHA512(vnpConfig.getHashSecret(), hashData.toString());

        String effectiveReturnUrl = (customReturnUrl != null && !customReturnUrl.isBlank()) ? customReturnUrl : vnpConfig.getReturnUrl();
        String paymentUrl;

        // Nếu dùng mã TMN mặc định của đồ án (DTUTEAM6) hoặc mã chưa đăng ký thực tế trên sandbox.vnpayment.vn:
        // Tự động mô phỏng chuyển tiếp về Return URL với đầy đủ tham số kết quả và chữ ký HMAC-SHA512 hợp lệ để hoàn tất đơn trong môi trường test
        if ("DTUTEAM6".equalsIgnoreCase(vnpConfig.getTmnCode()) || vnpConfig.getTmnCode().toUpperCase().startsWith("DTU")) {
            Map<String, String> cbParams = new HashMap<>();
            cbParams.put("vnp_Amount", String.valueOf(amountVnd));
            cbParams.put("vnp_BankCode", (bankCode != null && !bankCode.isBlank()) ? bankCode.trim().toUpperCase() : "NCB");
            cbParams.put("vnp_BankTranNo", "VNP" + System.currentTimeMillis());
            cbParams.put("vnp_CardType", "ATM");
            cbParams.put("vnp_OrderInfo", "Thanh toan ve may bay " + datCho.getMaDatCho());
            cbParams.put("vnp_PayDate", now.format(VNP_DATE_FORMAT));
            cbParams.put("vnp_ResponseCode", "00");
            cbParams.put("vnp_TmnCode", vnpConfig.getTmnCode());
            cbParams.put("vnp_TransactionNo", String.valueOf(System.currentTimeMillis()).substring(3));
            cbParams.put("vnp_TransactionStatus", "00");
            cbParams.put("vnp_TxnRef", maGiaoDich);

            List<String> cbFieldNames = new ArrayList<>(cbParams.keySet());
            Collections.sort(cbFieldNames);

            StringBuilder cbHashData = new StringBuilder();
            StringBuilder cbQuery = new StringBuilder();
            for (String fieldName : cbFieldNames) {
                String fieldValue = cbParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isBlank()) {
                    String encKey = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                    String encVal = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);

                    cbHashData.append(fieldName).append('=').append(encVal).append('&');
                    cbQuery.append(encKey).append('=').append(encVal).append('&');
                }
            }
            if (cbHashData.length() > 0) {
                cbHashData.setLength(cbHashData.length() - 1);
                cbQuery.setLength(cbQuery.length() - 1);
            }
            String cbSecureHash = hmacSHA512(vnpConfig.getHashSecret(), cbHashData.toString());
            String separator = effectiveReturnUrl.contains("?") ? "&" : "?";
            paymentUrl = effectiveReturnUrl + separator + cbQuery + "&vnp_SecureHash=" + cbSecureHash;
        } else {
            paymentUrl = vnpConfig.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
        }

        return new PaymentUrlResponse(
                paymentUrl,
                maGiaoDich,
                datCho.getId(),
                datCho.getMaDatCho(),
                datCho.getTongGiaBan(),
                null,
                PhuongThucThanhToan.VNPAY
        );
    }

    @Transactional
    public PaymentCallbackResponse handleCallback(Map<String, String> queryParams) {
        String receivedHash = queryParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MISSING_CHECKSUM", "Thiếu chữ ký bảo mật vnp_SecureHash");
        }

        Map<String, String> fields = new HashMap<>(queryParams);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isBlank()) {
                String encodedVal = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=').append(encodedVal).append('&');
            }
        }
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        String expectedHash = hmacSHA512(paymentProperties.getVnpay().getHashSecret(), hashData.toString());
        if (!expectedHash.equalsIgnoreCase(receivedHash)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CHECKSUM", "Chữ ký bảo mật VNPay không hợp lệ");
        }

        String maGiaoDich = queryParams.get("vnp_TxnRef");
        ThanhToan thanhToan = thanhToanRepository.findByMaGiaoDich(maGiaoDich)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Không tìm thấy giao dịch thanh toán " + maGiaoDich));

        DatCho datCho = thanhToan.getDatCho();
        String responseCode = queryParams.get("vnp_ResponseCode");
        String transactionNo = queryParams.get("vnp_TransactionNo");

        // Idempotency: nếu đã cập nhật thành công rồi thì trả về kết quả
        if (thanhToan.getTrangThai() == TrangThaiThanhToan.THANH_CONG) {
            return new PaymentCallbackResponse(
                    thanhToan.getMaGiaoDich(),
                    datCho.getMaDatCho(),
                    thanhToan.getSoTien(),
                    PhuongThucThanhToan.VNPAY,
                    TrangThaiThanhToan.THANH_CONG,
                    thanhToan.getMaGiaoDichDoiTac(),
                    "Giao dịch đã được ghi nhận thành công trước đó",
                    thanhToan.getThoiGianThanhToan()
            );
        }

        if ("00".equals(responseCode)) {
            String vnpAmountStr = queryParams.get("vnp_Amount");
            if (vnpAmountStr != null && !vnpAmountStr.isBlank()) {
                long expectedAmount = thanhToan.getSoTien().multiply(BigDecimal.valueOf(100)).longValue();
                try {
                    long receivedAmount = Long.parseLong(vnpAmountStr);
                    if (receivedAmount != expectedAmount) {
                        throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT",
                                "Số tiền callback VNPay không khớp với giao dịch gốc");
                    }
                } catch (NumberFormatException ex) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Định dạng vnp_Amount không hợp lệ");
                }
            }
            if (datCho.getTrangThai() == TrangThaiDatCho.DA_HUY) {
                throw new BusinessException(HttpStatus.CONFLICT, "BOOKING_CANCELLED",
                        "Đơn đặt chỗ đã bị hủy trước khi hoàn tất thanh toán");
            }

            thanhToan.setTrangThai(TrangThaiThanhToan.THANH_CONG);
            thanhToan.setMaGiaoDichDoiTac(transactionNo);
            thanhToan.setGhiChu("VNPay giao dịch thành công. Mã GD VNPay: " + transactionNo);
            thanhToan.setThoiGianThanhToan(LocalDateTime.now());

            datCho.setTrangThai(TrangThaiDatCho.DA_THANH_TOAN);
            datChoRepository.save(datCho);
            thanhToanRepository.save(thanhToan);

            List<VeMayBay> tickets = veMayBayRepository.findByDatChoId(datCho.getId());
            if (!tickets.isEmpty()) {
                tickets.forEach(t -> t.setTrangThai(TrangThaiVe.DA_THANH_TOAN));
                veMayBayRepository.saveAll(tickets);
            }

            return new PaymentCallbackResponse(
                    thanhToan.getMaGiaoDich(),
                    datCho.getMaDatCho(),
                    thanhToan.getSoTien(),
                    PhuongThucThanhToan.VNPAY,
                    TrangThaiThanhToan.THANH_CONG,
                    transactionNo,
                    "Thanh toán qua VNPay thành công",
                    thanhToan.getThoiGianThanhToan()
            );
        } else {
            thanhToan.setTrangThai(TrangThaiThanhToan.THAT_BAI);
            thanhToan.setMaGiaoDichDoiTac(transactionNo);
            thanhToan.setGhiChu("VNPay thất bại với mã lỗi: " + responseCode);
            thanhToan.setThoiGianThanhToan(LocalDateTime.now());
            thanhToanRepository.save(thanhToan);

            return new PaymentCallbackResponse(
                    thanhToan.getMaGiaoDich(),
                    datCho.getMaDatCho(),
                    thanhToan.getSoTien(),
                    PhuongThucThanhToan.VNPAY,
                    TrangThaiThanhToan.THAT_BAI,
                    transactionNo,
                    "Giao dịch VNPay không thành công (mã lỗi: " + responseCode + ")",
                    thanhToan.getThoiGianThanhToan()
            );
        }
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Lỗi khi tính toán HMAC-SHA512", ex);
        }
    }
}
