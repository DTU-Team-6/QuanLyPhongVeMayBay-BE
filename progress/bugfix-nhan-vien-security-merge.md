# Tiến độ nhánh bugfix/nhan-vien-security-merge

## Task hiện tại

- Chức năng: sửa lỗi tích hợp API thêm, sửa, xóa nhân viên với Spring Security.
- Nhánh: `bugfix/nhan-vien-security-merge`.
- Cập nhật lần cuối: 2026-11-12, Codex.

## Đã xong

- Bỏ điều kiện tắt mặc định của `NhanVienController`.
- Giới hạn API quản lý nhân viên cho `ADMIN`.
- Sau khi merge `origin/develop`, dùng `CookieAccessTokenFilter` và bearer resolver tách cookie khỏi header của `develop` để Spring Security áp dụng CSRF cho cookie.
- Cập nhật test và tài liệu API. Sau khi merge `origin/develop`, `build` chạy thành công qua ổ đĩa ánh xạ ASCII; 105 test, 0 lỗi.

## Đang dở

- Chưa kiểm tra ba API bằng ứng dụng chạy thực tế với database thử nghiệm.

## Việc tiếp theo

1. Kiểm tra thêm, sửa, xóa nhân viên trên database thử nghiệm nếu cần xác nhận luồng ghi dữ liệu thực tế.
2. Trước khi mở Pull Request, xóa file tiến độ này trong commit cuối theo quy định repository.

## Quyết định đã chốt và lưu ý

- Không đổi schema, migration, dependency hoặc logic nghiệp vụ của service.
- Không push trực tiếp lên `develop` hoặc `main`.

## Lỗi đang gặp

- `gradlew.bat` trên đường dẫn có dấu báo `Unable to access jarfile ... gradle-wrapper.jar`; chạy wrapper từ ổ đĩa ánh xạ ASCII đã thành công.
