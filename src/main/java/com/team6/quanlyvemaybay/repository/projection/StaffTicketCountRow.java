package com.team6.quanlyvemaybay.repository.projection;

/**
 * Projection đếm số lượng vé theo nhân viên lập đơn đặt chỗ.
 */
public interface StaffTicketCountRow {

    Long getNhanVienId();

    Long getSoVe();
}
