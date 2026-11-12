# Tiến độ nhánh feature/xoa-ve

## Task hiện tại

- Chức năng: 6.6.1.10 xóa vé.
- Nhánh: `feature/xoa-ve` từ `origin/develop`.
- Cập nhật lần cuối: 2026-11-12, Codex.

## Đã xong

- Đọc SRS v1.2: nhân viên chỉ xóa vé chưa thanh toán.
- Kiểm tra schema: vé có trạng thái `DA_HUY`, không có trạng thái đã sử dụng; vé liên kết đặt chỗ, hành khách, dịch vụ và thanh toán.
- API `DELETE /api/v1/ve-may-bay/{id}` hủy mềm vé chưa thanh toán, khóa hàng vé khi xử lý, giữ nguyên dữ liệu liên quan.
- Bổ sung test service/controller và tài liệu; `build` thành công qua ổ đĩa ánh xạ ASCII: 112 test, 0 lỗi.

## Đang dở

- Chưa kiểm tra API với dữ liệu vé trên ứng dụng chạy thực tế.

## Việc tiếp theo

1. Commit, đồng bộ develop, push và tạo PR.
2. Xóa file tiến độ trước PR.

## Quyết định đã chốt và lưu ý

- Không xóa cứng vé. Chỉ chuyển vé chưa thanh toán sang `DA_HUY`.
- Không sửa trạng thái đặt chỗ hoặc snapshot giá vì SRS chưa định nghĩa cách cập nhật khi hủy một vé trong đặt chỗ nhiều vé.

## Lỗi đang gặp

- Chưa có.
