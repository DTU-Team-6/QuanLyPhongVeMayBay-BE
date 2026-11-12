package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.payment.CreatePaymentUrlRequest;
import com.team6.quanlyvemaybay.dto.payment.PaymentUrlResponse;
import com.team6.quanlyvemaybay.dto.payment.TransactionHistoryDto;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.ThanhToan;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.LoaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.ThanhToanRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service điều phối tạo URL thanh toán và xem lịch sử giao dịch.
 */
@Service
public class ThanhToanService {

    private final DatChoRepository datChoRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final VeMayBayRepository veMayBayRepository;
    private final VnPayService vnPayService;
    private final PayPalService payPalService;

    public ThanhToanService(DatChoRepository datChoRepository,
                            ThanhToanRepository thanhToanRepository,
                            VeMayBayRepository veMayBayRepository,
                            VnPayService vnPayService,
                            PayPalService payPalService) {
        this.datChoRepository = datChoRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.veMayBayRepository = veMayBayRepository;
        this.vnPayService = vnPayService;
        this.payPalService = payPalService;
    }

    @Transactional
    public PaymentUrlResponse createPaymentUrl(CreatePaymentUrlRequest req, String clientIp) {
        DatCho datCho = datChoRepository.findById(req.datChoId())
                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy đơn đặt chỗ với ID: " + req.datChoId()));

        PhuongThucThanhToan phuongThuc = req.phuongThuc() != null ? req.phuongThuc() : PhuongThucThanhToan.VNPAY;

        return switch (phuongThuc) {
            case VNPAY -> vnPayService.createPaymentUrl(datCho, req.bankCode(), clientIp, req.returnUrl());
            case PAYPAL -> payPalService.createOrder(datCho, req.returnUrl(), null);
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PAYMENT_METHOD",
                    "Phương thức thanh toán '" + phuongThuc + "' chưa được hỗ trợ trực tuyến");
        };
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionHistoryDto> getHistory(LocalDate tuNgay, LocalDate denNgay,
                                                           TrangThaiDatCho trangThai, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        LocalDateTime start = tuNgay != null ? tuNgay.atStartOfDay() : null;
        LocalDateTime end = denNgay != null ? denNgay.plusDays(1).atStartOfDay() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<DatCho> bookingPage = datChoRepository.findHistory(start, end, trangThai, pageable);

        Page<TransactionHistoryDto> dtoPage = bookingPage.map(this::toHistoryDto);
        return PageResponse.of(dtoPage);
    }

    private TransactionHistoryDto toHistoryDto(DatCho datCho) {
        List<VeMayBay> veList = veMayBayRepository.findByDatChoId(datCho.getId());
        String tuyenBay = "Chuyến bay nội địa";
        LocalDate ngayBay = datCho.getNgayDat().toLocalDate();

        if (!veList.isEmpty()) {
            VeMayBay veDau = veList.get(0);
            if (veDau.getHangVe() != null && veDau.getHangVe().getChuyenBay() != null) {
                var cb = veDau.getHangVe().getChuyenBay();
                String tenDi = cb.getSanBayDi() != null ? cb.getSanBayDi().getThanhPho() : "Điểm đi";
                String tenDen = cb.getSanBayDen() != null ? cb.getSanBayDen().getThanhPho() : "Điểm đến";
                ngayBay = cb.getThoiGianKhoiHanh().toLocalDate();

                if (datCho.getLoaiChuyenBay() == LoaiChuyenBay.KHU_HOI) {
                    tuyenBay = tenDi + " ⇄ " + tenDen;
                } else {
                    tuyenBay = tenDi + " → " + tenDen;
                }
            }
        }

        List<ThanhToan> thanhToanList = thanhToanRepository.findByDatChoIdOrderByThoiGianThanhToanDesc(datCho.getId());
        String phuongThucStr = null;
        String maGiaoDichStr = null;

        if (!thanhToanList.isEmpty()) {
            ThanhToan tt = thanhToanList.get(0);
            phuongThucStr = tt.getPhuongThuc().name();
            maGiaoDichStr = tt.getMaGiaoDich();
        }

        return new TransactionHistoryDto(
                datCho.getId(),
                datCho.getMaDatCho(),
                tuyenBay,
                datCho.getNgayDat().toLocalDate(),
                ngayBay,
                datCho.getTongGiaBan(),
                datCho.getTrangThai().name(),
                phuongThucStr,
                maGiaoDichStr
        );
    }
}
