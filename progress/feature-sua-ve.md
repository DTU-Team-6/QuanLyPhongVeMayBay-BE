# Tiến độ nhánh feature/sua-ve

## Task hiện tại

- Chức năng: 6.6.1.11 sửa vé.
- Nhánh: `feature/sua-ve` từ `origin/develop`.
- Cập nhật lần cuối: 2026-11-12, Codex.

## Đã xong

- Đọc SRS v1.2: màn hình sửa vé chỉ có tên người đi (10–50 ký tự) và email thanh toán hợp lệ.
- Kiểm tra schema/entity: tên ở `hanh_khach`, email người đặt ở `dat_cho`; hai bản ghi có thể dùng chung cho nhiều vé.
- Xác nhận SRS chưa quy định trạng thái vé được phép sửa.

## Đang dở

- Người dùng đã chọn snapshot riêng theo vé và chỉ sửa khi vé/đặt chỗ chưa thanh toán. Đã viết đề xuất thiết kế trong `docs/de-xuat-sua-ve.md`; chờ duyệt schema và cơ chế migration trước khi sửa code liên quan.

## Việc tiếp theo

1. Khi thiết kế schema/migration được duyệt, triển khai API, validation và test.
2. Chạy build, đồng bộ develop, push và mở PR riêng.
3. Xóa file tiến độ trước PR.

## Quyết định đã chốt và lưu ý

- Không thay đổi schema hoặc migration khi chưa được chấp thuận.
- Không sửa thông tin chuyến bay, giá hoặc thanh toán ngoài phạm vi SRS.
- Quy tắc chỉ sửa trước thanh toán là tạm thời, cần BA/mentor xác nhận.

## Lỗi đang gặp

- MySQL80 đang chạy nhưng thiếu credential trong phiên này; truy vấn metadata bằng root không mật khẩu bị từ chối.
- Repository chưa có Flyway runtime hoặc migration dù tài liệu `agents-docs/be/03-database.md` mô tả Flyway.
