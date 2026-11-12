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
- Đã fetch `origin/develop` tới `bf3b30c` (bao gồm PR #11 thanh toán), giải conflict ở `VeMayBayRepository` bằng cách giữ cả `findByDatChoId` và truy vấn snapshot. Sau đó `./gradlew build -x test` đạt; chạy lại 3 lớp test chọn lọc: 10 test, 0 lỗi. Chưa chạy `@SpringBootTest`/full test vì chưa có database thử nghiệm an toàn.
- Bổ sung các ca Web MVC 400/401/403/404/409 và JSON lỗi cho API sửa vé; chạy lại 3 lớp test chọn lọc: 16 test, 0 lỗi. Đã viết hợp đồng request/response dự kiến tại `docs/sua-ve-api.md` để dùng khi kiểm thử Postman.
- Sửa validation ID trực tiếp trong controller để lỗi tên người đi trả `details[0].field = hoTenNguoiDi` thay vì `request`; chạy lại `build -x test` và 16 test chọn lọc đều đạt.
- Dựng MySQL 8.4.13 container tạm, dựng 19 bảng từ bản sao `schema.sql` đã bỏ lệnh nguy hiểm; nạp 4 vé mẫu. Flyway OSS 12.4.0 baseline tường minh `20261112.1000`, áp dụng hai migration snapshot; 0 null, 0 lệch nguồn.
- Spring Boot khởi động thật trên cổng 18080 và kết nối riêng container test. Đăng nhập nhân viên test qua API trả 200, `PUT /api/v1/ve-may-bay/2` trả 200. Truy vấn database xác nhận chỉ vé 2 đổi tên/email; vé 1 cùng hành khách/đặt chỗ, thông tin hành khách và email đặt chỗ không đổi.
- HTTP thực tế: 400 (tên/email/ID sai), 401 (chưa xác thực), 403 (khách hàng/thiếu CSRF), 404 (không có vé), 409 (vé hoặc đặt chỗ đã thanh toán). `./gradlew build` trỏ container test đạt 121 test, 0 lỗi.

## Đang dở

- Database hiện có của máy/nhóm vẫn chưa được kiểm tra và chưa được thay đổi; baseline trên môi trường dùng chung phải được duyệt sau khi đối chiếu schema/history thực tế.
- PR #24 vẫn chưa merge; cần review ảnh hưởng đến API danh sách vé. Cookie CSRF bị xóa sau yêu cầu ghi đã xác thực trong lần thử này, nên Postman cần lấy lại qua `GET /api/v1/auth/csrf` trước lệnh ghi tiếp theo.
- PR #24 chưa merge nên API danh sách trong nền `origin/develop` vẫn bị `@ConditionalOnProperty` tắt mặc định; API PUT của Task 3 đã chạy độc lập.

## Việc tiếp theo

1. Commit kết quả test, fetch và kiểm tra `origin/develop` lần cuối; xóa file tiến độ trước PR, push nhánh và mở PR vào `develop` với lưu ý môi trường dùng chung chưa được baseline.
2. Khi có quyền chỉ đọc trên database dùng chung, đối chiếu schema/history thật và trình baseline/backup để người dùng duyệt trước khi triển khai ở đó.
3. Khi PR #24 được review/merge, đối chiếu lại danh sách/tìm kiếm tên snapshot. Mọi đường tạo vé về sau phải gán snapshot trước khi cân nhắc `NOT NULL`.

## Quyết định đã chốt và lưu ý

- Đã thực thi migration chỉ trên container test được người dùng cho phép; không bật Flyway runtime của ứng dụng, không thay đổi database hiện có.
- Không sửa thông tin chuyến bay, giá hoặc thanh toán ngoài phạm vi SRS.
- Quy tắc chỉ sửa trước thanh toán là tạm thời, cần BA/mentor xác nhận.
- Email snapshot của vé không thay `dat_cho.email_nguoi_dat` hoặc email đang dùng trong luồng thanh toán.

## Lỗi đang gặp

- MySQL80 đang chạy nhưng thiếu credential trong phiên này; không truy cập database hiện có.
- Repository đã có hai file migration của Task 3 nhưng chưa có Flyway runtime; áp dụng trên container test bằng Flyway CLI. Tài liệu `agents-docs/be/03-database.md` mô tả V1/V2 không có trong cây nguồn này.
