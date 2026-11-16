# Tiến độ nhánh codex/home-integration

## Task hiện tại

- Chức năng: hoàn thiện liên kết Trang chủ / Header và API Spring Boot (task 1).
- Nhánh: `codex/home-integration`, bắt đầu từ `origin/develop` tại `7bd0677`.
- Cập nhật: 16/11/2026, agent BE.

## Đã xong (chưa commit)

- Bổ sung `POST /api/v1/auth/logout` trả 204 và xoá cookie `access_token`, `refresh_token` với cùng Path, HttpOnly, SameSite và Secure theo cấu hình.
- Cookie access hết hạn không chặn GET dữ liệu công khai, lấy CSRF, đăng nhập lại hoặc đăng xuất. Endpoint riêng tư vẫn xác thực và thao tác POST vẫn yêu cầu CSRF.
- Đồng bộ truy vấn tìm chuyến bay với Top 10: loại `DA_HUY`, giữ `DELAY` còn ghế và chưa khởi hành để card Trang chủ tìm được cùng chuyến khi người dùng mở kết quả.
- Thêm regression tests cho stale cookie, đăng nhập lại, logout, bảo vệ CSRF, endpoint riêng tư và luồng Top 10 → tìm kiếm chuyến DELAY.
- `gradlew.bat build` sau các thay đổi: BUILD SUCCESSFUL, 32 giây.

## Đang dở

- Chờ phản biện độc lập lần cuối và kiểm tra FE/BE thực tế trước khi commit/push.

## Việc tiếp theo

1. Parent agent kiểm tra tích hợp FE và review cuối.
2. Commit riêng task 1, xoá file tiến độ trước khi mở PR theo Git flow của repo.
3. Push nhánh task 1, PR vào `develop`; sau đó mới làm task 2.

## Quyết định và lưu ý

- Không sửa schema/migration hoặc thêm dependency trong task này.
- `/auth/refresh` và refresh-token rotation chưa được triển khai trong baseline; không giả lập làm mới phiên.
- Task 2 cần sửa tổng booking bị đếm trùng ở báo cáo theo hãng và số tiền bị cộng cả hãng khác khi lọc hãng trong báo cáo theo nhân viên/thời gian.
- Task 3 cần API danh mục sân bay cho form điểm đến; cấu trúc trường theo SRS/Figma cần QA xác nhận.

## Lỗi đang gặp

- Không có lỗi build tại lần kiểm tra cuối.

- QA độc lập chấp thuận sau sửa DELAY; browser FE nối BE thật đã xác minh Top10/search và login/logout.
