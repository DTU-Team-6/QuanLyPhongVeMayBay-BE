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

## Đang dở (chưa commit hoặc chưa chạy được)

- Đang triển khai OAuth Google/Facebook: đã tạo migration, logic tài khoản và luồng OAuth Client; chưa áp dụng migration hoặc kiểm tra với nhà cung cấp thật.
- Các test `OAuthAccountServiceTest`, `OAuthLoginHandlerTest`, `OAuthSecurityTest`, `AuthSecurityTest` và `./gradlew build -x test` thành công.
- Người dùng đã áp dụng migration vào database kiểm thử và khởi động lại BE. Kiểm tra API thật: cookie phiên/có thời hạn, rotation, đăng xuất, dùng lại token và hai refresh đồng thời đều đạt; dùng lại token thu hồi token mới.
- Đã tạo một tài khoản giả trong database kiểm thử; không xoá dữ liệu. Chưa chờ hết hạn thực tế 7 ngày.
- Sửa tài liệu database cho khớp cấu hình thực tế: repo chưa dùng Flyway và không tự chạy SQL khởi tạo.
- Chưa xác minh tích hợp FE/BE.

## Việc tiếp theo (theo thứ tự)

1. Kiểm tra tích hợp FE/BE trên giao diện và đồng bộ nhiều tab.
2. Tạo Pull Request vào `develop` sau khi đồng bộ nhánh và chạy build theo quy trình.

## Quyết định đã chốt và lưu ý

- Trường CMND/Passport được kiểm tra bắt buộc và giới hạn 20 ký tự; quốc tịch tối đa 50 ký tự, mặc định `Việt Nam`.
- Dùng các cột `khach_hang.cmnd_passport` và `khach_hang.quoc_tich` hiện có; không sửa migration và không thêm dependency.
- OAuth: không tự liên kết khi email trùng; tài khoản mới tạo khách hàng bằng dữ liệu nhà cung cấp, quốc tịch mặc định `Việt Nam`, chưa kiểm tra tuổi nếu thiếu ngày sinh.
- OAuth sử dụng cookie phiên, chưa hỗ trợ lựa chọn ghi nhớ; cần bật `APP_OAUTH2_ENABLED` và cấu hình thông tin nhà cung cấp ở BE.

## Lỗi đang gặp (nếu có)

- Các test `PhienLamMoiServiceTest`, `AuthServiceTest`, `AuthControllerTest`, `AuthSecurityTest` thành công; `./gradlew build -x test` thành công. Chưa chạy toàn bộ test.
