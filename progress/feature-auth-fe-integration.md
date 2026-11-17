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

- Không có.

## Việc tiếp theo (theo thứ tự)

1. Tạo Pull Request vào `develop` sau khi đồng bộ nhánh và chạy build theo quy trình.

## Quyết định đã chốt và lưu ý

- Trường CMND/Passport được kiểm tra bắt buộc và giới hạn 20 ký tự; quốc tịch tối đa 50 ký tự, mặc định `Việt Nam`.
- Dùng các cột `khach_hang.cmnd_passport` và `khach_hang.quoc_tich` hiện có; không sửa migration và không thêm dependency.

## Lỗi đang gặp (nếu có)

- Không có.
