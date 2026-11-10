# Tiến độ nhánh `develop`

## Task hiện tại

- Chức năng: API Trang chủ, Header/Footer (`GET /api/v1/public/common-info`) và Top 10 chuyến bay giá rẻ (`GET /api/v1/flights/top-cheap`)
- Nhánh: `develop` (đồng bộ từ `main` + cherry-pick `ee247bb` ở `3db34b6`)
- Cập nhật lần cuối: 2026-11-10

## Đã xong (đã commit)

- `3db34b6`: Đồng bộ `schema.sql` (`CHAR(64)` trên `xac_thuc_token`, bỏ `created_at` trên `chuyen_bay`) và `DATABASE_DESIGN.md` theo PDF
- `1595879`: Bước 1 - Thiết lập `build.gradle`, `application.yml`, `CorsConfig`, `OpenApiConfig`, `CommonInfoProperties`, `ApiResponse`, `GlobalExceptionHandler` (RFC 9457)
- `bfdfaad`: Bước 2 - Tạo Entity, Enum và Repository (`SanBay`, `HangHangKhong`, `ChuyenBay`, `HangVeChuyenBay`, `TinTuc`)
- `b681003`: Bước 3 - Xây dựng `GET /api/v1/flights/top-cheap` (`TimeConfig`, DTO flight, `ChuyenBayService`, `ChuyenBayController`, tests)
- `3e127a8`: Bước 4 - Xây dựng `GET /api/v1/public/common-info` (`dto/publicinfo/*`, `ThongTinChungService`, `CongKhaiController`, tests)
- Bước 5: Bổ sung xử lý `TransactionException` -> `DATABASE_ERROR` trong `GlobalExceptionHandler`, thêm test trong `ChuyenBayControllerTest`, chạy `./gradlew clean build` PASS 13/13 tests (`QuanlyvemaybayApplicationTests`: 1, `ChuyenBayServiceTest`: 2, `ThongTinChungServiceTest`: 2, `ChuyenBayControllerTest`: 5, `CongKhaiControllerTest`: 3). Kiểm tra thực tế bằng `curl` xác nhận `GET /v3/api-docs` (200), `GET /swagger-ui/index.html` (200) và `GET /api/v1/flights/top-cheap?limit=0` (400 RFC 9457).

## Đang dở (chưa commit hoặc chưa chạy được)

- Kiểm thử trực tiếp 2 endpoint `/api/v1/flights/top-cheap` và `/api/v1/public/common-info` với MySQL thật bằng Postman/Swagger sau khi nạp `$env:DB_PASSWORD` (và đảm bảo đã chạy `schema.sql` + `data.sql`).

## Việc tiếp theo (theo thứ tự)

1. Đặt biến môi trường `$env:DB_PASSWORD` đúng với MySQL local và chạy `.\gradlew.bat bootRun` để kiểm thử live trên Swagger/Postman.
2. Khi mở Pull Request: xoá `progress/develop.md` (`git rm progress/develop.md`) trước khi đẩy nhánh review.

## Quyết định đã chốt và lưu ý

- Dùng DB `quanlyvemaybay` theo `schema.sql` hiện tại (`ddl-auto: none`, không thêm bảng mới).
- Giữ path đề bài (`/api/v1/flights/top-cheap`, `/api/v1/public/common-info`); success trả `{statusCode, message, data}`, lỗi theo RFC 9457 (`ProblemDetail` + `code` + `details`).
- `common-info` đọc cấu hình tĩnh từ `application.yml` kết hợp query bài viết `HIEN_THI` từ `tin_tuc`.
- Đã cài Temurin JDK 21 (`21.0.13+9`).

## Lỗi đang gặp (nếu có)

- Khi chạy thử live jar mà chưa truyền `$env:DB_PASSWORD`, MySQL trả `Access denied for user 'root'@'localhost' (using password: NO)`. Cần đặt `$env:DB_PASSWORD` khi chạy `bootRun`.
