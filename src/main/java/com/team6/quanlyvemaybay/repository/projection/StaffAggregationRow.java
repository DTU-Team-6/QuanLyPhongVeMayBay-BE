package com.team6.quanlyvemaybay.repository.projection;

import java.math.BigDecimal;

/**
 * Projection tổng hợp doanh thu đơn đặt chỗ theo nhân viên lập phiếu (hoặc kênh Online).
 */
public interface StaffAggregationRow {

    Long getNhanVienId();

    String getMaNhanVien();

    String getHoTenNhanVien();

    Long getSoDonDatCho();

    BigDecimal getTongGiaGoc();

    BigDecimal getDoanhThu();
}
