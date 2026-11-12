package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.ticket.TieuChiTimVe;
import com.team6.quanlyvemaybay.dto.ticket.VeDanhSachResponse;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.HanhKhach;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.HanhKhachRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VeMayBayService {
    private final VeMayBayRepository veMayBayRepository;
    private final HanhKhachRepository hanhKhachRepository;

    public VeMayBayService(VeMayBayRepository veMayBayRepository, HanhKhachRepository hanhKhachRepository) {
        this.veMayBayRepository = veMayBayRepository;
        this.hanhKhachRepository = hanhKhachRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<VeDanhSachResponse> danhSachVe(TrangThaiVe trangThai, TieuChiTimVe tieuChi,
                                                        String tuKhoa, int trang, int kichThuoc) {
        String keyword = tuKhoa == null ? "" : tuKhoa.strip();
        if ((tieuChi == null) != keyword.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_SEARCH",
                    "Cần cung cấp đồng thời tiêu chí và từ khóa tìm kiếm.",
                    List.of(new ErrorDetail("tuKhoa", "INVALID_PARAMETER", "Thiếu tiêu chí hoặc từ khóa")));
        }
        Page<VeMayBay> page = veMayBayRepository.timDanhSachVe(trangThai,
                tieuChi == null ? "" : tieuChi.name(), keyword, PageRequest.of(trang, kichThuoc));
        Map<Long, String> tenNguoiDi = page.isEmpty() ? Map.of() : hanhKhachRepository.findAllById(
                page.getContent().stream().map(VeMayBay::getHanhKhachId).distinct().toList())
                .stream().collect(Collectors.toMap(HanhKhach::getId, HanhKhach::getHoTen));
        return PageResponse.of(page.map(v -> chuyenDoi(v, tenNguoiDi.get(v.getHanhKhachId()))));
    }

    private VeDanhSachResponse chuyenDoi(VeMayBay ve, String tenNguoiDi) {
        ChuyenBay chuyenBay = ve.getHangVe().getChuyenBay();
        return new VeDanhSachResponse(ve.getId(), ve.getMaVe(), tenNguoiDi,
                ve.getDatCho().getMaDatCho(), chuyenBay.getMaChuyenBay(),
                chuyenBay.getSanBayDi().getMaSanBay(), chuyenBay.getSanBayDen().getMaSanBay(),
                chuyenBay.getThoiGianKhoiHanh().toLocalDate(), ve.getTongGia(), ve.getTrangThai());
    }
}
