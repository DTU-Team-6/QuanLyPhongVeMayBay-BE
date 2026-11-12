# Hợp đồng API sửa thông tin riêng của vé (Task 3)

**Trạng thái:** đã gọi HTTP thật với MySQL 8.4.13 trong container Docker tách biệt. Database hiện có của máy/nhóm chưa được truy cập hoặc thay đổi. Trên môi trường khác, API chỉ hoạt động sau khi schema đích được xác minh, baseline được duyệt và hai migration snapshot được áp dụng an toàn.

## Yêu cầu

`PUT /api/v1/ve-may-bay/{id}` dành cho `ADMIN` hoặc `NHAN_VIEN` đã đăng nhập. Cookie xác thực là `access_token`; yêu cầu ghi cần cookie `XSRF-TOKEN` và header `X-XSRF-TOKEN` cùng giá trị. `{id}` phải là số nguyên dương.

Khi thử bằng Postman: gọi `GET /api/v1/auth/csrf`, đăng nhập, sau đó gọi lại endpoint CSRF trước lệnh `PUT`. Trong lần thử này cookie CSRF bị xóa sau yêu cầu ghi đã xác thực, nên cần lấy lại trước lệnh ghi tiếp theo. Không tắt CSRF.

```json
{
  "hoTenNguoiDi": "Tran Thi Lan",
  "emailThanhToan": "ve-hai@example.com"
}
```

Tên bắt buộc từ 10 đến 50 ký tự sau khi bỏ khoảng trắng hai đầu. Email bắt buộc, đúng định dạng và tối đa 100 ký tự. Chỉ sửa khi cả vé và đặt chỗ đều `CHO_THANH_TOAN`. Email này là snapshot của **vé**, không thay `dat_cho.email_nguoi_dat` hay email đang dùng trong luồng thanh toán.

## Phản hồi thành công đã quan sát

HTTP 200, `application/json`:

```json
{
  "statusCode": 200,
  "message": "Sửa thông tin vé thành công",
  "data": {
    "id": 2,
    "maVe": "HCM001",
    "hoTenNguoiDi": "Tran Thi Lan",
    "emailThanhToan": "ve-hai@example.com",
    "trangThai": "CHO_THANH_TOAN"
  }
}
```

## Lỗi đã gọi HTTP

| HTTP | `code` | Trường hợp |
|---|---|---|
| 400 | `VALIDATION_ERROR` | ID, tên hoặc email không hợp lệ |
| 400 | `INVALID_REQUEST_BODY` | JSON sai định dạng |
| 401 | `UNAUTHORIZED` | Thiếu/không hợp lệ token |
| 403 | `ACCESS_DENIED` | Khách hàng không có quyền hoặc thiếu CSRF |
| 404 | `TICKET_NOT_FOUND` | Không tìm thấy vé |
| 409 | `TICKET_NOT_EDITABLE` | Vé hoặc đặt chỗ đã thanh toán/đã hủy |

Lỗi trả `application/problem+json` theo RFC 9457, thêm `code` và `details`. Các mã HTTP trên đã được gọi bằng HTTP thật trong container test; ca 409 thực tế gồm vé đã thanh toán và đặt chỗ đã thanh toán, còn trạng thái đã hủy được kiểm tra bằng unit test. Với lỗi email sai, `details[0]` có `field = emailThanhToan` và thông báo tiếng Việt.

## Bằng chứng test database tách biệt

- Chỉ trên container tạm `codex-sua-ve-mysql-20261112`, MySQL 8.4.13, database `ticket_task3_test`: dựng 19 bảng từ bản sao `schema.sql` đã bỏ `CREATE DATABASE`, `USE`, `DROP TABLE` và lệnh tắt khóa ngoại; nạp dữ liệu mẫu, không chạy `schema.sql` gốc.
- Flyway OSS 12.4.0 chạy `baseline` **tường minh** ở version `20261112.1000`, rồi áp dụng `20261112.1100` và `20261112.1110`; 4 vé được backfill, 0 snapshot null và 0 giá trị lệch nguồn.
- Spring Boot khởi động trên cổng 18080, trỏ đến container qua `127.0.0.1:13306`. API đăng nhập thật trả 200 và cookie `access_token`; `PUT /api/v1/ve-may-bay/2` trả 200 với JSON phía trên.
- Truy vấn sau PUT cho thấy vé 1 và 2 cùng `hanh_khach_id = 1`, `dat_cho_id = 1` nhưng có tên/email snapshot khác nhau. `hanh_khach.ho_ten` và `dat_cho.email_nguoi_dat` không đổi.
- `./gradlew build`: 121 test, 0 lỗi. Kết quả này dùng database container riêng; chưa kiểm thử database dùng chung của nhóm.
