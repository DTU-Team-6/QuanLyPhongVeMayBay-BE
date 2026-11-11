# API sửa thông tin nhân viên

`PUT /api/v1/nhan-vien/{id}` cập nhật toàn bộ các trường được phép sửa: `hoTen`, `ngaySinh`, `gioiTinh`, `email`, `soDienThoai`, `vaiTro`. Mã nhân viên và mật khẩu không nằm trong request và không bị thay đổi. `ngaySinh` dùng định dạng ISO `yyyy-MM-dd` trong JSON; SRS mô tả `d/m/yyyy` cho giao diện.

Ví dụ body Postman trên database thử nghiệm biệt lập:

```json
{
  "hoTen": "Nhan Vien Moi",
  "ngaySinh": "1991-01-02",
  "gioiTinh": "Nam",
  "email": "moi@example.test",
  "soDienThoai": "0903333333",
  "vaiTro": "NHAN_VIEN"
}
```

Thành công trả HTTP 200 và DTO không có mật khẩu. ID nhân viên không tồn tại trả 404; email của người khác đã dùng trả 409; dữ liệu sai hoặc vai trò khác `ADMIN`/`NHAN_VIEN` trả 400. Lỗi theo RFC 9457 với `code` và `details`.

Endpoint mặc định tắt khi chưa có phân quyền. Chỉ bật tạm để thử với database biệt lập bằng `APP_NHAN_VIEN_ALLOW_UNSECURED=true`; đặt `SERVER_ADDRESS=127.0.0.1` để giới hạn truy cập trên máy. Không bật trên dữ liệu thật. Cần phân quyền ADMIN trước khi sử dụng thực tế.
