package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.booking.BookingRequest;
import com.team6.quanlyvemaybay.dto.booking.BookingResponse;
import com.team6.quanlyvemaybay.dto.booking.PassengerRequest;
import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.HanhKhach;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.ChieuBay;
import com.team6.quanlyvemaybay.entity.enums.LoaiHanhKhach;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiHang;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.HangVeChuyenBayRepository;
import com.team6.quanlyvemaybay.repository.HanhKhachRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý đặt chỗ và lưu thông tin hành khách.
 */
@Service
public class DatChoService {

    private final HangVeChuyenBayRepository hangVeRepo;
    private final DatChoRepository datChoRepo;
    private final HanhKhachRepository hanhKhachRepo;
    private final VeMayBayRepository veMayBayRepo;

    public DatChoService(HangVeChuyenBayRepository hangVeRepo,
                         DatChoRepository datChoRepo,
                         HanhKhachRepository hanhKhachRepo,
                         VeMayBayRepository veMayBayRepo) {
        this.hangVeRepo = hangVeRepo;
        this.datChoRepo = datChoRepo;
        this.hanhKhachRepo = hanhKhachRepo;
        this.veMayBayRepo = veMayBayRepo;
    }

    @Transactional
    public BookingResponse create(BookingRequest req) {
        HangVeChuyenBay hv = hangVeRepo.findById(req.hangVeId())
                .orElseThrow(() -> new ResourceNotFoundException("FARE_NOT_FOUND", "Không tìm thấy hạng vé"));

        if (hv.getChuyenBay().getTrangThai() == TrangThaiChuyenBay.DA_HUY) {
            throw new BusinessException(HttpStatus.CONFLICT, "CHUYEN_BAY_DA_HUY", "Chuyến bay đã bị hủy");
        }
        if (hv.getChuyenBay().getHangHangKhong() != null
                && hv.getChuyenBay().getHangHangKhong().getTrangThai() != TrangThaiHang.HOAT_DONG) {
            throw new BusinessException(HttpStatus.CONFLICT, "HANG_NGUNG_HOAT_DONG", "Hãng hàng không hiện đang tạm ngừng khai thác");
        }
        if (hv.getChuyenBay().getThoiGianKhoiHanh().isBefore(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.CONFLICT, "CHUYEN_BAY_DA_KHOI_HANH", "Chuyến bay đã khởi hành");
        }

        int effectiveSoEmBe = req.getEffectiveSoEmBe();
        if (effectiveSoEmBe > req.soNguoiLon()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PASSENGER_DATA",
                    "Số lượng em bé không được vượt quá số lượng người lớn đi cùng",
                    List.of(new ErrorDetail("soEmBe", "INFANT_EXCEEDS_ADULT",
                            "Mỗi người lớn chỉ được đi kèm tối đa 1 em bé"))
            );
        }
        int totalExpected = req.soNguoiLon() + req.soTreEm() + effectiveSoEmBe;
        if (req.hanhKhach().size() != totalExpected) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "PASSENGER_COUNT_MISMATCH",
                    "Số hành khách không khớp"
            );
        }

        long countNguoiLon = req.hanhKhach().stream().filter(p -> p.loaiHanhKhach() == LoaiHanhKhach.NGUOI_LON).count();
        long countTreEm = req.hanhKhach().stream().filter(p -> p.loaiHanhKhach() == LoaiHanhKhach.TRE_EM).count();
        long countEmBe = req.hanhKhach().stream().filter(p -> p.loaiHanhKhach() == LoaiHanhKhach.EM_BE).count();

        List<ErrorDetail> errors = new ArrayList<>();
        if (countNguoiLon != req.soNguoiLon()) {
            errors.add(new ErrorDetail("soNguoiLon", "COUNT_MISMATCH",
                    "Số lượng người lớn trong danh sách (" + countNguoiLon + ") không khớp với khai báo (" + req.soNguoiLon() + ")"));
        }
        if (countTreEm != req.soTreEm()) {
            errors.add(new ErrorDetail("soTreEm", "COUNT_MISMATCH",
                    "Số lượng trẻ em trong danh sách (" + countTreEm + ") không khớp với khai báo (" + req.soTreEm() + ")"));
        }
        if (countEmBe != effectiveSoEmBe) {
            errors.add(new ErrorDetail("soEmBe", "COUNT_MISMATCH",
                    "Số lượng em bé trong danh sách (" + countEmBe + ") không khớp với khai báo (" + effectiveSoEmBe + ")"));
        }

        LocalDate flightDate = hv.getChuyenBay().getThoiGianKhoiHanh().toLocalDate();
        for (int i = 0; i < req.hanhKhach().size(); i++) {
            PassengerRequest p = req.hanhKhach().get(i);
            LocalDate birthDate = p.ngaySinh();
            if (birthDate.isAfter(flightDate)) {
                errors.add(new ErrorDetail("hanhKhach[" + i + "].ngaySinh", "INVALID_BIRTHDATE",
                        "Ngày sinh của hành khách '" + p.hoTen() + "' không được sau ngày bay (" + flightDate + ")"));
            } else {
                int age = Period.between(birthDate, flightDate).getYears();
                switch (p.loaiHanhKhach()) {
                    case NGUOI_LON -> {
                        if (age < 12) {
                            errors.add(new ErrorDetail("hanhKhach[" + i + "].ngaySinh", "INVALID_AGE",
                                    "Hành khách người lớn '" + p.hoTen() + "' phải từ 12 tuổi trở lên tính đến ngày bay (hiện tại: " + age + " tuổi)"));
                        }
                        if (p.cmndPassport() == null || p.cmndPassport().isBlank()) {
                            errors.add(new ErrorDetail("hanhKhach[" + i + "].cmndPassport", "MISSING_CMND_PASSPORT",
                                    "Hành khách người lớn '" + p.hoTen() + "' bắt buộc phải có CMND/Passport"));
                        }
                    }
                    case TRE_EM -> {
                        if (age < 2 || age >= 12) {
                            errors.add(new ErrorDetail("hanhKhach[" + i + "].ngaySinh", "INVALID_AGE",
                                    "Hành khách trẻ em '" + p.hoTen() + "' phải từ 2 đến dưới 12 tuổi tính đến ngày bay (hiện tại: " + age + " tuổi)"));
                        }
                    }
                    case EM_BE -> {
                        if (age >= 2) {
                            errors.add(new ErrorDetail("hanhKhach[" + i + "].ngaySinh", "INVALID_AGE",
                                    "Hành khách em bé '" + p.hoTen() + "' phải dưới 2 tuổi tính đến ngày bay (hiện tại: " + age + " tuổi)"));
                        }
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PASSENGER_DATA", "Thông tin danh sách hành khách không hợp lệ", errors);
        }

        int seatsToReserve = (int) (countNguoiLon + countTreEm);
        if (hangVeRepo.reserveSeats(hv.getId(), seatsToReserve) == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "HET_CHO", "Chuyến bay không đủ chỗ");
        }

        BigDecimal fareWithFee = hv.getGiaBan().add(hv.getThuePhi());
        BigDecimal tongGiaBan = fareWithFee.multiply(BigDecimal.valueOf(seatsToReserve));
        BigDecimal tongGiaGoc = hv.getGiaGoc() != null
                ? hv.getGiaGoc().multiply(BigDecimal.valueOf(seatsToReserve))
                : BigDecimal.ZERO;

        DatCho dc = new DatCho();
        dc.setMaDatCho(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                dc.setKhachHangId(Long.valueOf(jwt.getSubject()));
            }
        } catch (Exception ignored) {
        }
        dc.setEmailNguoiDat(req.emailNguoiDat());
        dc.setSoDienThoaiNguoiDat(req.soDienThoaiNguoiDat());
        dc.setHoTenNguoiDat(req.hoTenNguoiDat());
        dc.setSoNguoiLon(req.soNguoiLon());
        dc.setSoTreEm(req.soTreEm());
        dc.setSoEmBe(effectiveSoEmBe);
        dc.setNgayDat(LocalDateTime.now());
        dc.setTongGiaGoc(tongGiaGoc);
        dc.setTongGiaBan(tongGiaBan);
        dc.setTrangThai(TrangThaiDatCho.CHO_THANH_TOAN);
        dc = datChoRepo.save(dc);

        for (PassengerRequest p : req.hanhKhach()) {
            HanhKhach hk = new HanhKhach();
            hk.setDatChoId(dc.getId());
            hk.setHoTen(p.hoTen());
            hk.setNgaySinh(p.ngaySinh());
            hk.setGioiTinh(p.gioiTinh());
            hk.setEmail(p.email());
            hk.setSoDienThoai(p.soDienThoai());
            hk.setCmndPassport(p.cmndPassport());
            hk.setLoaiHanhKhach(p.loaiHanhKhach());
            hk = hanhKhachRepo.save(hk);

            boolean isInfant = p.loaiHanhKhach() == LoaiHanhKhach.EM_BE;
            BigDecimal veGiaGoc = isInfant ? BigDecimal.ZERO : (hv.getGiaGoc() != null ? hv.getGiaGoc() : BigDecimal.ZERO);
            BigDecimal veGiaBan = isInfant ? BigDecimal.ZERO : hv.getGiaBan();
            BigDecimal veThuePhi = isInfant ? BigDecimal.ZERO : hv.getThuePhi();

            VeMayBay ve = new VeMayBay();
            ve.setMaVe("VE" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            ve.setDatCho(dc);
            ve.setHanhKhachId(hk.getId());
            ve.setHoTenNguoiDi(hk.getHoTen());
            ve.setEmailThanhToan(dc.getEmailNguoiDat());
            ve.setHangVe(hv);
            ve.setChieuBay(ChieuBay.CHIEU_DI);
            ve.setGiaGoc(veGiaGoc);
            ve.setGiaBan(veGiaBan);
            ve.setThuePhi(veThuePhi);
            ve.setCuocHanhLy(BigDecimal.ZERO);
            ve.setTongGia(veGiaBan.add(veThuePhi));
            ve.setDaInVe(false);
            ve.setTrangThai(TrangThaiVe.CHO_THANH_TOAN);
            veMayBayRepo.save(ve);
        }

        return new BookingResponse(
                dc.getId(),
                dc.getMaDatCho(),
                dc.getTongGiaBan(),
                dc.getTrangThai().name(),
                req.hanhKhach().size()
        );
    }

    @Transactional
    public BookingResponse cancel(Long datChoId) {
        DatCho dc = datChoRepo.findById(datChoId)
                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy mã đặt chỗ"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String role = jwt.getClaimAsString("role");
            if ("KHACH_HANG".equalsIgnoreCase(role)) {
                Long currentUserId = Long.valueOf(jwt.getSubject());
                String currentEmail = jwt.getClaimAsString("email");
                boolean isOwner = (dc.getKhachHangId() != null && dc.getKhachHangId().equals(currentUserId))
                        || (currentEmail != null && dc.getEmailNguoiDat() != null
                        && dc.getEmailNguoiDat().equalsIgnoreCase(currentEmail));
                if (!isOwner) {
                    throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Bạn không có quyền hủy đặt chỗ này");
                }
            }
        }

        if (dc.getTrangThai() == TrangThaiDatCho.DA_HUY) {
            return new BookingResponse(dc.getId(), dc.getMaDatCho(), dc.getTongGiaBan(), dc.getTrangThai().name(),
                    dc.getSoNguoiLon() + dc.getSoTreEm() + dc.getSoEmBe());
        }
        if (dc.getTrangThai() != TrangThaiDatCho.CHO_THANH_TOAN) {
            throw new BusinessException(HttpStatus.CONFLICT, "BOOKING_NOT_CANCELLABLE",
                    "Chỉ có thể hủy đơn đặt chỗ đang ở trạng thái chờ thanh toán");
        }

        dc.setTrangThai(TrangThaiDatCho.DA_HUY);
        datChoRepo.save(dc);

        List<VeMayBay> tickets = veMayBayRepo.findByDatChoId(dc.getId());
        Long hangVeIdToRelease = null;
        for (VeMayBay ve : tickets) {
            ve.setTrangThai(TrangThaiVe.DA_HUY);
            if (hangVeIdToRelease == null && ve.getHangVe() != null) {
                hangVeIdToRelease = ve.getHangVe().getId();
            }
        }
        if (!tickets.isEmpty()) {
            veMayBayRepo.saveAll(tickets);
        }
        int seatsToRelease = dc.getSoNguoiLon() + dc.getSoTreEm();
        if (hangVeIdToRelease != null && seatsToRelease > 0) {
            hangVeRepo.releaseSeats(hangVeIdToRelease, seatsToRelease);
        }

        return new BookingResponse(
                dc.getId(),
                dc.getMaDatCho(),
                dc.getTongGiaBan(),
                dc.getTrangThai().name(),
                dc.getSoNguoiLon() + dc.getSoTreEm() + dc.getSoEmBe()
        );
    }
}
