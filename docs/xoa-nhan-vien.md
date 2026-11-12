# API xóa mềm nhân viên

`DELETE /api/v1/nhan-vien/{id}` không có body. Thành công trả HTTP 200 với `id`, `maNhanVien`, `trangThai: 0`. Bản ghi ở `nhan_vien` và `nguoi_dung` vẫn tồn tại; chỉ `nguoi_dung.trang_thai` đổi từ `1` sang `0`. ID không tồn tại trả 404; đã ngừng hoạt động trả 409; ID không hợp lệ trả 400. Lỗi dùng RFC 9457 với `code` và `details`.

SRS mô tả thao tác xóa sau khi quản trị viên xác nhận; theo quy định nhóm và yêu cầu chức năng, Backend thực hiện xóa mềm. Việc hiển thị bước xác nhận thuộc giao diện, không nằm trong API này.

Endpoint yêu cầu đăng nhập với vai trò `ADMIN`. Với yêu cầu DELETE dùng cookie xác thực, FE phải gửi cookie `XSRF-TOKEN` và header `X-XSRF-TOKEN` tương ứng.
