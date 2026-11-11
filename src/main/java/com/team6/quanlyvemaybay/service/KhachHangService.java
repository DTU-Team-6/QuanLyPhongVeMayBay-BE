package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;

    public KhachHangService(KhachHangRepository khachHangRepository) {
        this.khachHangRepository = khachHangRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<KhachHangResponse> layDanhSach(String keyword, int page, int size) {
        String normalizedKeyword = normalizeOptionalText(keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<KhachHangResponse> result = khachHangRepository.findWithFilters(normalizedKeyword, pageable)
                .map(this::toResponse);
        return PageResponse.of(result);
    }

    @Transactional(readOnly = true)
    public KhachHangResponse layChiTiet(Long id) {
        KhachHang khachHang = khachHangRepository.findWithNguoiDungByNguoiDungId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CUSTOMER_NOT_FOUND",
                        "Không tìm thấy khách hàng."
                ));
        return toResponse(khachHang);
    }

    private KhachHangResponse toResponse(KhachHang khachHang) {
        var nguoiDung = khachHang.getNguoiDung();
        return new KhachHangResponse(
                nguoiDung.getId(),
                nguoiDung.getTaiKhoanEmail(),
                nguoiDung.getHoTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getNgaySinh(),
                nguoiDung.getGioiTinh(),
                nguoiDung.getDiaChi(),
                khachHang.getCmndPassport(),
                khachHang.getQuocTich(),
                khachHang.getXepHang(),
                khachHang.getDiemTichLuy(),
                nguoiDung.getTrangThai()
        );
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
