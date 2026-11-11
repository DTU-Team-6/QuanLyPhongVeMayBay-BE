## Task hiện tại

- Chức năng: API quản lý khách hàng
- Nhánh: `feature/quan-ly-khach-hang`
- Cập nhật lần cuối: 2026-11-11

## Đã xong (đã commit)

- API GET danh sách khách hàng có phân trang, tìm kiếm và giới hạn quyền.
- API GET chi tiết khách hàng; test service/controller thành công.
- API POST tạo khách hàng, mật khẩu tùy chọn và BCrypt khi có.
- Sửa CSRF để token trong cookie không làm request bỏ qua kiểm tra CSRF.
- API PUT cập nhật hồ sơ và DELETE ngừng hoạt động mềm; test service/controller mục tiêu thành công.
- Tài liệu API khách hàng và phân quyền đã được cập nhật.
- `./gradlew build` thành công.

## Đang dở (chưa commit hoặc chưa chạy được)

- Chưa có.

## Việc tiếp theo (theo thứ tự)

1. Trước khi mở PR, xóa file tiến độ này theo quy trình dự án.

## Quyết định đã chốt và lưu ý

- Chỉ `ADMIN` và `NHAN_VIEN` được quản lý khách hàng.
- Có API danh sách và chi tiết.
- Tạo khách hàng cho phép bỏ trống mật khẩu để tạo hồ sơ tại quầy.
- Sửa các trường hồ sơ khách hàng đã thống nhất; không cho sửa mật khẩu qua API quản lý.
- Xóa là ngừng hoạt động mềm; không đổi schema.

## Lỗi đang gặp (nếu có)

- Chưa có.
