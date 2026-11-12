# Hợp đồng API sửa thông tin riêng của vé (Task 3)

**Trạng thái:** đã chuẩn bị và kiểm tra bằng unit/Web MVC test; chưa gọi HTTP với MySQL thật. API chỉ hoạt động sau khi schema đích được xác minh, baseline được duyệt và hai migration snapshot được áp dụng an toàn.

## Yêu cầu

`PUT /api/v1/ve-may-bay/{id}` dành cho `ADMIN` hoặc `NHAN_VIEN` đã đăng nhập. Cookie xác thực là `access_token`; yêu cầu ghi cần cookie `XSRF-TOKEN` và header `X-XSRF-TOKEN` cùng giá trị. `{id}` phải là số nguyên dương.

```json
{
  "hoTenNguoiDi": "Nguyen Van Moi",
  "emailThanhToan": "ve-moi@example.com"
}
```

Tên bắt buộc từ 10 đến 50 ký tự sau khi bỏ khoảng trắng hai đầu. Email bắt buộc, đúng định dạng và tối đa 100 ký tự. Chỉ sửa khi cả vé và đặt chỗ đều `CHO_THANH_TOAN`. Email này là snapshot của **vé**, không thay `dat_cho.email_nguoi_dat` hay email đang dùng trong luồng thanh toán.

## Phản hồi thành công dự kiến

HTTP 200, `application/json`:

```json
{
  "statusCode": 200,
  "message": "Sửa thông tin vé thành công",
  "data": {
    "id": 1,
    "maVe": "VE001",
    "hoTenNguoiDi": "Nguyen Van Moi",
    "emailThanhToan": "ve-moi@example.com",
    "trangThai": "CHO_THANH_TOAN"
  }
}
```

## Lỗi dự kiến

| HTTP | `code` | Trường hợp |
|---|---|---|
| 400 | `VALIDATION_ERROR` | ID, tên hoặc email không hợp lệ |
| 400 | `INVALID_REQUEST_BODY` | JSON sai định dạng |
| 401 | `UNAUTHORIZED` | Thiếu/không hợp lệ token |
| 403 | `ACCESS_DENIED` | Khách hàng không có quyền hoặc thiếu CSRF |
| 404 | `TICKET_NOT_FOUND` | Không tìm thấy vé |
| 409 | `TICKET_NOT_EDITABLE` | Vé hoặc đặt chỗ đã thanh toán/đã hủy |

Lỗi trả `application/problem+json` theo RFC 9457, thêm `code` và `details`. Đây là hợp đồng dự kiến từ code và Web MVC test, **chưa phải kết quả gọi HTTP thực tế**.
