# Tiến độ: đăng ký và đăng nhập

## Task hiện tại

- Chức năng: API đăng ký, đăng nhập và xác thực người dùng.
- Nhánh: `feature/auth-register-login`.
- Cập nhật lần cuối: 2026-11-11.

## Đã xong

- Thêm API `GET /api/v1/auth/csrf`, `POST /api/v1/auth/register`, `POST /api/v1/auth/login` và `GET /api/v1/auth/me`.
- Đăng ký tài khoản khách hàng với mật khẩu BCrypt, chuẩn hóa email và tạo hồ sơ `khach_hang`.
- Cấp access JWT HS256 15 phút qua cookie HttpOnly; bật CSRF và phân quyền các API hiện có.
- Thêm tài liệu API, cấu hình môi trường và test cho auth, JWT, CSRF, quyền truy cập.
- `./gradlew build` thành công; `env -u JWT_SECRET ./gradlew test --rerun-tasks` thành công.

## Đang dở

- Chưa có thay đổi schema hoặc migration trong task này.

## Việc tiếp theo

1. Tích hợp và kiểm thử luồng cookie/CSRF với frontend.
2. Triển khai refresh token, rotation, revoke, phát hiện reuse và logout sau khi có migration database được phê duyệt.
3. Bổ sung OAuth2 Google/Facebook theo đặc tả riêng.

## Quyết định đã chốt và lưu ý

- Phạm vi tạm thời chỉ gồm access JWT; chưa triển khai refresh token, logout hoặc OAuth2.
- JWT lấy từ `JWT_SECRET` (tối thiểu 32 byte); HTTPS cần đặt `COOKIE_SECURE=true`.
- Đăng ký chỉ tạo vai trò `KHACH_HANG`; không nhận vai trò từ client.
- Repository hiện không có file migration Flyway; `ddl-auto` vẫn là `none`. Không chạy trực tiếp `schema.sql` vì có lệnh `DROP TABLE`.
- Tài khoản mẫu dùng hash SHA-1 cũ nên không đăng nhập được bằng BCrypt; dùng tài khoản mới để kiểm thử.
