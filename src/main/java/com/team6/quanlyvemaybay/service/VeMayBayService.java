package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.ticket.TieuChiTimVe;
import com.team6.quanlyvemaybay.dto.ticket.VeDanhSachResponse;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VeMayBayService {
    private final VeMayBayRepository veMayBayRepository;

    public VeMayBayService(VeMayBayRepository veMayBayRepository) {
        this.veMayBayRepository = veMayBayRepository;
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
        return PageResponse.of(page.map(this::chuyenDoi));
    }

    private VeDanhSachResponse chuyenDoi(VeMayBay ve) {
        ChuyenBay chuyenBay = ve.getHangVe().getChuyenBay();
        return new VeDanhSachResponse(ve.getId(), ve.getMaVe(), ve.getHoTenNguoiDi(),
                ve.getDatCho().getMaDatCho(), chuyenBay.getMaChuyenBay(),
                chuyenBay.getSanBayDi().getMaSanBay(), chuyenBay.getSanBayDen().getMaSanBay(),
                chuyenBay.getThoiGianKhoiHanh().toLocalDate(), ve.getTongGia(), ve.getTrangThai());
    }
}
