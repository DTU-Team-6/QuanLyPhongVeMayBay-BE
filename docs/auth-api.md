# API đăng ký và đăng nhập (phạm vi tạm thời)

Các endpoint dùng tiền tố `/api/v1/auth`. Token không được trả trong JSON; sau khi đăng nhập, access JWT được đặt trong cookie `access_token`.

## Luồng gọi từ frontend

1. Gọi `GET /api/v1/auth/csrf`, nhận cookie `XSRF-TOKEN`.
2. Với request `POST`, gửi cookie cùng header `X-XSRF-TOKEN` có giá trị tương ứng.
3. Gọi đăng ký hoặc đăng nhập với `credentials: "include"`.
4. Sau đăng nhập, trình duyệt tự gửi cookie `access_token`; gọi `/api/v1/auth/me` để lấy tài khoản hiện tại.

Không gửi token trong response body hoặc tự lưu token vào `localStorage`.

## Endpoint

### `GET /api/v1/auth/csrf`

Khởi tạo CSRF cookie.

- Thành công: `204 No Content`
- Cookie: `XSRF-TOKEN` (được FE đọc), `SameSite=Lax`; `Secure` theo `COOKIE_SECURE`.

### `POST /api/v1/auth/register`

Tạo tài khoản khách hàng và hồ sơ khách hàng. Đăng ký không tự đăng nhập.

```json
{
  "email": "khach@example.com",
  "password": "MatKhau123",
  "hoTen": "Nguyễn Văn A",
  "soDienThoai": "+84901234567",
  "ngaySinh": "1995-04-20",
  "gioiTinh": "Nam"
}
```

`soDienThoai`, `ngaySinh`, `gioiTinh` là tùy chọn. Nếu thiếu `gioiTinh`, hệ thống dùng `Khac`. Vai trò do server gán là `KHACH_HANG`.

- Thành công: `201 Created`
- Email đã tồn tại: `409 EMAIL_ALREADY_REGISTERED`
- Request không hợp lệ: `400 VALIDATION_ERROR`

### `POST /api/v1/auth/login`

```json
{
  "email": "khach@example.com",
  "password": "MatKhau123"
}
```

- Thành công: `200 OK`, đặt cookie `access_token` HttpOnly, hạn 15 phút, `SameSite=Lax`.
- Sai thông tin đăng nhập: `401 INVALID_CREDENTIALS` (không tiết lộ email có tồn tại hay không).
- Tài khoản không hoạt động: `403 ACCOUNT_INACTIVE`.

Response thành công có dạng:

```json
{
  "statusCode": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "id": 123,
    "email": "khach@example.com",
    "hoTen": "Nguyễn Văn A",
    "vaiTro": "KHACH_HANG"
  }
}
```

### `GET /api/v1/auth/me`

Yêu cầu access token hợp lệ trong cookie `access_token` hoặc header Bearer. Trả `id`, `email`, `hoTen`, `vaiTro`; access token hết hạn hoặc không có sẽ nhận `401`.

## Giới hạn đã biết của phiên bản tạm

- Chưa có refresh token, rotation, revoke, phát hiện reuse hoặc logout; access token hết hạn sau 15 phút thì cần đăng nhập lại.
- CSRF vẫn bật. FE phải gọi endpoint CSRF và gửi header tương ứng cho các request thay đổi dữ liệu.
- Báo cáo, thống kê, quản lý nhân viên, vé, tin tức và điểm đến ghi mới yêu cầu vai trò `ADMIN` hoặc `NHAN_VIEN`; tạo đặt chỗ yêu cầu đăng nhập. Tìm chuyến bay, thông tin công khai và đọc điểm đến là API công khai.
- Tài khoản mẫu trong `data.sql` dùng hash SHA-1 cũ; không thể đăng nhập qua cơ chế BCrypt mới. Tạo tài khoản mới để kiểm thử.
- Ứng dụng yêu cầu đặt `JWT_SECRET` dài tối thiểu 32 byte. Trên HTTPS cần đặt `COOKIE_SECURE=true`; dev HTTP dùng `false`.

Lỗi trả theo `application/problem+json` với `code` và `details` theo quy ước chung của backend.
