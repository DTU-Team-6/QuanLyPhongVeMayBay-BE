# BE – Quyết định kỹ thuật đã chốt và mục chưa chốt

> Phần "đã chốt" là quyết định của nhóm, agent làm theo, không tự đổi.
> Phần "chưa chốt" là chỗ nhóm **chưa quyết định**: agent **không tự chọn**, phải hỏi người dùng trước khi viết code liên quan, nên đưa 2–3 lựa chọn kèm ưu nhược điểm ngắn gọn.
> Khi nhóm chốt thêm, ghi vào file này và **báo người dùng cập nhật cùng nội dung ở `agents-docs/fe/02-ket-noi-backend.md`** (phía FE, có thể nằm ở repo khác).

## Đã chốt: tổng quan

| Mục                       | Quyết định                                                                          |
| ------------------------- | ----------------------------------------------------------------------------------- |
| Xác thực                  | **JWT**                                                                             |
| Lưu token                 | Cookie **HttpOnly**, bật **Secure trên HTTPS**, có **CSRF protection** (chi tiết bên dưới) |
| Thời hạn token            | Access token **15 phút**; refresh token **7 ngày**, có **rotation**                 |
| Đăng nhập Google/Facebook | **OAuth2**, BE xử lý bằng **Authorization Code Flow**                               |
| Thư viện                  | **Spring Security**, **OAuth2 Client**, **OAuth2 Resource Server**                  |
| Tiền tố API               | **`/api/v1`**, giữ nguyên quy ước tên tài nguyên (ví dụ `/api/v1/chuyen-bay`)       |
| JSON báo lỗi              | **RFC 9457** + `code` + `details` (chi tiết bên dưới)                               |
| Lỗi ghế bị chiếm          | HTTP 409 + `code` = `GHE_DA_BI_CHIEM` (giữ nguyên)                                  |
| Giờ bay                   | Lưu **UTC**, kèm **ZoneId** riêng cho sân bay đi và sân bay đến (chi tiết bên dưới) |
| Refresh token             | Lưu **hash** trong database, có rotation, revoke, phát hiện reuse                   |
| Database test             | Giữ nguyên cấu hình hiện có; Testcontainers là task riêng, **chưa triển khai**      |

## Đã chốt: cookie và CSRF

| Cookie / header | Tên             | Ghi chú                                          |
| --------------- | --------------- | ------------------------------------------------ |
| Access token    | `access_token`  | `HttpOnly`; `Secure` trên HTTPS                  |
| Refresh token   | `refresh_token` | `HttpOnly`; `Secure` trên HTTPS                  |
| CSRF cookie     | `XSRF-TOKEN`    | **Không** có `HttpOnly` (để FE đọc được)         |
| CSRF header     | `X-XSRF-TOKEN`  | FE gửi giá trị lấy từ cookie `XSRF-TOKEN`        |

- `SameSite=Lax` khi FE và BE cùng site. Nếu sau này FE và BE khác site, hỏi người dùng trước (không tự đổi `SameSite`).
- `Secure` bật trên môi trường HTTPS (dev chạy HTTP thì không bật).
- **Không tắt CSRF protection.**

## Đã chốt: endpoint xác thực

| Method | Đường dẫn                        | Mục đích                                             |
| ------ | -------------------------------- | ---------------------------------------------------- |
| POST   | `/api/v1/auth/login`             | Đăng nhập                                            |
| POST   | `/api/v1/auth/register`          | Đăng ký                                              |
| GET    | `/api/v1/auth/me`                | Lấy thông tin người dùng hiện tại                   |
| POST   | `/api/v1/auth/refresh`           | Làm mới token (có rotation)                          |
| POST   | `/api/v1/auth/logout`            | Đăng xuất                                            |
| GET    | `/oauth2/authorization/google`   | Bắt đầu đăng nhập Google (FE chuyển hướng tới đây)   |
| GET    | `/oauth2/authorization/facebook` | Bắt đầu đăng nhập Facebook (FE chuyển hướng tới đây) |
| GET    | `/login/oauth2/code/google`      | Google gọi về BE sau khi người dùng đồng ý           |
| GET    | `/login/oauth2/code/facebook`    | Facebook gọi về BE sau khi người dùng đồng ý         |

