package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.chat.GuiTinNhanRequest;
import com.team6.quanlyvemaybay.dto.chat.HoiThoaiResponse;
import com.team6.quanlyvemaybay.dto.chat.TinNhanChatResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.TinNhanChat;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.TinNhanChatRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TinNhanChatService {
    private final TinNhanChatRepository tinNhanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final Clock clock;

    public TinNhanChatService(TinNhanChatRepository tinNhanRepository,
                             NguoiDungRepository nguoiDungRepository, Clock clock) {
        this.tinNhanRepository = tinNhanRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.clock = clock;
    }

    @Transactional
    public TinNhanChatResponse khachGui(Long nguoiDungId, GuiTinNhanRequest request) {
        NguoiDung khach = yeuCauVaiTro(nguoiDungId, VaiTro.KHACH_HANG);
        NguoiDung admin = nguoiDungRepository.findFirstByVaiTroAndTrangThaiOrderByIdAsc(VaiTro.ADMIN, (byte) 1)
                .orElseThrow(() -> new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                        "ADMIN_UNAVAILABLE", "Hiện không có Admin đang hoạt động để nhận tin nhắn."));
        return luuTinNhan(khach, admin, request);
    }

    @Transactional
    public TinNhanChatResponse adminTraLoi(Long adminId, Long khachHangId, GuiTinNhanRequest request) {
        NguoiDung admin = yeuCauVaiTro(adminId, VaiTro.ADMIN);
        NguoiDung khach = khachHang(khachHangId);
        return luuTinNhan(admin, khach, request);
    }

    @Transactional(readOnly = true)
    public PageResponse<TinNhanChatResponse> lichSuCuaKhach(Long nguoiDungId, int trang, int kichThuoc) {
        yeuCauVaiTro(nguoiDungId, VaiTro.KHACH_HANG);
        return lichSu(nguoiDungId, trang, kichThuoc);
    }

    @Transactional(readOnly = true)
    public PageResponse<TinNhanChatResponse> lichSuChoAdmin(Long adminId, Long khachHangId,
                                                            int trang, int kichThuoc) {
        yeuCauVaiTro(adminId, VaiTro.ADMIN);
        khachHang(khachHangId);
        return lichSu(khachHangId, trang, kichThuoc);
    }

    @Transactional(readOnly = true)
    public PageResponse<HoiThoaiResponse> danhSachHoiThoai(Long adminId, int trang, int kichThuoc) {
        yeuCauVaiTro(adminId, VaiTro.ADMIN);
        return PageResponse.of(tinNhanRepository.hoiThoai(VaiTro.KHACH_HANG, VaiTro.ADMIN,
                        PageRequest.of(trang, kichThuoc))
                .map(khach -> new HoiThoaiResponse(khach.getId(), khach.getHoTen(), khach.getTaiKhoanEmail())));
    }

    private PageResponse<TinNhanChatResponse> lichSu(Long khachHangId, int trang, int kichThuoc) {
        return PageResponse.of(tinNhanRepository.lichSu(khachHangId, VaiTro.ADMIN,
                PageRequest.of(trang, kichThuoc)).map(this::toResponse));
    }

    private TinNhanChatResponse luuTinNhan(NguoiDung nguoiGui, NguoiDung nguoiNhan,
                                           GuiTinNhanRequest request) {
        String noiDung = request.noiDung().strip();
        if (noiDung.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_MESSAGE",
                    "Nội dung tin nhắn không được để trống.");
        }
        TinNhanChat tinNhan = new TinNhanChat();
        tinNhan.setNguoiGui(nguoiGui);
        tinNhan.setNguoiNhan(nguoiNhan);
        tinNhan.setNoiDung(noiDung);
        tinNhan.setGuiLuc(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        return toResponse(tinNhanRepository.saveAndFlush(tinNhan));
    }

    private NguoiDung yeuCauVaiTro(Long id, VaiTro vaiTro) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED", "Tài khoản không còn tồn tại."));
        if (user.getTrangThai() == null || user.getTrangThai() != 1 || user.getVaiTro() != vaiTro) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "Tài khoản không có quyền sử dụng hội thoại này.");
        }
        return user;
    }

    private NguoiDung khachHang(Long id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Không tìm thấy khách hàng."));
        if (user.getVaiTro() != VaiTro.KHACH_HANG) {
            throw new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Không tìm thấy khách hàng.");
        }
        if (user.getTrangThai() == null || user.getTrangThai() != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "CUSTOMER_INACTIVE",
                    "Khách hàng không còn hoạt động.");
        }
        return user;
    }

    private TinNhanChatResponse toResponse(TinNhanChat tinNhan) {
        return new TinNhanChatResponse(tinNhan.getId(), tinNhan.getNguoiGui().getId(),
                tinNhan.getNguoiGui().getVaiTro(), tinNhan.getNguoiNhan().getId(),
                tinNhan.getNoiDung(), tinNhan.getGuiLuc().toInstant(ZoneOffset.UTC));
    }
}
