# BE – Quy tắc nghiệp vụ lấy từ SRS

- `dat_cho` tương ứng bảng `VE` trong SRS (mã đặt chỗ, tổng giá); `ve` là vé điện tử cho từng hành khách trên từng chặng.
- **`gia_goc` (giá mua từ hãng) tuyệt đối không trả ra API dành cho khách hàng và không in lên vé.** Chỉ nhân viên/admin được xem để làm báo cáo.
- Một lần thanh toán PayPal có thể gồm nhiều đặt chỗ (`thanh_toan_dat_cho`).
- Vai trò: `ADMIN`, `NHAN_VIEN`, `KHACH_HANG`. Khách do nhân viên nhập tại quầy có thể chưa có `tai_khoan`.
- Báo cáo (doanh thu theo hãng, nhân viên, thời gian) là truy vấn tổng hợp, không có bảng riêng.
- **JSON báo lỗi (đã chốt):** theo **RFC 9457** (`application/problem+json`, gồm `type`, `title`, `status`, `detail`, `instance`) và thêm hai trường: `code` (mã lỗi cố định, dạng chuỗi) và `details` (**mảng** các phần tử gồm `field`, `code`, `message`; nếu không có lỗi chi tiết thì trả `details: []`). FE dựa vào `code`, không dựa vào nội dung `detail`.
- **Lỗi "ghế đã bị chiếm":** khi chọn hoặc đặt ghế mà ghế đã có người đặt (kể cả khi vi phạm `UNIQUE(chuyen_bay_id, ghe_id)`), BE trả **HTTP 409** với `code` = **`GHE_DA_BI_CHIEM`** để FE nhận ra và cho người dùng chọn lại.
- **Tách DTO cho khách và cho nhân viên/admin:** trường chỉ nhân viên được xem (như `gia_goc`) chỉ nằm trong DTO của nhân viên/admin. Không dùng chung một DTO rồi gán `null` hoặc ẩn cho khách.
- **PayPal:** việc xác nhận thanh toán do BE làm (BE tự xác minh với PayPal), không tin kết quả do FE báo về. `client-id` của PayPal được phép đưa ra FE, `secret` chỉ ở BE và đọc từ biến môi trường.