- Các đường dẫn `/oauth2/...` và `/login/oauth2/...` **không** nằm dưới `/api/v1`.
- Trang callback ở phía FE: **`/auth/callback`** (BE chuyển hướng người dùng về đây sau khi đăng nhập OAuth2).

## Đã chốt: JSON báo lỗi

Theo **RFC 9457** (`application/problem+json`, gồm `type`, `title`, `status`, `detail`, `instance`), kết hợp thêm:

- `code`: mã lỗi nghiệp vụ (chuỗi cố định, ví dụ `GHE_DA_BI_CHIEM`). FE dựa vào `code`, không dựa vào nội dung `detail`.
- `details`: **mảng** các phần tử, mỗi phần tử gồm `field`, `code`, `message`. Nếu không có lỗi chi tiết thì trả `details: []`.
- Lỗi ghế đã bị chiếm giữ nguyên: HTTP **409** + `code` = `GHE_DA_BI_CHIEM`.

## Đã chốt: giờ bay

- BE lưu thời điểm chuẩn **UTC**.
- Lưu **ZoneId riêng** cho sân bay đi và sân bay đến. Nơi lưu (cột ở bảng sân bay hay bảng chuyến bay) quyết định sau khi kiểm tra schema hiện tại.
- API trả mỗi mốc giờ gồm `dateTime` (ISO 8601 **có offset**) và `timeZone` (dạng IANA ZoneId, ví dụ `Asia/Ho_Chi_Minh`).
- `dateTime` biểu diễn **giờ địa phương của sân bay tương ứng kèm offset**; ví dụ `2026-12-01T08:00:00+07:00` cùng `timeZone: "Asia/Ho_Chi_Minh"`. Offset xác định thời điểm tuyệt đối; FE hiển thị theo `timeZone`, không theo múi giờ máy người dùng.
- Không sửa migration `V1`. Nếu cần đổi schema, tạo migration mới sau khi kiểm tra schema hiện tại (xem `03-database.md`).

## Đã chốt: refresh token

- Lưu **hash** của refresh token trong database; không lưu token gốc.
- Phải có **migration mới** cho bảng quản lý refresh token.
- Hỗ trợ **rotation**, **revoke** và **phát hiện reuse**.

## Đã chốt: testing

- Hiện tại giữ nguyên cấu hình database test đang có.
- Không viết test ghi hoặc xoá dữ liệu trên database dev.
- Chưa triển khai Testcontainers; đây là task riêng, chỉ làm khi được yêu cầu. Lúc đó máy cần Docker (xem `02-dung-moi-truong.md`) và nên dùng cùng phiên bản MySQL với dev (9.7.4); kiểm tra image có tag đó trước khi dùng.

## Đã chốt: quy tắc thực hiện

- **Trước khi code:** kiểm tra các file migration, schema hiện tại, cấu trúc project và `05-bao-mat-va-cau-truc.md`.
- Không sửa migration đã áp dụng.
- Không tự thêm dependency vào `build.gradle` khi chưa được phê duyệt (kể cả các thư viện Spring Security đã nêu ở trên).
- **Báo cáo các thay đổi schema và migration dự kiến trước khi thực hiện**, không tự tạo migration rồi mới báo.

## Đã chốt: Endpoint lấy người dùng hiện tại

- `GET /api/v1/auth/me` trả `id`, họ tên, email và vai trò.
- Nếu chưa đăng nhập hoặc token hết hạn thì trả 401. FE gọi `/api/v1/auth/refresh` một lần rồi gọi lại; vẫn 401 thì đưa người dùng về trang đăng nhập.

## Đã chốt: Khoá JWT

- khoá đối xứng (HS256), chuỗi ngẫu nhiên dài ít nhất 32 byte, đọc từ biến JWT_SECRET, mỗi môi trường một khoá.

