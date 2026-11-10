# Tiến độ nhánh `feature/reports_statistics-api`

## Task hiện tại

- Chức năng: Báo cáo & Thống kê (`POST /api/v1/reports`, `GET /api/v1/reports/generate`, `GET /api/v1/statistics`)
- Nhánh: `feature/reports_statistics-api` (tách từ `feature/home_top-cheap-common-info`)
- Cập nhật lần cuối: 2026-11-10

## Đã xong (đã commit)

- `438dfa1`: Bước 1 - Entities (`NguoiDung`, `NhanVien`, `DatCho`, `VeMayBay`), Enums và nâng cấp `GlobalExceptionHandler`
- `bd3c4f0`: Bước 2 - Validation `@ValidDateRange`, bộ DTO lọc/kết quả và Repository Aggregation Queries
- Bước 3: `BaoCaoService`, `BaoCaoController` (`POST /api/v1/reports`, `GET /api/v1/reports/generate`) và `ThongKeController` (`GET /api/v1/statistics`) — `./gradlew build -x test` PASS

## Đang dở (đưa sang bước tiếp)

- Bước 4: Viết Unit Test (`BaoCaoServiceTest`) và WebMvc Test (`BaoCaoControllerTest`, `ThongKeControllerTest`) cho toàn bộ trường hợp lọc, rỗng, thiếu tham số, `startDate > endDate`, sai kiểu ngày/enum

## Việc tiếp theo (theo thứ tự)

1. Bước 4: `BaoCaoServiceTest`, `BaoCaoControllerTest`, `ThongKeControllerTest` -> `./gradlew clean build` -> commit -> xoá file tiến độ & push nhánh
