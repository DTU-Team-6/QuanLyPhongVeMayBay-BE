# API quản lý khách hàng

Các API quản lý khách hàng dành cho `ADMIN` và `NHAN_VIEN`. Tất cả request thay đổi dữ liệu phải gửi cookie `XSRF-TOKEN` cùng header `X-XSRF-TOKEN`; JWT có thể được gửi trong cookie `access_token` như các API auth hiện tại.

Các endpoint dùng tiền tố `/api/v1/khach-hang`. API không trả mật khẩu hoặc hash mật khẩu.

## Danh sách khách hàng

`GET /api/v1/khach-hang?keyword=nguyen&page=0&size=10`

- `keyword` tùy chọn, tìm theo email, họ tên, số điện thoại hoặc số giấy tờ.
- `page` bắt đầu từ 0, mặc định `0`.
- `size` từ 1 đến 100, mặc định `10`.
- Danh sách bao gồm cả khách hàng đã ngừng hoạt động; xem trường `trangThai`.

## Chi tiết khách hàng

`GET /api/v1/khach-hang/{id}`

Trả hồ sơ khách hàng, gồm email, thông tin cá nhân, giấy tờ, quốc tịch, hạng, điểm tích lũy và trạng thái tài khoản.

## Thêm khách hàng

`POST /api/v1/khach-hang`

```json
{
  "email": "khach@example.com",
  "matKhau": "MatKhau123",
  "hoTen": "Nguyễn Văn A",
  "soDienThoai": "+84901234567",
  "ngaySinh": "1995-04-20",
  "gioiTinh": "Nam",
  "diaChi": "Đà Nẵng",
  "cmndPassport": "012345678901",
  "quocTich": "Việt Nam",
  "xepHang": "Member",
  "diemTichLuy": 0
}
```

- Bắt buộc: `email`, `hoTen`.
- `matKhau` tùy chọn. Nếu bỏ qua, khách được tạo để phục vụ bán tại quầy và chưa thể đăng nhập. Nếu có, mật khẩu được băm BCrypt.
- Nếu bỏ qua, `gioiTinh`, `quocTich`, `xepHang`, `diemTichLuy` mặc định lần lượt là `Khac`, `Việt Nam`, `Member`, `0`.
- `xepHang` nhận một trong `Member`, `Silver`, `Gold`, `Diamond`; điểm tích lũy không âm.
- Thành công: `201 Created`.
- Email đã tồn tại: `409 EMAIL_ALREADY_REGISTERED`.

## Cập nhật khách hàng

`PUT /api/v1/khach-hang/{id}`

Body là thông tin thay thế đầy đủ. Các trường bắt buộc: `email`, `hoTen`, `gioiTinh`, `quocTich`, `xepHang`, `diemTichLuy`. Các trường liên hệ và giấy tờ có thể gửi `null` để xóa giá trị. Mật khẩu không được cập nhật qua API này.

```json
{
  "email": "khach@example.com",
  "hoTen": "Nguyễn Văn A",
  "soDienThoai": "+84901234567",
  "ngaySinh": "1995-04-20",
  "gioiTinh": "Nam",
  "diaChi": "Đà Nẵng",
  "cmndPassport": "012345678901",
  "quocTich": "Việt Nam",
  "xepHang": "Silver",
  "diemTichLuy": 250
}
```

- Thành công: `200 OK`.
- Không tìm thấy khách hàng: `404 CUSTOMER_NOT_FOUND`.
- Email đã tồn tại hoặc khách hàng đã ngừng hoạt động: `409`.

## Ngừng hoạt động khách hàng

`DELETE /api/v1/khach-hang/{id}`

Đây là xóa mềm: hệ thống đặt `nguoi_dung.trang_thai` thành `0`, không xóa dữ liệu để giữ lịch sử đặt chỗ. Khách hàng đã ngừng hoạt động không thể đăng nhập; yêu cầu ngừng hoạt động lặp lại trả `409 CUSTOMER_ALREADY_INACTIVE`.

- Thành công: `200 OK`, dữ liệu trả về có `id` và `trangThai: 0`.
- Không tìm thấy khách hàng: `404 CUSTOMER_NOT_FOUND`.

Lỗi validation trả theo `application/problem+json` với `code` và mảng `details` theo quy ước chung.
