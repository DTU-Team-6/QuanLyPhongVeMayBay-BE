package com.team6.quanlyvemaybay.dto.report;

import com.team6.quanlyvemaybay.validation.DateRangeValidatable;
import com.team6.quanlyvemaybay.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO nhận điều kiện lọc thống kê tổng hợp theo kỳ (doanh thu, số vé bán ra, tỷ lệ lấp đầy).
 */
@ValidDateRange
public class StatisticsFilterRequest implements DateRangeValidatable {

    @Schema(description = "Từ ngày (định dạng yyyy-MM-dd)", example = "2026-11-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ngày bắt đầu (startDate) không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "Đến ngày (định dạng yyyy-MM-dd)", example = "2026-11-30", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ngày kết thúc (endDate) không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "Kỳ thống kê: DAY, MONTH, YEAR (mặc định DAY)", example = "DAY")
    private TieuChiThongKe period = TieuChiThongKe.DAY;

    @Schema(description = "Lọc theo mã hãng hàng không (tuỳ chọn)", example = "VN")
    private String maHang;

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public TieuChiThongKe getPeriod() {
        return period;
    }

    public void setPeriod(TieuChiThongKe period) {
        this.period = period;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }
}
