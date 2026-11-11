package com.team6.quanlyvemaybay.dto.nhanvien;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import java.time.LocalDate;

public record NhanVienResponse(Long id, String maNhanVien, String hoTen, LocalDate ngaySinh,
                               GioiTinh gioiTinh, String email, String soDienThoai,
                               VaiTro vaiTro, Byte trangThai) {
}