## Đã chốt: Tiền tệ Paypal

- Giữ giá VND trong hệ thống; khi thanh toán quy đổi sang USD theo tỷ giá cố định đặt trong cấu hình và hiển thị cả hai số tiền trước khi trả.
- Lưu tỷ giá và số tiền USD vào bản ghi thanh_toan (cần cột mới, tức migration mới).

## Đã chốt: Tên biến môi trường

- DB_PASSWORD, JWT_SECRET, GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET, FACEBOOK_CLIENT_ID/FACEBOOK_CLIENT_SECRET, PAYPAL_CLIENT_ID/PAYPAL_CLIENT_SECRET, PAYPAL_MODE (sandbox hoặc live), CLOUDINARY_CLOUD_NAME/CLOUDINARY_API_KEY/CLOUDINARY_API_SECRET, MAIL_HOST/MAIL_PORT/MAIL_USERNAME/MAIL_PASSWORD/MAIL_FROM, APP_FRONTEND_URL (dùng cho CORS và chuyển hướng về /auth/callback).

## Đã chốt: Gửi email

- Dùng **Brevo SMTP relay** cho production; môi trường phát triển/kiểm thử dùng **Mailtrap Email Sandbox** để không gửi thư thử tới người nhận thật.
- Production dùng `MAIL_HOST=smtp-relay.brevo.com`, `MAIL_PORT=587` với STARTTLS. `MAIL_USERNAME` là SMTP login do Brevo cấp; `MAIL_PASSWORD` là SMTP key, **không phải API key**. Địa chỉ `MAIL_FROM` phải là sender/domain đã xác minh trong Brevo.
- Mỗi môi trường đặt bộ biến `MAIL_*` riêng. Không lưu thông tin xác thực trong repo, log, hoặc đưa thông tin production vào môi trường dev/test.
- Các cổng và thông tin xác thực Mailtrap lấy từ cấu hình Email Sandbox của project; dùng TLS theo cấu hình Mailtrap hiện hành, không tái sử dụng credential Brevo production.

## Đã chốt: Upload ảnh Cloudinary

- Dùng signed direct upload: FE xin chữ ký upload từ BE; BE tạo chữ ký bằng `CLOUDINARY_API_SECRET`; FE dùng chữ ký để tải file trực tiếp từ trình duyệt lên Cloudinary.
- Không gửi API secret về FE, không đặt secret trong biến `VITE_*` và không commit thông tin xác thực.
- Chỉ cho phép ảnh JPEG, PNG, WebP; kích thước tối đa 5 MB mỗi ảnh. FE kiểm tra trước để phản hồi sớm; BE chỉ ký các tham số cố định theo nghiệp vụ và quyền người dùng. Cấu hình upload phía Cloudinary phải áp giới hạn định dạng/kích thước tương ứng; không tin MIME type hoặc kích thước do trình duyệt tự khai báo.
- BE xác minh kết quả/metadata asset với Cloudinary, gồm định dạng và kích thước, trước khi gắn ảnh vào bản ghi nghiệp vụ. Tên endpoint, request/response và quy trình gắn asset được xác định khi triển khai chức năng.

## Lưu ý khi triển khai

- **Cookie HttpOnly:** JavaScript ở FE không đọc được token. FE phải gửi kèm cookie khi gọi API, và BE phải cấu hình CORS cho phép credentials với đúng origin của FE (không dùng `*`).
- Kiểm tra schema thật trước khi làm phần giờ bay: `V1` có thể chưa có cột múi giờ hoặc đang dùng kiểu cột thời gian khác.

## Chi tiết cần đặc tả khi triển khai

Hiện chưa ghi nhận lựa chọn kỹ thuật cấp nhóm nào còn chờ quyết định. Khi triển khai từng tính năng, cần xác định hợp đồng API và chi tiết tích hợp chưa được đặc tả trong các mục đã chốt; không tự suy diễn schema hoặc API ngoài phạm vi đã thống nhất.
