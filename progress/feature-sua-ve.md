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

- Chờ người dùng chọn phạm vi cập nhật bản ghi dùng chung và trạng thái vé được phép sửa.

## Việc tiếp theo

1. Khi có quyết định nghiệp vụ, triển khai API, validation và test.
2. Chạy build, đồng bộ develop, push và mở PR riêng.
3. Xóa file tiến độ trước PR.

## Quyết định đã chốt và lưu ý

- Không thay đổi schema hoặc migration khi chưa được chấp thuận.
- Không sửa thông tin chuyến bay, giá hoặc thanh toán ngoài phạm vi SRS.

## Lỗi đang gặp

- Không có lỗi build; chưa viết code cho Task 3.
