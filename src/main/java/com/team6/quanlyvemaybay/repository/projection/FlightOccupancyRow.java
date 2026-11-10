package com.team6.quanlyvemaybay.repository.projection;

/**
 * Projection tổng hợp công suất ghế và tỷ lệ lấp đầy chuyến bay theo kỳ thời gian.
 */
public interface FlightOccupancyRow {

    Integer getNam();

    Integer getThang();

    Integer getNgay();

    Long getSoChuyenBay();

    Long getTongSoGhe();

    Long getSoGheTrong();
}
