# Tiến độ nhánh `feature/api-diem-den`

## Task hiện tại

- Chức năng: Quản lý điểm đến (`GET /api/v1/destinations`, `POST /api/v1/destinations`) — màn hình 99.7.3
- Nhánh: `feature/api-diem-den` (tách từ `develop` `08fb1d8`)
- Cập nhật lần cuối: 2026-11-11

## Đã xong (đã commit)

- Bước 1: Tạo `DiemDen` entity và `DiemDenRepository` (`findWithFilters` phân trang, `existsDuplicateInAirport`) — `QuanlyvemaybayApplicationTests` PASS

## Đang dở (chưa commit hoặc chưa chạy được)

- Bước 2: DTOs (`CreateDestinationRequest`, `DestinationResponse`), `DiemDenService`, `DiemDenController`

## Việc tiếp theo (theo thứ tự)

1. Bước 2: DTOs, `DiemDenService`, `DiemDenController` -> `./gradlew build -x test` -> commit
2. Bước 3: `DiemDenServiceTest`, `DiemDenControllerTest` -> `./gradlew clean build` -> commit -> xoá file tiến độ và push nhánh

## Quyết định đã chốt và lưu ý

- Dùng đúng 4 trường bảng `diem_den` (`ten_diem_den`, `ma_san_bay`, `mo_ta`, `hinh_anh`), không sửa `schema.sql`.
- Bắt buộc `tenDiemDen` (<=100 ký tự) và `maSanBay` (<=10 ký tự, tồn tại trong `san_bay`); `moTa` và `hinhAnh` tuỳ chọn.
- Trùng `(tenDiemDen, maSanBay)` trả HTTP 409 (`DESTINATION_ALREADY_EXISTS`); mã sân bay không tồn tại trả HTTP 404 (`AIRPORT_NOT_FOUND`).
- `GET /api/v1/destinations` hỗ trợ `keyword`, `maSanBay`, `page=0`, `size=10`, trả `ApiResponse<PageResponse<DestinationResponse>>`.

## Lỗi đang gặp (nếu có)

- (Chưa có)
