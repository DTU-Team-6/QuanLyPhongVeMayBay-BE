# BE – Bảo mật và cấu trúc code

## Bảo mật

- Repo này **Public**. Không bao giờ commit mật khẩu, token, khoá PayPal/OAuth/Cloudinary, file `.env`. Giá trị bí mật chỉ đọc từ biến môi trường (`${DB_PASSWORD}`...).
- Mật khẩu người dùng băm bằng **BCrypt** (không dùng SHA-1 dù SRS có ghi).
- API không trả entity trực tiếp, dùng DTO; validate đầu vào bằng Bean Validation.
- Tách DTO cho khách và DTO cho nhân viên/admin (chi tiết ở `04-nghiep-vu-srs.md`) để chắc chắn không lộ `gia_goc`.
- Xác thực: JWT trong cookie HttpOnly, bật Secure trên HTTPS, có CSRF protection; đăng nhập Google/Facebook bằng OAuth2 do BE xử lý. Quyết định đầy đủ (tên cookie, header CSRF, endpoint, refresh token) và chi tiết cần đặc tả khi triển khai nằm ở `07-chua-chot.md`; đọc file đó trước khi viết code xác thực.
- `client_secret` của Google/Facebook chỉ nằm ở BE, đọc từ biến môi trường, không commit.
- Cloudinary `api_secret` chỉ nằm ở BE, đọc từ biến môi trường, không trả về FE. Với upload từ trình duyệt, BE ký các tham số cố định được phép; FE không được tự quyết định phạm vi/quyền của asset ngoài tham số BE đã ký. Chỉ nhận JPEG/PNG/WebP tối đa 5 MB; không tin MIME type/kích thước do client khai báo, và phải xác minh metadata asset với Cloudinary trước khi lưu liên kết vào dữ liệu nghiệp vụ.
- Email production dùng Brevo SMTP; chỉ dùng SMTP key làm mật khẩu SMTP và sender đã xác minh. Môi trường dev/test dùng Mailtrap Sandbox với credential riêng. Không ghi credential hoặc nội dung nhạy cảm trong log.

## Quy ước đặt tên (class và API)

- Class entity, DTO, service...: `PascalCase` tiếng Việt không dấu, tên theo bảng: bảng `chuyen_bay` → `ChuyenBay`, `dat_cho` → `DatCho`; kèm hậu tố theo vai trò: `ChuyenBayRepository`, `ChuyenBayService`, `ChuyenBayController`.
- URL API: tiền tố **`/api/v1`**, tên tài nguyên `kebab-case` tiếng Việt không dấu: `/api/v1/chuyen-bay`, `/api/v1/dat-cho`.
- Thời gian và múi giờ: xem `03-database.md`.

## Cấu trúc code (quy ước, tạo dần khi làm)

```
com.team6.quanlyvemaybay
├── config/        # Security, CORS, ...
├── controller/    # REST API, chỉ nhận/trả DTO
├── service/       # logic nghiệp vụ, @Transactional
├── repository/    # Spring Data JPA
├── entity/        # ánh xạ bảng
├── dto/           # request/response
└── exception/     # xử lý lỗi tập trung
```
