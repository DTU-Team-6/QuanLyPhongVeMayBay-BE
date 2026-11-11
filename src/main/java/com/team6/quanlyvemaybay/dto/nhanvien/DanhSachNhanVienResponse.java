package com.team6.quanlyvemaybay.dto.nhanvien;

import java.util.List;

public record DanhSachNhanVienResponse(List<NhanVienResponse> nhanVien, int trang, int kichThuoc,
                                       long tongSo, int tongTrang) {
}
