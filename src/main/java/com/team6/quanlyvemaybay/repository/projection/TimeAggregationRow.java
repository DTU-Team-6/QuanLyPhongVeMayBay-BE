package com.team6.quanlyvemaybay.repository.projection;

import java.math.BigDecimal;

/**
 * Projection tổng hợp số lượng và doanh thu theo kỳ thời gian (năm / tháng / ngày).
 */
public interface TimeAggregationRow {

    Integer getNam();

    Integer getThang();

    Integer getNgay();

    Long getSoDonDatCho();

    Long getSoVeBanRa();

    BigDecimal getTongGiaGoc();

    BigDecimal getDoanhThu();
}
