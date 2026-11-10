# Tiến độ nhánh `feature/reports_statistics-api`

## Task hiện tại

- Chức năng: Báo cáo & Thống kê (`POST /api/v1/reports`, `GET /api/v1/reports/generate`, `GET /api/v1/statistics`)
- Nhánh: `feature/reports_statistics-api` (tách từ `feature/home_top-cheap-common-info`)
- Cập nhật lần cuối: 2026-11-10

## Đã xong (đã commit)

- Bước 1: Tạo Entities (`NguoiDung`, `NhanVien`, `DatCho`, `VeMayBay`), Enums (`VaiTro`, `GioiTinh`, `LoaiChuyenBay`, `TrangThaiDatCho`, `ChieuBay`, `TrangThaiVe`) và nâng cấp `GlobalExceptionHandler` xử lý `HttpMessageNotReadableException` + `isBindingFailure()` — `./gradlew build -x test` PASS

## Đang dở (đưa sang bước tiếp)

- Bước 2: Tạo custom validator `@ValidDateRange`, các DTO lọc & kết quả (`dto/report/*`), và các truy vấn Aggregation (`SUM`, `COUNT`, `GROUP BY`) trong Repository

## Việc tiếp theo (theo thứ tự)

1. Bước 2: Validation `@ValidDateRange`, DTOs & Repositories -> `./gradlew build -x test` -> commit
2. Bước 3: `BaoCaoService` & `BaoCaoController` -> `./gradlew build -x test` -> commit
3. Bước 4: `BaoCaoServiceTest` & `BaoCaoControllerTest` -> `./gradlew clean build` -> xoá file tiến độ & push nhánh
