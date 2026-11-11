# Tiến độ nhánh feature/api-thanh-toan-vnpay-paypal

## Task hiện tại
- Chức năng: Backend tạo URL thanh toán (VNPay / PayPal) và xử lý callback thanh toán
- Nhánh: `feature/api-thanh-toan-vnpay-paypal`
- Cập nhật lần cuối: 11/11/2026, Antigravity

## Kế hoạch chia nhỏ các chức năng
1. **Chức năng 1 (Cấu hình & Entity):** Tạo enum `PhuongThucThanhToan`, `TrangThaiThanhToan`, Entity `ThanhToan`, `ThanhToanRepository`, bổ sung config VNPay/PayPal trong `application.yml`.
2. **Chức năng 2 (Cổng VNPay):** Service sinh URL thanh toán VNPay Sandbox (HMAC-SHA512) và API xử lý callback kiểm tra chữ ký, cập nhật trạng thái `THANH_CONG` / `DA_THANH_TOAN`.
3. **Chức năng 3 (Cổng PayPal):** Service tạo Order PayPal Sandbox (quy đổi VND -> USD), lấy approval link và API capture xác nhận giao dịch phía BE.
4. **Chức năng 4 (API Chung & Lịch sử giao dịch):** Controller chung `/api/v1/thanh-toan`, API tạo URL thanh toán theo phương thức và API xem lịch sử giao dịch (lọc ngày, trạng thái, phân trang theo ảnh 2).
5. **Chức năng 5 (Kiểm thử tự động):** Chạy test toàn bộ luồng tạo URL, chữ ký VNPay, capture và cập nhật trạng thái đặt chỗ.

## Đã xong (đã commit)
- (Chưa có)

## Đang dở
- Khởi tạo kế hoạch và cấu trúc nhánh.

## Việc tiếp theo
1. Thực hiện Chức năng 1: Entity `ThanhToan`, repository và cấu hình.
