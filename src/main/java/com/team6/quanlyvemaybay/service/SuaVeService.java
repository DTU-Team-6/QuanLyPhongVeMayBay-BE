package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeRequest;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeResponse;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuaVeService {
    private final VeMayBayRepository repository;

    public SuaVeService(VeMayBayRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SuaVeResponse suaVe(Long id, SuaVeRequest request) {
        VeMayBay ve = repository.findForTicketUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("TICKET_NOT_FOUND", "Không tìm thấy vé."));
        if (ve.getTrangThai() != TrangThaiVe.CHO_THANH_TOAN
                || ve.getDatCho().getTrangThai() != TrangThaiDatCho.CHO_THANH_TOAN) {
            throw new BusinessException(HttpStatus.CONFLICT, "TICKET_NOT_EDITABLE",
                    "Chỉ được sửa vé thuộc đặt chỗ chưa thanh toán; vé đã thanh toán hoặc đã hủy không được sửa.");
        }

        String hoTen = request.hoTenNguoiDi().strip();
        if (hoTen.length() < 10 || hoTen.length() > 50) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_TICKET_INFO",
                    "Tên người đi phải có từ 10 đến 50 ký tự.",
                    List.of(new ErrorDetail("hoTenNguoiDi", "INVALID_FIELD", "Tên người đi phải có từ 10 đến 50 ký tự.")));
        }
        ve.setHoTenNguoiDi(hoTen);
        ve.setEmailThanhToan(request.emailThanhToan().strip());
        repository.saveAndFlush(ve);
        return new SuaVeResponse(ve.getId(), ve.getMaVe(), ve.getHoTenNguoiDi(),
                ve.getEmailThanhToan(), ve.getTrangThai());
    }
}
