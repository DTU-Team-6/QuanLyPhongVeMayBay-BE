package com.team6.quanlyvemaybay.dto.report;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.validation.DateRangeValidatable;
import com.team6.quanlyvemaybay.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO nhận điều kiện lọc báo cáo doanh thu chi tiết (dành cho Nhân viên/Admin).
 */
@ValidDateRange
public class ReportFilterRequest implements DateRangeValidatable {

    @Schema(description = "Từ ngày (định dạng yyyy-MM-dd)", example = "2026-11-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ngày bắt đầu (startDate) không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "Đến ngày (định dạng yyyy-MM-dd)", example = "2026-11-30", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ngày kết thúc (endDate) không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "Loại báo cáo", example = "THEO_HANG", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Loại báo cáo (reportType) không được để trống")
    private LoaiBaoCao reportType;

    @Schema(description = "Tiêu chí gom nhóm khi báo cáo theo thời gian (mặc định DAY)", example = "DAY")
    private TieuChiThongKe groupBy = TieuChiThongKe.DAY;

    @Schema(description = "Lọc theo mã hãng hàng không (tuỳ chọn)", example = "VN")
    private String maHang;

    @Schema(description = "Lọc theo ID nhân viên (tuỳ chọn)", example = "2")
    private Long nhanVienId;

    @Schema(description = "Lọc theo trạng thái đơn đặt chỗ (mặc định DA_THANH_TOAN)", example = "DA_THANH_TOAN")
    private TrangThaiDatCho trangThaiDatCho = TrangThaiDatCho.DA_THANH_TOAN;

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

    public LoaiBaoCao getReportType() {
        return reportType;
    }

    public void setReportType(LoaiBaoCao reportType) {
        this.reportType = reportType;
    }

    public TieuChiThongKe getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(TieuChiThongKe groupBy) {
        this.groupBy = groupBy;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public Long getNhanVienId() {
        return nhanVienId;
    }

    public void setNhanVienId(Long nhanVienId) {
        this.nhanVienId = nhanVienId;
    }

    public TrangThaiDatCho getTrangThaiDatCho() {
        return trangThaiDatCho;
    }

    public void setTrangThaiDatCho(TrangThaiDatCho trangThaiDatCho) {
        this.trangThaiDatCho = trangThaiDatCho;
    }
}
