package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.destination.CreateDestinationRequest;
import com.team6.quanlyvemaybay.dto.destination.DestinationResponse;
import com.team6.quanlyvemaybay.entity.DiemDen;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DiemDenRepository;
import com.team6.quanlyvemaybay.repository.SanBayRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Service xử lý nghiệp vụ truy vấn danh sách và thêm mới điểm đến du lịch.
 */
@Service
public class DiemDenService {

    private final DiemDenRepository diemDenRepository;
    private final SanBayRepository sanBayRepository;

    public DiemDenService(DiemDenRepository diemDenRepository, SanBayRepository sanBayRepository) {
        this.diemDenRepository = diemDenRepository;
        this.sanBayRepository = sanBayRepository;
    }

    /**
     * Lấy danh sách điểm đến có hỗ trợ tìm kiếm theo từ khóa, lọc theo mã sân bay và phân trang.
     */
    @Transactional(readOnly = true)
    public PageResponse<DestinationResponse> getDestinations(String keyword, String maSanBay, int page, int size) {
        String normalizedKeyword = normalizeOptionalText(keyword);
        String normalizedAirport = normalizeOptionalCode(maSanBay);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        Page<DestinationResponse> dtoPage = diemDenRepository
                .findWithFilters(normalizedKeyword, normalizedAirport, pageable)
                .map(this::toResponse);

        return PageResponse.of(dtoPage);
    }

    /**
     * Thêm mới điểm đến sau khi kiểm tra sân bay tồn tại và không trùng tên điểm đến tại cùng sân bay.
     */
    @Transactional
    public DestinationResponse createDestination(CreateDestinationRequest request) {
        String normalizedName = request.tenDiemDen().trim().replaceAll("\\s+", " ");
        String normalizedAirport = request.maSanBay().trim().toUpperCase(Locale.ROOT);

        SanBay sanBay = sanBayRepository.findById(normalizedAirport)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AIRPORT_NOT_FOUND",
                        "Không tìm thấy sân bay với mã: " + normalizedAirport
                ));

        if (diemDenRepository.existsDuplicateInAirport(normalizedName, sanBay.getMaSanBay())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "DESTINATION_ALREADY_EXISTS",
                    "Điểm đến '" + normalizedName + "' đã tồn tại tại sân bay " + sanBay.getMaSanBay(),
                    List.of(new ErrorDetail(
                            "tenDiemDen",
                            "DUPLICATE_DESTINATION",
                            "Điểm đến '" + normalizedName + "' đã tồn tại tại sân bay " + sanBay.getMaSanBay()
                    ))
            );
        }

        DiemDen diemDen = new DiemDen();
        diemDen.setTenDiemDen(normalizedName);
        diemDen.setSanBay(sanBay);
        diemDen.setMoTa(normalizeOptionalText(request.moTa()));
        diemDen.setHinhAnh(normalizeOptionalText(request.hinhAnh()));

        DiemDen saved = diemDenRepository.save(diemDen);
        return toResponse(saved);
    }

    private DestinationResponse toResponse(DiemDen diemDen) {
        SanBay sanBay = diemDen.getSanBay();
        return new DestinationResponse(
                diemDen.getId(),
                diemDen.getTenDiemDen(),
                sanBay.getMaSanBay(),
                sanBay.getTenSanBay(),
                sanBay.getThanhPho(),
                sanBay.getQuocGia(),
                diemDen.getMoTa(),
                diemDen.getHinhAnh()
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeOptionalCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
