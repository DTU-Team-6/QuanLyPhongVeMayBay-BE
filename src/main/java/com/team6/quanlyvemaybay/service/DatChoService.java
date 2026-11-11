package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.booking.BookingRequest;
import com.team6.quanlyvemaybay.dto.booking.BookingResponse;
import com.team6.quanlyvemaybay.dto.booking.PassengerRequest;
import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.HanhKhach;
import com.team6.quanlyvemaybay.entity.enums.LoaiHanhKhach;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.HangVeChuyenBayRepository;
import com.team6.quanlyvemaybay.repository.HanhKhachRepository;
import org.springframework.http.HttpStatus;
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

    public DatChoService(HangVeChuyenBayRepository hangVeRepo,
                         DatChoRepository datChoRepo,
                         HanhKhachRepository hanhKhachRepo) {
        this.hangVeRepo = hangVeRepo;
        this.datChoRepo = datChoRepo;
        this.hanhKhachRepo = hanhKhachRepo;
    }

    @Transactional
    public BookingResponse create(BookingRequest req) {
        HangVeChuyenBay hv = hangVeRepo.findById(req.hangVeId())
                .orElseThrow(() -> new ResourceNotFoundException("FARE_NOT_FOUND", "Không tìm thấy hạng vé"));

        if (hv.getChuyenBay().getThoiGianKhoiHanh().isBefore(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.CONFLICT, "CHUYEN_BAY_DA_KHOI_HANH", "Chuyến bay đã khởi hành");
        }

        int effectiveSoEmBe = req.getEffectiveSoEmBe();
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

        DatCho dc = new DatCho();
        dc.setMaDatCho(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        dc.setEmailNguoiDat(req.emailNguoiDat());
        dc.setSoDienThoaiNguoiDat(req.soDienThoaiNguoiDat());
        dc.setHoTenNguoiDat(req.hoTenNguoiDat());
        dc.setSoNguoiLon(req.soNguoiLon());
        dc.setSoTreEm(req.soTreEm());
        dc.setSoEmBe(effectiveSoEmBe);
        dc.setNgayDat(LocalDateTime.now());
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
            hanhKhachRepo.save(hk);
        }

        return new BookingResponse(
                dc.getId(),
                dc.getMaDatCho(),
                dc.getTongGiaBan(),
                dc.getTrangThai().name(),
                req.hanhKhach().size()
        );
    }
}
