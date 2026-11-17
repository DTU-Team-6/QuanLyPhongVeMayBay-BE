# API đăng ký và đăng nhập (phạm vi tạm thời)

Các endpoint dùng tiền tố `/api/v1/auth`. Token không được trả trong JSON; sau khi đăng nhập, token được đặt trong cookie HttpOnly.

Trước khi dùng, áp dụng thủ công migration `src/main/resources/db/migration/V20261117_1331__them_phien_lam_moi.sql` vào database phù hợp. BE hiện chưa cấu hình Flyway hoặc tự chạy migration.

## Đăng nhập Google và Facebook

- Để bật OAuth, áp dụng thủ công thêm migration `src/main/resources/db/migration/V20261117_1552__them_lien_ket_oauth.sql` tạo bảng `dinh_danh_oauth` vào database được chọn; không đụng bảng `lien_ket_oauth` cũ. Đặt `APP_OAUTH2_ENABLED=true`, `APP_FRONTEND_URL` và cặp `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` hoặc `FACEBOOK_CLIENT_ID`/`FACEBOOK_CLIENT_SECRET` trong môi trường BE. Không đặt secret ở FE hoặc commit vào repo.
- Đăng ký URI callback BE với nhà cung cấp: `<địa chỉ BE>/login/oauth2/code/google` hoặc `<địa chỉ BE>/login/oauth2/code/facebook`. Cấu hình URL công khai HTTPS và các yêu cầu của nhà cung cấp phù hợp với môi trường.
- FE chuyển cả trang đến `/oauth2/authorization/google` hoặc `/oauth2/authorization/facebook`. BE xác thực qua OAuth2 rồi chuyển về `<APP_FRONTEND_URL>/auth/callback`; nếu có nhiều origin, dùng origin đầu tiên.
- Thành công cấp cookie phiên HttpOnly và FE gọi `/api/v1/auth/me`. OAuth hiện không áp dụng lựa chọn “Ghi nhớ đăng nhập”. Thất bại chuyển về callback với `error` là `OAUTH_FAILED`, `OAUTH_PROFILE_INVALID`, `EMAIL_ALREADY_REGISTERED` hoặc `ACCOUNT_INACTIVE`.
- Nhận diện theo nhà cung cấp và mã định danh, không tự liên kết khi email đã tồn tại. Người mới cần email và họ tên; Google phải xác nhận email. Tài khoản mới là khách hàng, không có mật khẩu, quốc tịch mặc định `Việt Nam`, chưa kiểm tra tuổi nếu thiếu ngày sinh. Facebook có thể không cung cấp email, khi đó đăng nhập thất bại.

## Luồng gọi từ frontend

