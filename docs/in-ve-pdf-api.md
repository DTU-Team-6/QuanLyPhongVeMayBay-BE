# API xuất vé PDF

## Hợp đồng API

- `GET /api/v1/ve-may-bay/{id}/pdf`
- Quyền: `ADMIN` hoặc `NHAN_VIEN` qua JWT cookie `access_token` hoặc Bearer token.
- Không có request body. GET không yêu cầu header CSRF.
- Thành công: HTTP 200, `Content-Type: application/pdf`, `Content-Disposition: attachment; filename="ve-{id}.pdf"` và body là byte PDF.
- Lỗi: HTTP 400 (`INVALID_TICKET_ID`), 401 (`UNAUTHORIZED`), 403 (`ACCESS_DENIED`), 404 (`TICKET_NOT_FOUND`), 409 (`TICKET_SNAPSHOT_MISSING`). Lỗi trả `application/problem+json`.

PDF dùng tên và email snapshot trên chính vé; không in `gia_goc`. API không thay đổi vé hoặc cờ `da_in_ve`. Cần áp dụng hai migration snapshot của Task 3 trước khi chạy trên database khác; không tự baseline database hiện có.

Ví dụ gọi bằng HTTP client sau khi đăng nhập nhân viên:

```http
GET /api/v1/ve-may-bay/1/pdf HTTP/1.1
Cookie: access_token=<JWT của nhân viên>
Accept: application/pdf
```

Response 200 là file PDF, không phải JSON. Ví dụ lỗi khi không tìm thấy vé:

```json
{"status":404,"code":"TICKET_NOT_FOUND","detail":"Không tìm thấy vé.","details":[]}
```

## Kiểm thử đã thực hiện trên database tách biệt

Ngày 2026-11-12: Spring Boot 4.1.2 chạy cổng 18081 với MySQL 8.4.13 container `codex-in-ve-mysql-20261112` ở cổng localhost 13307. Database thử nghiệm được tạo mới từ bản `schema.sql` đã loại bỏ các câu `DROP TABLE`, `CREATE DATABASE`, `USE`, rồi nạp dữ liệu mẫu. Flyway CLI được baseline **chỉ trên container này** ở `20261112.1000` và áp dụng migration snapshot `1100`, `1110`. Database hiện có không bị kết nối hoặc sửa.

`gradlew build --no-daemon`: BUILD SUCCESSFUL, 125 test, 0 lỗi, 0 bỏ qua.

- Đăng nhập nhân viên bằng API thật: HTTP 200.
- Sửa snapshot vé số 1 bằng API Task 3: HTTP 200.
- GET PDF vé số 1 sau lần sửa cuối: HTTP 200, `application/pdf`, `attachment; filename="ve-1.pdf"`, file 14.346 byte, chữ ký `%PDF-`, A4 một trang. Nội dung tiếng Việt hiển thị đúng và dùng snapshot vừa sửa.
- Không đăng nhập: HTTP 401. Đăng nhập khách hàng: HTTP 403.
- Vé không tồn tại: HTTP 404. ID bằng 0: HTTP 400. Snapshot thiếu trên dữ liệu thử nghiệm: HTTP 409; đã phục hồi giá trị sau phép thử.
- Truy vấn test DB xác nhận `da_in_ve` không đổi sau GET.

Lần gọi HTTP đầu tiên trả 500 vì dữ liệu mẫu được nạp với charset sai làm tên thành phố chứa ký tự điều khiển. Đã sửa dữ liệu **chỉ trên container thử nghiệm** và thêm xử lý ký tự điều khiển trong PDF; lần gọi sau trả 200. Unit test có trường hợp này.

Font `NotoSans-Regular.ttf` được nhúng vào PDF để hỗ trợ tiếng Việt. Font lấy từ kho [Noto Fonts](https://github.com/notofonts/noto-fonts) và kèm giấy phép SIL Open Font License tại `src/main/resources/fonts/OFL.txt`.
