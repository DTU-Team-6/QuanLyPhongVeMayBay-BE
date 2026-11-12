# Tiến độ nhánh feature/in-ve-pdf

## Task hiện tại

- Chức năng: In vé PDF (SRS 6.6.1.12)
- Nhánh: `feature/in-ve-pdf`
- Cập nhật: 2026-11-12

## Đã xong

- API GET PDF đã tạo; dùng snapshot riêng của vé, font Unicode, không in giá gốc.
- `gradlew build --no-daemon` trên code cuối thành công: 125 test, 0 lỗi, 0 bỏ qua.
- HTTP trên MySQL test riêng: đăng nhập nhân viên 200, tải PDF 200, không đăng nhập 401, khách hàng 403, không có vé 404, ID 0 trả 400, thiếu snapshot 409.
- PDF cuối 14.346 byte, A4 một trang, đã render và kiểm tra tiếng Việt.

## Đang dở

- Cần commit và hoàn thành Git Flow.

## Việc tiếp theo

1. Commit, fetch và merge develop; kiểm thử lại.
2. Push và tạo PR hoặc cung cấp link tạo PR.

## Quyết định đã chốt và lưu ý

- PR #27 của Task 3 đã merge vào develop ở `2730419`.
- Không thay đổi schema, không in `gia_goc`, không sửa cờ `da_in_ve` khi tải file.

## Lỗi đang gặp

- Chưa có.
