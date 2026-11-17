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

## Đang dở (chưa commit hoặc chưa chạy được)

- Đã triển khai refresh token, rotation, phát hiện dùng lại và thu hồi; migration mới cần được áp dụng thủ công trước khi kiểm tra với database thật.
- Chưa xác minh tích hợp FE/BE.

## Việc tiếp theo (theo thứ tự)

1. Áp dụng migration vào database kiểm thử đã được xác nhận, sau đó kiểm tra tích hợp FE/BE.
2. Tạo Pull Request vào `develop` sau khi đồng bộ nhánh và chạy build theo quy trình.

## Quyết định đã chốt và lưu ý

- Trường CMND/Passport được kiểm tra bắt buộc và giới hạn 20 ký tự; quốc tịch tối đa 50 ký tự, mặc định `Việt Nam`.
- Dùng các cột `khach_hang.cmnd_passport` và `khach_hang.quoc_tich` hiện có; không sửa migration và không thêm dependency.

## Lỗi đang gặp (nếu có)

- Các test `PhienLamMoiServiceTest`, `AuthServiceTest`, `AuthControllerTest`, `AuthSecurityTest` thành công; `./gradlew build -x test` thành công. Chưa chạy toàn bộ test.
