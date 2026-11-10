# AGENTS.md – QuanLyPhongVeMayBay-BE

Backend của **Hệ thống quản lý phòng bán vé máy bay** (đồ án nhóm DTU-Team-6, theo tài liệu SRS v1.2).
Hãy trả lời và viết comment bằng **tiếng Việt**.

Hướng dẫn chi tiết được chia thành các file nhỏ trong `agents-docs/be/`. **Đọc file liên quan đến việc đang làm trước khi bắt đầu:**

| Khi nào | Đọc file |
|---|---|
| Luôn đọc đầu tiên: stack (đừng tự ý đổi), lệnh Gradle | [agents-docs/be/01-stack-va-lenh.md](agents-docs/be/01-stack-va-lenh.md) |
| Dựng môi trường, gặp lỗi khi chạy app | [agents-docs/be/02-dung-moi-truong.md](agents-docs/be/02-dung-moi-truong.md) |
| Đụng tới schema, entity, repository, Flyway, thời gian và múi giờ | [agents-docs/be/03-database.md](agents-docs/be/03-database.md) |
| Viết logic nghiệp vụ, API, lỗi API, báo cáo, thanh toán | [agents-docs/be/04-nghiep-vu-srs.md](agents-docs/be/04-nghiep-vu-srs.md) |
| Viết code mới (đặt tên, cấu trúc package, bảo mật, DTO) | [agents-docs/be/05-bao-mat-va-cau-truc.md](agents-docs/be/05-bao-mat-va-cau-truc.md) |
| Commit, merge, hoặc nhận task dài | [agents-docs/be/06-git-va-quy-trinh.md](agents-docs/be/06-git-va-quy-trinh.md) |
| Xác thực, cookie/CSRF, endpoint auth, OAuth2, refresh token, giờ bay, email, Cloudinary, test | [agents-docs/be/07-chua-chot.md](agents-docs/be/07-chua-chot.md) |

## Quy tắc bắt buộc (tóm tắt, chi tiết ở các file trên)

> Đây chỉ là bản tóm tắt. **Nếu khác với file chi tiết thì file chi tiết đúng.** Khi phát hiện hai chỗ khác nhau, báo người dùng và sửa lại bản tóm tắt cho khớp.

- **Trước khi code:** kiểm tra các file migration, schema hiện tại, cấu trúc project và `agents-docs/be/05-bao-mat-va-cau-truc.md`.
- Không sửa migration đã áp dụng (`V1`, `V2`...). Đổi schema thì thêm file mới đặt version theo thời gian: `V<yyyyMMdd>_<HHmm>__<mo_ta>.sql`. **Báo cáo thay đổi schema và migration dự kiến trước khi thực hiện.** `ddl-auto` luôn là `none`.
- Không tự thêm dependency vào `build.gradle` khi chưa được phê duyệt.
- `gia_goc` tuyệt đối không trả ra API dành cho khách hàng và không in lên vé; chỉ nằm trong DTO của nhân viên/admin.
- Xác thực: JWT trong cookie HttpOnly (`access_token` 15 phút, `refresh_token` 7 ngày có rotation); CSRF bằng cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN`, không tắt CSRF; `SameSite=Lax`, `Secure` trên HTTPS. Refresh token lưu **hash** trong database (bảng mới bằng migration mới), hỗ trợ rotation, revoke, phát hiện reuse.
- Đăng nhập Google/Facebook: OAuth2, BE xử lý Authorization Code Flow; FE callback là `/auth/callback`. Endpoint xác thực: xem `agents-docs/be/07-chua-chot.md`.
- API: tiền tố `/api/v1`, tên tài nguyên theo quy ước (`/api/v1/chuyen-bay`). Lỗi theo RFC 9457 + `code` + `details` (mảng `field`, `code`, `message`; không có thì `[]`); ghế đã bị chiếm trả HTTP 409 với `code` = `GHE_DA_BI_CHIEM`.
- Giờ bay: lưu UTC, ZoneId riêng cho sân bay đi và đến; `dateTime` là giờ địa phương của sân bay kèm offset, API trả thêm `timeZone` (IANA).
- Test: giữ nguyên cấu hình database test hiện có; không viết test ghi/xoá dữ liệu trên database dev; chưa triển khai Testcontainers (task riêng).
- PayPal: `secret` chỉ ở BE; xác nhận thanh toán do BE làm.
- Cloudinary: upload ảnh dùng chữ ký do BE tạo; `CLOUDINARY_API_SECRET` chỉ ở BE, FE tải file trực tiếp lên Cloudinary bằng chữ ký và các tham số được BE cho phép.
- Email: production dùng Brevo SMTP, dev/test dùng Mailtrap Email Sandbox; cấu hình credential riêng theo môi trường, xem `agents-docs/be/07-chua-chot.md`.
- Repo **Public**: không commit mật khẩu, token, khoá PayPal/OAuth/Cloudinary, `.env`.
- Không tự chạy `DROP DATABASE`, `DROP USER` hay xoá dữ liệu.
- Không đẩy thẳng lên `develop`/`main`; không tự giải conflict bằng cách xoá code của người khác.
- Task dài: liệt kê các chức năng nhỏ, để người dùng xác nhận, rồi làm từng cái một, mỗi cái một commit.

## Ghi tiến độ (để agent khác làm tiếp được)

Agent có thể bị ngắt bất cứ lúc nào (hết token, hết giới hạn) và người dùng có thể chuyển sang agent khác, vì vậy:

- **Mỗi nhánh một file tiến độ:** `progress/<ten-nhanh>.md` ở thư mục gốc repo, tên nhánh thay `/` bằng `-` (nhánh `feature/tim-chuyen-bay` → `progress/feature-tim-chuyen-bay.md`). Không dùng một file chung cho cả repo, vì sẽ conflict khi merge.
- **Đầu phiên:** đọc file tiến độ của nhánh hiện tại (nếu có), rồi chạy `git log --oneline -10` và `git status` để biết đang làm tới đâu. Làm tiếp phần "Đang dở", không làm lại phần đã xong.
- **Sau mỗi chức năng nhỏ** (cùng lúc với commit): cập nhật file tiến độ gồm việc đã xong, việc đang dở, việc tiếp theo, quyết định đã chốt. Chưa có file thì tạo từ mẫu [agents-docs/PROGRESS_TEMPLATE.md](agents-docs/PROGRESS_TEMPLATE.md).
- **Khi thấy sắp hết token hoặc người dùng nói sắp dừng:** commit tạm trên nhánh làm việc (`chore: wip ...`, không đẩy lên `develop`/`main`) và cập nhật file tiến độ ngay.
- **Trước khi mở Pull Request:** xoá file tiến độ của nhánh trong commit cuối (`git rm progress/<ten-nhanh>.md`) để không đưa vào `develop`.
- Chỉ ghi những gì đã thật sự xảy ra (kết quả build thật, lỗi nguyên văn), không ghi phỏng đoán.
- Đừng cùng lúc hai agent làm trên một nhánh. Nếu thấy `git status` có thay đổi lạ chưa commit, hỏi người dùng trước khi động vào.
