package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.booking.BookingDetailDto;
import com.team6.quanlyvemaybay.dto.booking.BookingRequest;
import com.team6.quanlyvemaybay.dto.booking.BookingResponse;
import com.team6.quanlyvemaybay.dto.booking.CancelBookingRequest;
import com.team6.quanlyvemaybay.dto.booking.CancelBookingResponse;
import com.team6.quanlyvemaybay.dto.booking.PassengerItemDto;
import com.team6.quanlyvemaybay.dto.booking.PassengerRequest;
import com.team6.quanlyvemaybay.dto.booking.PendingPaymentScreenDto;
import com.team6.quanlyvemaybay.dto.booking.PendingTicketDto;
import com.team6.quanlyvemaybay.dto.booking.TicketItemDto;
import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.HanhKhach;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.ChieuBay;
import com.team6.quanlyvemaybay.entity.enums.LoaiHanhKhach;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.HangVeChuyenBayRepository;
import com.team6.quanlyvemaybay.repository.HanhKhachRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý đặt chỗ, quản lý đơn vé đã đặt, hủy vé và hiển thị màn hình thanh toán.
 */
@Service
public class DatChoService {

    private static final DateTimeFormatter FORMATTER_THONG_BAO = DateTimeFormatter.ofPattern("HH:mm, 'ngày' dd/MM/yyyy");

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

    /**
     * Tạo đặt chỗ, lưu danh sách hành khách và khởi tạo các vé máy bay chi tiết.
     */
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
            hk = hanhKhachRepo.save(hk);

