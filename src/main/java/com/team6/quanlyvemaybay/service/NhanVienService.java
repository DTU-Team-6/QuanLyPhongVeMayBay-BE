package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.nhanvien.XoaNhanVienResponse;
import com.team6.quanlyvemaybay.entity.NhanVien;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.NhanVienRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public NhanVienService(NhanVienRepository nhanVienRepository, NguoiDungRepository nguoiDungRepository) {
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Transactional
    public XoaNhanVienResponse xoaNhanVien(Long id) {
        NhanVien nhanVien = nhanVienRepository.findWithNguoiDungById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên."));
        if (nhanVien.getNguoiDung().getTrangThai() == null
                || nhanVien.getNguoiDung().getTrangThai() != 1) {
            throw daNgungHoatDong();
        }
        // UPDATE có điều kiện để hai yêu cầu đồng thời không cùng báo xóa thành công.
        if (nguoiDungRepository.ngungHoatDongNeuDangHoatDong(id) != 1) {
            throw daNgungHoatDong();
        }
        return new XoaNhanVienResponse(id, nhanVien.getMaNhanVien(), (byte) 0);
    }

    private BusinessException daNgungHoatDong() {
        return new BusinessException(HttpStatus.CONFLICT, "EMPLOYEE_ALREADY_INACTIVE",
                "Nhân viên đã ngừng hoạt động.");
    }
}
