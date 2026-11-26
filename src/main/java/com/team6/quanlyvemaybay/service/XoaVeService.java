package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.ticket.XoaVeResponse;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class XoaVeService {
    private final VeMayBayRepository repository;

    public XoaVeService(VeMayBayRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public XoaVeResponse xoaVe(Long id) {
        VeMayBay ve = repository.findForCancellation(id)
                .orElseThrow(() -> new ResourceNotFoundException("TICKET_NOT_FOUND", "Không tìm thấy vé."));
        if (ve.getTrangThai() == TrangThaiVe.DA_HUY) {
            throw new BusinessException(HttpStatus.CONFLICT, "TICKET_ALREADY_CANCELLED", "Vé đã bị hủy.");
        }
        if (ve.getTrangThai() != TrangThaiVe.CHO_THANH_TOAN
                || ve.getDatCho().getTrangThai() != TrangThaiDatCho.CHO_THANH_TOAN) {
            throw new BusinessException(HttpStatus.CONFLICT, "TICKET_NOT_CANCELLABLE",
                    "Chỉ có thể hủy vé thuộc đặt chỗ chưa thanh toán.");
        }
        ve.setTrangThai(TrangThaiVe.DA_HUY);
        repository.saveAndFlush(ve);
        return new XoaVeResponse(ve.getId(), ve.getMaVe(), TrangThaiVe.DA_HUY);
    }
}
