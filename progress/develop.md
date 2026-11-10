# Tiến độ nhánh `develop`

## Task hiện tại

- Chức năng: API Trang chủ, Header/Footer (`GET /api/v1/public/common-info`) và Top 10 chuyến bay giá rẻ (`GET /api/v1/flights/top-cheap`)
- Nhánh: `develop` (đồng bộ từ `main` + cherry-pick `ee247bb` ở `3db34b6`)
- Cập nhật lần cuối: 2026-11-10

## Đã xong (đã commit)

- `3db34b6`: Đồng bộ `schema.sql` (`CHAR(64)` trên `xac_thuc_token`, bỏ `created_at` trên `chuyen_bay`) và `DATABASE_DESIGN.md` theo PDF
- `1595879`: Bước 1 - Thiết lập `build.gradle`, `application.yml`, `CorsConfig`, `OpenApiConfig`, `CommonInfoProperties`, `ApiResponse`, `GlobalExceptionHandler` (RFC 9457)
- `bfdfaad`: Bước 2 - Tạo Entity, Enum và Repository (`SanBay`, `HangHangKhong`, `ChuyenBay`, `HangVeChuyenBay`, `TinTuc`)
- `b681003`: Bước 3 - Xây dựng `GET /api/v1/flights/top-cheap` (`TimeConfig`, DTO flight, `ChuyenBayService`, `ChuyenBayController`, `ChuyenBayServiceTest` 2/2 PASS, `ChuyenBayControllerTest` 4/4 PASS)
- Bước 4: Xây dựng `GET /api/v1/public/common-info` (`dto/publicinfo/*`, `ThongTinChungService`, `CongKhaiController`, `ThongTinChungServiceTest` 2/2 PASS, `CongKhaiControllerTest` 3/3 PASS)

## Đang dở (chưa commit hoặc chưa chạy được)

- Bước 5: Kiểm thử thực tế (MySQL / Swagger / endpoint), kiểm tra `QuanlyvemaybayApplicationTests` và `./gradlew build`

## Việc tiếp theo (theo thứ tự)

1. Kiểm tra chạy thực tế với MySQL (`bootRun` / curl / Swagger) nếu DB sẵn sàng
2. Chạy `./gradlew build` kiểm tra tổng thể
3. Xoá `progress/develop.md` khi hoàn thành xong để sẵn sàng tạo PR

## Quyết định đã chốt và lưu ý

- Dùng DB `quanlyvemaybay` theo `schema.sql` hiện tại (`ddl-auto: none`, không thêm bảng mới).
- Giữ path đề bài (`/api/v1/flights/top-cheap`, `/api/v1/public/common-info`); success trả `{statusCode, message, data}`, lỗi theo RFC 9457 (`ProblemDetail` + `code` + `details`).
- `common-info` đọc cấu hình tĩnh từ `application.yml` kết hợp query bài viết `HIEN_THI` từ `tin_tuc`.
- Đã cài Temurin JDK 21 (`21.0.13+9`).

## Lỗi đang gặp (nếu có)

- (Không có)
