package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.TimeConfig;
import com.team6.quanlyvemaybay.dto.flight.AirportSummaryDto;
import com.team6.quanlyvemaybay.dto.flight.ChuyenBayCuaToiItem;
import com.team6.quanlyvemaybay.dto.flight.ChuyenBayCuaToiResponse;
import com.team6.quanlyvemaybay.dto.flight.FlightTimeDto;
import com.team6.quanlyvemaybay.dto.flight.TraCuuChuyenBayCuaToiRequest;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DatChoRepository;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ChuyenBayCuaToiService {
    private static final Map<String, ZoneId> MUI_GIO_QUOC_TE = Map.ofEntries(
            Map.entry("BKK", ZoneId.of("Asia/Bangkok")),
            Map.entry("DMK", ZoneId.of("Asia/Bangkok")),
            Map.entry("SIN", ZoneId.of("Asia/Singapore")),
            Map.entry("KUL", ZoneId.of("Asia/Kuala_Lumpur")),
            Map.entry("ICN", ZoneId.of("Asia/Seoul")),
            Map.entry("GMP", ZoneId.of("Asia/Seoul")),
            Map.entry("NRT", ZoneId.of("Asia/Tokyo")),
            Map.entry("HND", ZoneId.of("Asia/Tokyo")),
            Map.entry("TPE", ZoneId.of("Asia/Taipei")),
            Map.entry("HKG", ZoneId.of("Asia/Hong_Kong")),
            Map.entry("SYD", ZoneId.of("Australia/Sydney")),
            Map.entry("MEL", ZoneId.of("Australia/Melbourne"))
    );

    private final DatChoRepository datChoRepository;
    private final VeMayBayRepository veMayBayRepository;
    private final GioiHanTraCuuService gioiHan;

    public ChuyenBayCuaToiService(DatChoRepository datChoRepository,
                                  VeMayBayRepository veMayBayRepository,
                                  GioiHanTraCuuService gioiHan) {
        this.datChoRepository = datChoRepository;
        this.veMayBayRepository = veMayBayRepository;
        this.gioiHan = gioiHan;
    }

    @Transactional(readOnly = true)
    public ChuyenBayCuaToiResponse traCuu(TraCuuChuyenBayCuaToiRequest request, String ip) {
        gioiHan.kiemTra(ip);
        String maDatCho = request.maDatCho().strip().toUpperCase(Locale.ROOT);
        DatCho datCho = datChoRepository.findByMaDatCho(maDatCho).orElse(null);
        if (datCho == null
                || !datCho.getEmailNguoiDat().equalsIgnoreCase(request.email().strip())
                || !chuanHoaSoDienThoai(datCho.getSoDienThoaiNguoiDat())
                        .equals(chuanHoaSoDienThoai(request.soDienThoai()))) {
            throw new ResourceNotFoundException("BOOKING_LOOKUP_FAILED",
                    "Không tìm thấy đặt chỗ với thông tin đã cung cấp.");
        }

        LinkedHashMap<Long, ChuyenBayCuaToiItem> chuyenBay = new LinkedHashMap<>();
        veMayBayRepository.findByDatChoId(datCho.getId()).stream()
                .sorted(Comparator.comparing(VeMayBay::getId))
                .map(this::toItem)
                .forEach(item -> chuyenBay.putIfAbsent(item.chuyenBayId(), item));
        return new ChuyenBayCuaToiResponse(List.copyOf(chuyenBay.values()));
    }

    private String chuanHoaSoDienThoai(String soDienThoai) {
        return soDienThoai.replace(" ", "").strip();
    }

    private ChuyenBayCuaToiItem toItem(VeMayBay ve) {
        ChuyenBay cb = ve.getHangVe().getChuyenBay();
        return new ChuyenBayCuaToiItem(
                cb.getId(), cb.getMaChuyenBay(),
                cb.getHangHangKhong().getTenHang(),
                toAirport(cb.getSanBayDi()), toAirport(cb.getSanBayDen()),
                toFlightTime(cb.getThoiGianKhoiHanh().atZone(muiGio(cb.getSanBayDi()))),
                toFlightTime(cb.getThoiGianHaCanh().atZone(muiGio(cb.getSanBayDen()))),
                cb.getTrangThai()
        );
    }

    private AirportSummaryDto toAirport(SanBay sanBay) {
        return new AirportSummaryDto(sanBay.getMaSanBay(), sanBay.getTenSanBay(),
                sanBay.getThanhPho(), sanBay.getQuocGia());
    }

    private FlightTimeDto toFlightTime(ZonedDateTime time) {
        return new FlightTimeDto(time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), time.getZone().getId());
    }

    private ZoneId muiGio(SanBay sanBay) {
        return MUI_GIO_QUOC_TE.getOrDefault(sanBay.getMaSanBay().toUpperCase(Locale.ROOT),
                TimeConfig.DEFAULT_VN_ZONE);
    }
}
