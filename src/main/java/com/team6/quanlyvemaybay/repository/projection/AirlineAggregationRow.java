package com.team6.quanlyvemaybay.repository.projection;

import java.math.BigDecimal;

/**
 * Projection tổng hợp doanh thu và số lượng vé theo hãng hàng không.
 */
public interface AirlineAggregationRow {

    String getMaHang();

    String getTenHang();

    Long getSoDonDatCho();

    Long getSoVeBanRa();

    BigDecimal getTongGiaGoc();

    BigDecimal getDoanhThu();
}
