# API thêm nhân viên

`POST /api/v1/nhan-vien` nhận JSON gồm `maNhanVien`, `hoTen`, `ngaySinh` (ISO `yyyy-MM-dd`), `gioiTinh` (`Nam`, `Nu`, `Khac`), `email`, `matKhau`, `soDienThoai` và `vaiTro` (`ADMIN` hoặc `NHAN_VIEN`). Mã và họ tên không chứa ký tự đặc biệt; số điện thoại gồm 10–13 chữ số. Mật khẩu 8–20 ký tự, được băm bằng BCrypt trước khi lưu và không có trong response.

Ví dụ body Postman (chỉ dùng dữ liệu giả trên database thử nghiệm):

```json
{
  "maNhanVien": "NV900",
  "hoTen": "Nguyen Van Test",
  "ngaySinh": "1995-02-03",
  "gioiTinh": "Nam",
  "email": "nv900@example.test",
  "matKhau": "TestOnly123",
  "soDienThoai": "0901234567",
  "vaiTro": "NHAN_VIEN"
}
```

Thành công trả HTTP 201, `Location: /api/v1/nhan-vien/{id}` và DTO nhân viên. Dữ liệu sai trả HTTP 400; email hoặc mã trùng trả HTTP 409. Các lỗi theo RFC 9457, có `code` và `details`.

Endpoint mặc định tắt vì chưa có xác thực/phân quyền. Chỉ để thử trong môi trường biệt lập, đặt `APP_NHAN_VIEN_ALLOW_UNSECURED=true` và `SERVER_ADDRESS=127.0.0.1` trong phiên chạy ứng dụng. Không bật trên database thật hoặc môi trường có thể truy cập từ bên ngoài. Phải tích hợp phân quyền ADMIN trước khi sử dụng thực tế.

Lưu ý: SRS mô tả ngày dạng `d/m/yyyy` cho giao diện; API JSON dùng ISO `yyyy-MM-dd` để Spring xử lý nhất quán. Project chưa có migration Flyway trên nhánh `develop`; API này không thay đổi schema.
