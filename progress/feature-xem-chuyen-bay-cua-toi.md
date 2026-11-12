# Tiến độ nhánh feature/xem-chuyen-bay-cua-toi

## Task hiện tại

- Chức năng: Xem chuyến bay của tôi không cần đăng nhập (99.6)
- Nhánh: `feature/xem-chuyen-bay-cua-toi`
- Cập nhật: 2026-11-12

## Đã xong

- Nhánh được tạo từ `origin/develop` tại `2730419`.
- Đã kiểm tra SRS v1.2 và schema; SRS không mô tả cách xác minh chủ đặt chỗ.
- Đã hỏi người dùng chọn điều kiện xác minh trước khi triển khai endpoint công khai.
- Người dùng đã chốt mã đặt chỗ + email + số điện thoại.
- API đã gọi HTTP: đúng thông tin 200, ba loại sai cùng 404, thiếu CSRF 403, sai định dạng 400, lần thứ 11 cùng IP 429.
- Response chỉ có hai chuyến bay riêng biệt cho đặt chỗ thử nghiệm; SQL xác nhận liên kết dữ liệu.
- `gradlew build --no-daemon --rerun-tasks` thành công: 124 test, 0 lỗi, 0 bỏ qua.

## Đang dở

- Cần commit và hoàn thành Git Flow.

## Việc tiếp theo

1. Commit, fetch và merge develop; kiểm thử lại.
2. Push và cung cấp link tạo PR.

## Quyết định đã chốt và lưu ý

- Không dùng database chung và không đổi schema nếu không cần thiết.
- Không cho phép liệt kê dữ liệu đặt chỗ công khai.

## Lỗi đang gặp

- Chưa có.