1. Gọi `GET /api/v1/auth/csrf`, nhận cookie `XSRF-TOKEN`.
2. Với request `POST`, gửi cookie cùng header `X-XSRF-TOKEN` có giá trị tương ứng.
3. Gọi đăng ký hoặc đăng nhập với `credentials: "include"`.
4. Sau đăng nhập, trình duyệt tự gửi cookie `access_token`; gọi `/api/v1/auth/me` để lấy tài khoản hiện tại. Khi access token hết hạn, gọi `/api/v1/auth/refresh` rồi thử lại.

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
  "password": "MatKhau123",
  "rememberMe": true
}
```

- Thành công: `200 OK`, đặt cookie `access_token` và `refresh_token` HttpOnly, `SameSite=Lax`. JWT access có hạn 15 phút; refresh có hạn tối đa 7 ngày từ lúc đăng nhập.
- `rememberMe` tùy chọn, mặc định `false`. Nếu `false`, hai cookie là cookie phiên; nếu `true`, cookie có thời hạn. Việc khôi phục cookie phiên sau khi đóng trình duyệt phụ thuộc trình duyệt.
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

### `POST /api/v1/auth/refresh`

Gửi cookie `refresh_token` và CSRF. Thành công trả `204` và hai cookie mới; refresh token được thay mỗi lần sử dụng, không gia hạn quá 7 ngày tính từ lúc đăng nhập. Token hết hạn, bị thu hồi hoặc không hợp lệ trả `401 REFRESH_TOKEN_INVALID`. Nếu token đã dùng bị gửi lại, toàn bộ nhóm phiên đó bị thu hồi.

### `POST /api/v1/auth/logout`

Gửi CSRF và cookie; thành công trả `204`, thu hồi nhóm refresh token và xóa hai cookie.

### `GET /api/v1/auth/me`

Yêu cầu access token hợp lệ trong cookie `access_token` hoặc header Bearer. Trả thông tin cá nhân gồm `id`, `email`, `hoTen`, `soDienThoai`, `ngaySinh`, `gioiTinh`, `diaChi`, `anhDaiDien`, `vaiTro`; access token hết hạn hoặc không có sẽ nhận `401`.

### `PUT /api/v1/auth/me`

Cập nhật hồ sơ của tài khoản đang đăng nhập. Tất cả trường trong body đều được gửi theo dạng thay thế; các trường tùy chọn gửi `null` nếu muốn xoá giá trị. Email, vai trò và ảnh đại diện không được thay đổi qua API này.

```json
{
  "hoTen": "Nguyễn Văn A",
  "soDienThoai": "+84901234567",
  "ngaySinh": "1995-04-20",
  "gioiTinh": "Nam",
  "diaChi": "Đà Nẵng"
}
```

- Thành công: `200 OK`, trả hồ sơ đã cập nhật.
- Không có hoặc token không hợp lệ: `401 UNAUTHORIZED`.
- Request không hợp lệ: `400 VALIDATION_ERROR`.
- Cần gửi cookie và header CSRF như các request `POST`.

### `POST /api/v1/auth/change-password`

Yêu cầu đăng nhập và CSRF token hợp lệ. Mật khẩu hiện tại phải đúng; mật khẩu mới dài 8–72 ký tự và tối đa 72 byte UTF-8 (giới hạn BCrypt).

```json
{
  "currentPassword": "MatKhauCu123",
  "newPassword": "MatKhauMoi456"
}
```

- Thành công: `200 OK`, message `Đổi mật khẩu thành công`.
- Mật khẩu hiện tại không đúng: `400 CURRENT_PASSWORD_INVALID`.
- Mật khẩu mới trùng mật khẩu hiện tại: `400 PASSWORD_UNCHANGED`.
- Thu hồi refresh token của tài khoản. Access token đã phát hành vẫn có thể hợp lệ đến hết thời hạn tối đa 15 phút.

## Giới hạn đã biết của phiên bản tạm

- Refresh token yêu cầu bảng `phien_lam_moi`; migration phải được áp dụng thủ công trước khi sử dụng.
- Đổi mật khẩu không thu hồi access token đã phát hành; token cũ hết hạn theo TTL tối đa 15 phút.
- CSRF vẫn bật. FE phải gọi endpoint CSRF và gửi header tương ứng cho các request thay đổi dữ liệu.
- Báo cáo, thống kê, quản lý nhân viên, vé, tin tức và điểm đến ghi mới yêu cầu vai trò `ADMIN` hoặc `NHAN_VIEN`; tạo đặt chỗ yêu cầu đăng nhập. Tìm chuyến bay, thông tin công khai và đọc điểm đến là API công khai.
- Các API quản lý khách hàng `/api/v1/khach-hang/**` yêu cầu vai trò `ADMIN` hoặc `NHAN_VIEN`; request thay đổi dữ liệu phải có CSRF token hợp lệ.
- Tài khoản mẫu trong `data.sql` dùng hash SHA-1 cũ; không thể đăng nhập qua cơ chế BCrypt mới. Tạo tài khoản mới để kiểm thử.
- Ứng dụng yêu cầu đặt `JWT_SECRET` dài tối thiểu 32 byte. Trên HTTPS cần đặt `COOKIE_SECURE=true`; dev HTTP dùng `false`.

Lỗi trả theo `application/problem+json` với `code` và `details` theo quy ước chung của backend.
