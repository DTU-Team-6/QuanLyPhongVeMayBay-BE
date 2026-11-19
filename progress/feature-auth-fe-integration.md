# Tiến độ nhánh kết nối đăng ký/đăng nhập FE

## Task hiện tại

- Chức năng: Bổ sung dữ liệu hồ sơ khách hàng cho API đăng ký để FE gửi đầy đủ biểu mẫu
- Nhánh: `feature/auth-fe-integration`
- Cập nhật lần cuối: 2026-11-17

## Đã xong (đã commit)

- Bổ sung `cmndPassport` và `quocTich` vào `RegisterRequest` và lưu vào hồ sơ `KhachHang`.
- Cập nhật tài liệu endpoint CSRF và hợp đồng request đăng ký.
- Kiểm tra schema hiện có; không cần migration.
- `./gradlew test --tests 'com.team6.quanlyvemaybay.service.AuthServiceTest' --tests 'com.team6.quanlyvemaybay.controller.AuthControllerTest'` thành công.
- Triển khai refresh token, rotation, phát hiện dùng lại và thu hồi trong commit `ddde9f1`.
- Mở rộng `GET/PUT /auth/me` để đọc và sửa quốc tịch, CMND/hộ chiếu của khách hàng; không cần migration. Các trường mới bị bỏ qua hoặc `null` được giữ nguyên.
- Test chọn lọc `AuthServiceTest`, `AuthControllerTest`, `AuthSecurityTest` và `./gradlew build -x test` thành công.

## Đang dở (chưa commit hoặc chưa chạy được)

- Người dùng chọn khôi phục mật khẩu bằng liên kết dùng một lần, hết hạn sau 15 phút; chỉ áp dụng cho tài khoản đã có mật khẩu. Email dùng Mailtrap Sandbox khi dev/test và Brevo SMTP khi production; đã đồng ý thêm dependency Spring Boot Mail.
- Người dùng kiểm tra database kiểm thử và xác nhận chưa có bảng `xac_thuc_token`. Đã tạo migration `V20261119_0817__them_bang_xac_thuc_token.sql` theo schema hiện có; `./gradlew build -x test` thành công. Migration chưa được chạy hoặc kiểm thử trên database. Các API, email và giao diện khôi phục chưa triển khai.
- Đã thêm API ký upload avatar và xác minh/lưu ảnh Cloudinary; migration `V20261118_1246__them_public_id_avatar.sql` chưa áp dụng. Test chọn lọc service/controller/security cho avatar đạt; chưa kiểm thử Cloudinary thật.
- Sửa `CloudinaryAvatarClient` tự tạo `RestClient`, không phụ thuộc bean `RestClient.Builder` bị thiếu khi khởi động. Cần chạy lại `bootRun` trong terminal có cấu hình `JWT_SECRET` và database.
- Chưa kiểm tra tích hợp chỉnh sửa hồ sơ với FE thật.
- Người dùng xác nhận đăng nhập Google và Facebook thật thành công trên môi trường phát triển.
- Người dùng xác nhận database kiểm thử `ve_may_bay_dong_bo_test` có bảng `dinh_danh_oauth` với cấu trúc khớp migration. Lệnh chạy migration trả lỗi bảng đã tồn tại; không sửa hay xóa bảng.
- Các test `OAuthAccountServiceTest`, `OAuthLoginHandlerTest`, `OAuthSecurityTest`, `AuthSecurityTest` và `./gradlew build -x test` thành công.
- Người dùng đã áp dụng migration vào database kiểm thử và khởi động lại BE. Kiểm tra API thật: cookie phiên/có thời hạn, rotation, đăng xuất, dùng lại token và hai refresh đồng thời đều đạt; dùng lại token thu hồi token mới.
- Đã tạo một tài khoản giả trong database kiểm thử; không xoá dữ liệu. Chưa chờ hết hạn thực tế 7 ngày.
- Sửa tài liệu database cho khớp cấu hình thực tế: repo chưa dùng Flyway và không tự chạy SQL khởi tạo.
- Đã bổ sung hướng dẫn agent BE cấu hình môi trường và database kiểm thử cho các chức năng tài khoản FE.
- Chưa xác minh tích hợp FE/BE.

## Việc tiếp theo (theo thứ tự)

1. Kiểm tra tích hợp FE/BE trên giao diện và đồng bộ nhiều tab.
2. Áp dụng migration avatar và cấu hình signed upload preset Cloudinary trên môi trường kiểm thử, sau đó thử upload thật.
3. Tạo Pull Request vào `develop` sau khi đồng bộ nhánh và chạy build theo quy trình.

## Quyết định đã chốt và lưu ý

- Trường CMND/Passport được kiểm tra bắt buộc và giới hạn 20 ký tự; quốc tịch tối đa 50 ký tự, mặc định `Việt Nam`.
- Dùng các cột `khach_hang.cmnd_passport` và `khach_hang.quoc_tich` hiện có; không sửa migration và không thêm dependency.
- OAuth: không tự liên kết khi email trùng; tài khoản mới tạo khách hàng bằng dữ liệu nhà cung cấp, quốc tịch mặc định `Việt Nam`, chưa kiểm tra tuổi nếu thiếu ngày sinh.
- OAuth sử dụng cookie phiên, chưa hỗ trợ lựa chọn ghi nhớ; cần bật `APP_OAUTH2_ENABLED` và cấu hình thông tin nhà cung cấp ở BE.
- Với Facebook ở chế độ phát triển, Meta tự cho phép callback localhost; người dùng đã thêm quyền `email` trong cấu hình use case để đăng nhập thử.
- Database kiểm thử đã có bảng `lien_ket_oauth` cũ tham chiếu `tai_khoan`; người dùng đồng ý tạo bảng mới `dinh_danh_oauth` tham chiếu `nguoi_dung`, không sửa bảng cũ.

## Lỗi đang gặp (nếu có)

- Các test `PhienLamMoiServiceTest`, `AuthServiceTest`, `AuthControllerTest`, `AuthSecurityTest` thành công; `./gradlew build -x test` thành công. Chưa chạy toàn bộ test.
