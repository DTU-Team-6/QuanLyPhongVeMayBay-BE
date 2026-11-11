# Tiến độ nhánh feature/api-danh-sach-don-ve-va-huy-ve

## Task hiện tại

- Chức năng: Viết API lấy danh sách đơn vé đã đặt / vé chờ thanh toán, chi tiết đơn vé và API hủy vé (hỗ trợ màn hình Thanh toán vé & Lịch sử giao dịch)
- Nhánh: `feature/api-danh-sach-don-ve-va-huy-ve`
- Cập nhật lần cuối: 2026-11-11 12:05, Antigravity

## Đã xong (đã commit)

- Nhánh được rẽ từ `feature/api-thanh-toan-vnpay-paypal` đã tích hợp đầy đủ model đặt chỗ, thanh toán VNPay/PayPal và lịch sử giao dịch.

## Đang dở (chưa commit hoặc chưa chạy được)

- Thiết kế DTO và API:
  1. `GET /api/v1/dat-cho/cho-thanh-toan`: Lấy danh sách các vé đang chờ thanh toán (mã vé, nơi đi, nơi đến, ngày đặt, ngày bay, giá tiền, hạn giữ chỗ).
  2. `GET /api/v1/dat-cho/{maDatCho}`: Chi tiết đơn đặt vé (hành khách, vé máy bay, trạng thái).
  3. `POST /api/v1/dat-cho/huy`: Hủy đơn đặt chỗ/vé chờ thanh toán, hoàn trả ghế trống (`HangVeChuyenBay`).

## Việc tiếp theo (theo thứ tự)

1. Tạo các DTO: `PendingTicketDto`, `BookingDetailDto`, `CancelBookingRequest`, `CancelBookingResponse`.
2. Bổ sung các query method vào `DatChoRepository`, `VeMayBayRepository`.
3. Viết logic nghiệp vụ trong `DatChoService`: lấy danh sách vé chờ thanh toán, xem chi tiết và hủy đặt chỗ (hoàn ghế).
4. Viết controller endpoints trong `DatChoController`.
5. Viết script kiểm thử thực tế và chạy test với MariaDB.
6. Build và commit.

## Quyết định đã chốt và lưu ý

- Khớp 100% giao diện "Thanh toán vé" trong ảnh người dùng cung cấp (mã đặt chỗ, hạn giữ chỗ, nút Hủy vé).
- Khi hủy vé: chỉ cho phép hủy khi trạng thái là `CHO_THANH_TOAN`, hoàn lại số ghế trống cho chuyến bay (`so_ghe_trong += 1`), cập nhật trạng thái `DA_HUY`.
- DTO không để lộ `gia_goc` ra ngoài cho khách hàng (tuân thủ quy định SRS).

## Lỗi đang gặp (nếu có)

- Chưa có lỗi.
