# Tiến độ nhánh feature/api-thanh-toan-vnpay-paypal

## Task hiện tại
- Chức năng: Backend tạo URL thanh toán (VNPay / PayPal) và xử lý callback thanh toán
- Nhánh: `feature/api-thanh-toan-vnpay-paypal`
- Cập nhật lần cuối: 11/11/2026, Antigravity

## Đã xong (đã commit & kiểm thử thực tế PASS 100%)
1. **Cấu hình & Entity:**
   - Tạo enum `PhuongThucThanhToan` (VNPAY, PAYPAL...) và `TrangThaiThanhToan` (CHO_XU_LY, THANH_CONG, THAT_BAI).
   - Tạo entity `ThanhToan` ánh xạ bảng `thanh_toan` trong `schema.sql`.
   - Tạo `ThanhToanRepository` hỗ trợ tìm kiếm theo `maGiaoDich`, `maGiaoDichDoiTac`, `datChoId`.
   - Bổ sung cấu hình an toàn cho VNPay và PayPal sandbox trong `application.yml` (đọc qua biến môi trường).
2. **Cổng thanh toán VNPay:**
   - Tạo `VnPayService` tính toán HMAC-SHA512 chuẩn đặc tả VNPay.
   - Endpoint tạo URL thanh toán `POST /api/v1/thanh-toan/vnpay/create-url`.
   - Endpoint tiếp nhận callback `GET /api/v1/thanh-toan/vnpay/callback`: kiểm tra chữ ký `vnp_SecureHash`, cập nhật trạng thái `ThanhToan` thành `THANH_CONG` và chuyển `DatCho` sang `DA_THANH_TOAN`.
3. **Cổng thanh toán PayPal:**
   - Tạo `PayPalService`: quy đổi VND sang USD theo tỷ giá cấu hình, gọi PayPal Sandbox API tạo order và approval link.
   - Endpoint `POST /api/v1/thanh-toan/paypal/create-order`.
   - Endpoint `POST /api/v1/thanh-toan/paypal/capture`: BE tự xác minh kết quả capture với PayPal, cập nhật `ThanhToan` và `DatCho`.
4. **Endpoint chung & Lịch sử giao dịch (khớp 100% Ảnh 2):**
   - Endpoint chung `POST /api/v1/thanh-toan/tao-url`: nhận `phuongThuc` (`VNPAY` hoặc `PAYPAL`).
   - Endpoint `GET /api/v1/thanh-toan/lich-su`: hỗ trợ lọc theo khoảng ngày (`tuNgay`, `denNgay`), `trangThai`, phân trang `page`, `size`.
5. **Kiểm thử tự động thực tế (PASS 9/9):**
   - Đã kiểm thử đầy đủ các luồng tạo URL, chữ ký đúng/sai, đơn đã thanh toán, đơn không tồn tại, capture PayPal và xem lịch sử giao dịch.

## Quyết định đã chốt
- Không thêm dependency bên ngoài vào `build.gradle`, dùng mã hóa `HmacSHA512` tiêu chuẩn của Java.
- Tuyệt đối không lưu secret cứng trong code.
