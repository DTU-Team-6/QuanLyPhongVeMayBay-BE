# Tiến độ nhánh `feature/reports_statistics-api`

## Task hiện tại

- Chức năng: Báo cáo & Thống kê (`POST /api/v1/reports`, `GET /api/v1/reports/generate`, `GET /api/v1/statistics`)
- Nhánh: `feature/reports_statistics-api` (tách từ `feature/home_top-cheap-common-info`)
- Cập nhật lần cuối: 2026-11-10

## Đã xong (đã commit)

- `438dfa1`: Bước 1 - Entities (`NguoiDung`, `NhanVien`, `DatCho`, `VeMayBay`), Enums và nâng cấp `GlobalExceptionHandler`
- Bước 2: Validation `@ValidDateRange`, bộ DTO lọc & kết quả (`dto/report/*`), Projections và Repository Aggregation (`DatChoRepository`, `VeMayBayRepository`, `HangVeChuyenBayRepository`) — `QuanlyvemaybayApplicationTests` PASS (toàn bộ JPQL queries hợp lệ với Hibernate 7)

## Đang dở (đưa sang bước tiếp)

- Bước 3: `BaoCaoService` & `BaoCaoController` (`POST /api/v1/reports`, `GET /api/v1/reports/generate`, `GET /api/v1/statistics`)

## Việc tiếp theo (theo thứ tự)

1. Bước 3: `BaoCaoService` & `BaoCaoController` -> `./gradlew build -x test` -> commit
2. Bước 4: `BaoCaoServiceTest` & `BaoCaoControllerTest` -> `./gradlew clean build` -> xoá file tiến độ & push nhánh
