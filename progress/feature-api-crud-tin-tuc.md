# Tiến độ nhánh feature/api-crud-tin-tuc

## Task hiện tại

- Chức năng: Viết các API CRUD (GET danh sách & chi tiết, POST thêm, PUT sửa, DELETE xóa) cho bảng Tin tức (khớp 4 ảnh giao diện Quản lý tin tức FE).
- Nhánh: `feature/api-crud-tin-tuc`
- Cập nhật lần cuối: 2026-11-11 12:50, Antigravity

## Đã xong (đã commit)

- Nhánh được tạo từ `origin/develop`.

## Đang dở (chưa commit hoặc chưa chạy được)

- Thiết kế DTOs:
  1. `NewsItemDto` (hiển thị thẻ tin tức ở danh sách: mã tin TT001, tiêu đề, mô tả ngắn, ảnh đại diện, chuyên mục, ngày đăng, tác giả).
  2. `NewsDetailDto` (chi tiết bài viết phục vụ xem/sửa).
  3. `CreateNewsRequest` (thêm tin tức mới).
  4. `UpdateNewsRequest` (chỉnh sửa tin tức).
  5. `NewsFilterRequest` (bộ lọc từ khóa, chuyên mục, phân trang).
- Mở rộng `TinTucRepository` (tìm kiếm theo từ khóa, lọc theo loại tin, phân trang).
- Viết `TinTucService` triển khai nghiệp vụ CRUD:
  - `GET /api/v1/tin-tuc`: Danh sách bài viết có phân trang, tìm kiếm từ khóa, lọc chuyên mục.
  - `GET /api/v1/tin-tuc/{id}`: Chi tiết bài viết.
  - `POST /api/v1/tin-tuc`: Thêm bài viết mới.
  - `PUT /api/v1/tin-tuc/{id}`: Sửa bài viết.
  - `DELETE /api/v1/tin-tuc/{id}`: Xóa bài viết (phục vụ popup xác nhận xóa).
- Viết `TinTucController`.

## Việc tiếp theo (theo thứ tự)

1. Tạo các DTO trong package `com.team6.quanlyvemaybay.dto.news`.
2. Bổ sung các query method vào `TinTucRepository`.
3. Viết `TinTucService` và `TinTucController`.
4. Viết kịch bản kiểm thử tự động `test-tintuc-crud.ps1` chạy trên Spring Boot và MariaDB thật.
5. Kiểm tra build gradle và commit từng chức năng.

## Quyết định đã chốt và lưu ý

- Khớp 100% 4 ảnh giao diện FE người dùng gửi:
  - Mã tin định dạng `TT%03d` (ví dụ TT001).
  - Tác giả hiển thị (Vân Việt / Admin).
  - Chuẩn RFC 9457 khi báo lỗi (404 NEWS_NOT_FOUND, 400 INVALID_REQUEST).
  - Sử dụng enum `LoaiTinTuc` và `TrangThaiTinTuc` hiện có trong schema.

## Lỗi đang gặp (nếu có)

- Chưa có lỗi.
