## Task hiện tại

- Chức năng: API quản lý khách hàng
- Nhánh: `feature/quan-ly-khach-hang`
- Cập nhật lần cuối: 2026-11-11

## Đã xong (đã commit)

- API GET danh sách khách hàng có phân trang, tìm kiếm và giới hạn quyền.
- API GET chi tiết khách hàng; test service/controller thành công.

## Đang dở (chưa commit hoặc chưa chạy được)

- Chưa có.

## Việc tiếp theo (theo thứ tự)

1. Thêm API tạo khách hàng với mật khẩu tùy chọn.
2. Thêm API sửa hồ sơ khách hàng.
3. Thêm API ngừng hoạt động mềm.
4. Hoàn thiện tài liệu, chạy build và kiểm tra nhánh.

## Quyết định đã chốt và lưu ý

- Chỉ `ADMIN` và `NHAN_VIEN` được quản lý khách hàng.
- Có API danh sách và chi tiết.
- Tạo khách hàng cho phép bỏ trống mật khẩu để tạo hồ sơ tại quầy.
- Sửa các trường hồ sơ khách hàng đã thống nhất; không cho sửa mật khẩu qua API quản lý.
- Xóa là ngừng hoạt động mềm; không đổi schema.

## Lỗi đang gặp (nếu có)

- Chưa có.