            // Khởi tạo vé máy bay chi tiết cho từng hành khách
            VeMayBay ve = new VeMayBay();
            ve.setMaVe("TK" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            ve.setDatCho(dc);
            ve.setHanhKhachId(hk.getId());
            ve.setHangVe(hv);
            ve.setChieuBay(ChieuBay.CHIEU_DI);
            ve.setGiaGoc(hv.getGiaGoc());
            if (p.loaiHanhKhach() == LoaiHanhKhach.EM_BE) {
                ve.setGiaBan(BigDecimal.ZERO);
                ve.setThuePhi(BigDecimal.ZERO);
                ve.setCuocHanhLy(BigDecimal.ZERO);
                ve.setTongGia(BigDecimal.ZERO);
            } else {
                ve.setGiaBan(hv.getGiaBan());
                ve.setThuePhi(hv.getThuePhi());
                ve.setCuocHanhLy(BigDecimal.ZERO);
                ve.setTongGia(hv.getGiaBan().add(hv.getThuePhi()));
            }
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

    /**
     * Lấy dữ liệu cho màn hình "Thanh toán vé" (khớp 100% Ảnh 1).
     */
    @Transactional(readOnly = true)
    public PendingPaymentScreenDto getPendingPaymentScreen(String maDatCho) {
        List<VeMayBay> tickets = veMayBayRepo.findTicketsWithFlightDetails(maDatCho, TrangThaiVe.CHO_THANH_TOAN);

        if (tickets.isEmpty()) {
            if (maDatCho != null && !maDatCho.isBlank()) {
                DatCho dc = datChoRepo.findByMaDatCho(maDatCho.trim().toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy mã đặt chỗ: " + maDatCho));
                return new PendingPaymentScreenDto(
                        dc.getMaDatCho(),
                        calculateExpiry(dc.getNgayDat()),
                        "Mã đặt chỗ " + dc.getMaDatCho() + " hiện không có vé nào ở trạng thái chờ thanh toán (Trạng thái đơn: " + dc.getTrangThai() + ")",
                        BigDecimal.ZERO,
                        0,
                        List.of()
                );
            }
            return new PendingPaymentScreenDto("", null, "Không có đơn vé nào đang chờ thanh toán", BigDecimal.ZERO, 0, List.of());
        }

        // Tải thông tin hành khách để map tên hiển thị
        List<Long> datChoIds = tickets.stream().map(v -> v.getDatCho().getId()).distinct().toList();
        List<HanhKhach> allPassengers = new ArrayList<>();
        for (Long dId : datChoIds) {
            allPassengers.addAll(hanhKhachRepo.findByDatChoId(dId));
        }
        Map<Long, String> passengerNameMap = allPassengers.stream()
                .collect(Collectors.toMap(HanhKhach::getId, HanhKhach::getHoTen, (a, b) -> a));

        List<PendingTicketDto> dtoList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        DatCho firstBooking = tickets.get(0).getDatCho();
        LocalDateTime expiry = calculateExpiry(firstBooking.getNgayDat());

        for (VeMayBay v : tickets) {
            ChuyenBay cb = v.getHangVe().getChuyenBay();
            String passengerName = passengerNameMap.getOrDefault(v.getHanhKhachId(), "Hành khách");
            String baggageInfo = "Hành lý xách tay " + v.getHangVe().getHanhLyXachTayKg() + "kg";
            if (v.getGoiHanhLyId() != null) {
                baggageInfo += " + ký gửi";
            }

            dtoList.add(new PendingTicketDto(
                    v.getId(),
                    v.getMaVe(),
                    v.getDatCho().getId(),
                    v.getDatCho().getMaDatCho(),
                    cb.getSanBayDi().getThanhPho(),
                    cb.getSanBayDi().getMaSanBay(),
                    cb.getSanBayDen().getThanhPho(),
                    cb.getSanBayDen().getMaSanBay(),
                    v.getDatCho().getNgayDat(),
                    cb.getThoiGianKhoiHanh(),
                    v.getTongGia(),
                    "Chờ thanh toán",
                    expiry,
                    baggageInfo,
                    passengerName,
                    v.getSoGhe()
            ));

            totalAmount = totalAmount.add(v.getTongGia());
        }

        String thongBao = "Mã đặt chỗ " + firstBooking.getMaDatCho() + " đang chờ thanh toán. Hạn giữ chỗ: "
                + expiry.format(FORMATTER_THONG_BAO);

        return new PendingPaymentScreenDto(
                firstBooking.getMaDatCho(),
                expiry,
                thongBao,
                totalAmount,
                dtoList.size(),
                dtoList
        );
    }

    /**
     * Xem chi tiết đầy đủ một đơn đặt chỗ theo mã PNR.
     */
    @Transactional(readOnly = true)
    public BookingDetailDto getBookingDetail(String maDatCho) {
        DatCho dc = datChoRepo.findByMaDatCho(maDatCho.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy mã đặt chỗ: " + maDatCho));

        List<HanhKhach> passengers = hanhKhachRepo.findByDatChoId(dc.getId());
        List<VeMayBay> tickets = veMayBayRepo.findTicketsWithFlightDetails(dc.getMaDatCho(), null);

        Map<Long, String> passengerMap = passengers.stream()
                .collect(Collectors.toMap(HanhKhach::getId, HanhKhach::getHoTen, (a, b) -> a));

        List<TicketItemDto> ticketDtos = tickets.stream().map(v -> {
            ChuyenBay cb = v.getHangVe().getChuyenBay();
            return new TicketItemDto(
                    v.getId(),
                    v.getMaVe(),
                    cb.getMaChuyenBay(),
                    cb.getHangHangKhong().getTenHang(),
                    cb.getSanBayDi().getThanhPho(),
                    cb.getSanBayDi().getMaSanBay(),
                    cb.getSanBayDen().getThanhPho(),
                    cb.getSanBayDen().getMaSanBay(),
                    cb.getThoiGianKhoiHanh(),
                    cb.getThoiGianHaCanh(),
                    v.getChieuBay().name(),
                    v.getHangVe().getLoaiKhoang().name(),
                    v.getSoGhe(),
                    passengerMap.getOrDefault(v.getHanhKhachId(), ""),
                    v.getGiaBan(),
                    v.getThuePhi(),
                    v.getCuocHanhLy(),
                    v.getTongGia(),
                    v.getTrangThai().name()
            );
        }).toList();

        List<PassengerItemDto> passengerDtos = passengers.stream().map(p -> new PassengerItemDto(
                p.getId(),
                p.getHoTen(),
                p.getNgaySinh(),
                p.getGioiTinh().name(),
                p.getLoaiHanhKhach().name(),
                p.getCmndPassport(),
                p.getSoDienThoai(),
                p.getEmail()
        )).toList();

        LocalDateTime expiry = calculateExpiry(dc.getNgayDat());

        return new BookingDetailDto(
                dc.getId(),
                dc.getMaDatCho(),
                dc.getHoTenNguoiDat(),
                dc.getEmailNguoiDat(),
                dc.getSoDienThoaiNguoiDat(),
                dc.getLoaiChuyenBay().name(),
                dc.getSoNguoiLon(),
                dc.getSoTreEm(),
                dc.getSoEmBe(),
                dc.getNgayDat(),
                expiry,
                dc.getTongGiaBan(),
                dc.getTrangThai().name(),
                ticketDtos,
                passengerDtos
        );
    }

    /**
     * Hủy đơn đặt chỗ hoặc hủy danh sách vé được chọn (Ứng với nút [🗑 Hủy vé] ở Ảnh 1).
     * Tự động hoàn lại số ghế trống cho chuyến bay.
     */
    @Transactional
    public CancelBookingResponse cancelBooking(CancelBookingRequest req) {
        if (req.danhSachMaVe() != null && !req.danhSachMaVe().isEmpty()) {
            return cancelByTicketCodes(req.danhSachMaVe());
        }

        if (req.maDatCho() == null || req.maDatCho().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MISSING_CANCEL_TARGET", "Cần cung cấp mã đặt chỗ hoặc danh sách mã vé cần hủy");
        }

        String maDatCho = req.maDatCho().trim().toUpperCase();
        DatCho dc = datChoRepo.findByMaDatCho(maDatCho)
                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy mã đặt chỗ: " + maDatCho));

        if (dc.getTrangThai() == TrangThaiDatCho.DA_HUY) {
            throw new BusinessException(HttpStatus.CONFLICT, "ALREADY_CANCELLED", "Đơn đặt chỗ " + maDatCho + " đã bị hủy trước đó");
        }
        if (dc.getTrangThai() == TrangThaiDatCho.DA_THANH_TOAN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CANNOT_CANCEL_PAID", "Đơn đặt chỗ đã thanh toán thành công, vui lòng liên hệ quầy vé để làm thủ tục hoàn vé");
        }

        List<VeMayBay> tickets = veMayBayRepo.findByDatChoId(dc.getId());
        int releasedSeats = 0;
        for (VeMayBay v : tickets) {
            if (v.getTrangThai() == TrangThaiVe.CHO_THANH_TOAN) {
                v.setTrangThai(TrangThaiVe.DA_HUY);
                veMayBayRepo.save(v);
                hangVeRepo.releaseSeats(v.getHangVe().getId(), 1);
                releasedSeats++;
            }
        }

        dc.setTrangThai(TrangThaiDatCho.DA_HUY);
        datChoRepo.save(dc);

        return new CancelBookingResponse(
                true,
                maDatCho,
                releasedSeats,
                "Hủy thành công đơn đặt chỗ " + maDatCho + " và đã giải phóng " + releasedSeats + " ghế ngồi"
        );
    }

    private CancelBookingResponse cancelByTicketCodes(List<String> ticketCodes) {
        List<VeMayBay> tickets = veMayBayRepo.findByMaVeIn(ticketCodes);
        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("TICKETS_NOT_FOUND", "Không tìm thấy mã vé nào trong danh sách yêu cầu hủy");
        }

        int count = 0;
        String bookingCode = null;
        for (VeMayBay v : tickets) {
            if (v.getTrangThai() == TrangThaiVe.CHO_THANH_TOAN) {
                v.setTrangThai(TrangThaiVe.DA_HUY);
                veMayBayRepo.save(v);
                hangVeRepo.releaseSeats(v.getHangVe().getId(), 1);
                count++;
                if (bookingCode == null) {
                    bookingCode = v.getDatCho().getMaDatCho();
                }
            }
        }

        // Kiểm tra xem đơn đặt chỗ còn vé nào hợp lệ không
        if (bookingCode != null) {
            DatCho dc = datChoRepo.findByMaDatCho(bookingCode).orElse(null);
            if (dc != null) {
                List<VeMayBay> remaining = veMayBayRepo.findByDatChoId(dc.getId());
                boolean allCancelled = remaining.stream().allMatch(t -> t.getTrangThai() == TrangThaiVe.DA_HUY);
                if (allCancelled) {
                    dc.setTrangThai(TrangThaiDatCho.DA_HUY);
                    datChoRepo.save(dc);
                }
            }
        }

        return new CancelBookingResponse(
                true,
                bookingCode != null ? bookingCode : "",
                count,
                "Đã hủy thành công " + count + " vé và giải phóng ghế ngồi"
        );
    }

    /**
     * Hạn giữ chỗ = 24 giờ sau khi đặt.
     */
    private LocalDateTime calculateExpiry(LocalDateTime bookingTime) {
        if (bookingTime == null) {
            return LocalDateTime.now().plusHours(24);
        }
        return bookingTime.plusHours(24);
    }
}
