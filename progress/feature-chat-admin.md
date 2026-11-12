# Tiến độ nhánh feature/chat-admin

## Task hiện tại

- Chức năng: Chat giữa khách hàng và Admin (99.3)
- Nhánh: `feature/chat-admin`
- Cập nhật: 2026-11-12

## Đã xong

- Nhánh được tạo từ `origin/develop` tại `2730419`.
- Đã kiểm tra `tin_nhan_chat` trong `schema.sql`; không cần migration hoặc dependency.
- REST API khách gửi/xem lịch sử và Admin xem danh sách/xem lịch sử/trả lời đã chạy HTTP thực tế.
- MySQL test xác nhận tin được lưu, lịch sử khách khác không lộ chéo.
- `gradlew build --no-daemon --rerun-tasks` thành công: 125 test, 0 lỗi, 0 bỏ qua.

## Đang dở

- Cần commit và hoàn thành Git Flow.

## Việc tiếp theo

1. Commit, fetch và merge develop; kiểm thử lại.
2. Push và tạo PR hoặc cung cấp link.

## Quyết định đã chốt và lưu ý

- Chỉ làm Backend REST API; không làm Frontend, WebSocket hoặc SSE.
- Khách chỉ được xem hội thoại của mình; Admin có thể xem và trả lời theo khách hàng.

## Lỗi đang gặp

- Chưa có.
