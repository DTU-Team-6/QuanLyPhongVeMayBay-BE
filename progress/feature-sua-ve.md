# Tiến độ nhánh feature/sua-ve

## Task hiện tại

- Chức năng: 6.6.1.11 sửa vé.
- Nhánh: `feature/sua-ve` từ `origin/develop`.
- Cập nhật lần cuối: 2026-11-12, Codex.

## Đã xong

- Đọc SRS v1.2: màn hình sửa vé chỉ có tên người đi (10–50 ký tự) và email thanh toán hợp lệ.
- Kiểm tra schema/entity: tên ở `hanh_khach`, email người đặt ở `dat_cho`; hai bản ghi có thể dùng chung cho nhiều vé.
- Xác nhận SRS chưa quy định trạng thái vé được phép sửa.
- Đã chuẩn bị entity snapshot, API `PUT /api/v1/ve-may-bay/{id}`, DTO validation, service khóa vé và chỉ cập nhật hai trường của vé; danh sách/tìm kiếm tên đọc snapshot.
- Đã chuẩn bị hai file Flyway version `20261112_1100` (thêm cột nullable) và `20261112_1110` (backfill từng vé). Chưa chạy migration hoặc baseline.
- `./gradlew assemble test` với ba lớp test chọn lọc đạt ngày 2026-11-12: 10 test, 0 lỗi. Đây là unit/Web MVC test dùng mock, không kết nối database.

## Đang dở

- Chờ quyền chỉ đọc và database thử nghiệm tách biệt để xác minh schema/history, baseline, backfill và các truy vấn JPA thực tế. Người dùng cho phép chuẩn bị code và file migration nhưng chưa cho phép chạy migration hoặc kết nối database thật.
- PR #24 chưa merge nên API danh sách trong nền `origin/develop` vẫn bị `@ConditionalOnProperty` tắt mặc định; cần tích hợp sau review của team.

## Việc tiếp theo

1. Khi có quyền chỉ đọc, đối chiếu schema/history thật, chốt version baseline và preflight dữ liệu cũ; trình phương án cuối để người dùng duyệt.
2. Dựng database thử nghiệm an toàn, chạy thử baseline/migration, kiểm tra backfill, truy vấn tìm kiếm và API; bổ sung mọi đường tạo vé để ghi snapshot, sau đó mới cân nhắc cột `NOT NULL`.
3. Sau khi PR phụ thuộc được review/merge, đồng bộ `origin/develop`, chạy full build/test, xử lý conflict, push và mở PR riêng; xóa file tiến độ trước PR.

## Quyết định đã chốt và lưu ý

- Chỉ chuẩn bị file migration theo sự cho phép của người dùng; không thực thi, không bật Flyway runtime, không thay đổi database thật.
- Không sửa thông tin chuyến bay, giá hoặc thanh toán ngoài phạm vi SRS.
- Quy tắc chỉ sửa trước thanh toán là tạm thời, cần BA/mentor xác nhận.
- Email snapshot của vé không thay `dat_cho.email_nguoi_dat` hoặc email đang dùng trong luồng thanh toán.

## Lỗi đang gặp

- MySQL80 đang chạy nhưng thiếu credential trong phiên này; truy vấn metadata bằng root không mật khẩu bị từ chối.
- Repository chưa có Flyway runtime hoặc migration dù tài liệu `agents-docs/be/03-database.md` mô tả Flyway.
