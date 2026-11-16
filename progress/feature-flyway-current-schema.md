# Tiến độ – Đồng bộ Flyway database

## Task hiện tại

- Chức năng: Quản lý schema local bằng Flyway và hướng dẫn dựng môi trường.
- Nhánh: `feature/flyway-current-schema`
- Cập nhật lần cuối: 2026-11-16

## Đã xong (đưa vào commit hiện tại)

- Thêm migration baseline V1 cho 23 bảng và V2 cho dữ liệu danh mục.
- Bật Flyway cho Spring Boot và cấu hình các task Gradle Flyway.
- Điều chỉnh hai migration snapshot để dùng bảng `ve` và cột email đặt chỗ đúng với schema hiện tại.
- Cập nhật hướng dẫn database local cho macOS và Windows; thêm các file dump cục bộ vào `.gitignore`.
- `./gradlew build -x test` đã thành công. Chủ database local đã chạy `flywayMigrate` và xác nhận bốn migration thành công.

## Đang dở

- Chưa mở Pull Request vào `develop`.

## Việc tiếp theo

1. Nhóm review và merge nhánh này vào `develop`.
2. Trước khi mở Pull Request, xóa file tiến độ này theo quy trình repo.

## Quyết định đã chốt và lưu ý

- Mỗi thành viên dùng database MySQL local riêng; Flyway đồng bộ schema qua migration trong repo, không đồng bộ dữ liệu.
- Với database đã có Flyway history, cần kiểm tra history và sao lưu trước khi migrate hoặc repair.
- Không commit các file dump schema cục bộ.

## Lỗi đang gặp

- Không có lỗi build được ghi nhận; chưa chạy full test suite.
