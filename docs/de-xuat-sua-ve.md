# Đề xuất thiết kế sửa thông tin riêng theo vé

## Căn cứ đã kiểm tra

- SRS v1.2, mục 6.6.1.11: nhân viên sửa **Tên Người đi** (bắt buộc, 10–50 ký tự) và **Email thanh toán** (bắt buộc, đúng định dạng). SRS không quy định sửa sau thanh toán.
- Quyết định tạm thời của người dùng: chỉ sửa khi cả `ve_may_bay.trang_thai` và `dat_cho.trang_thai` đều là `CHO_THANH_TOAN`. Không mở rộng cho vé đã thanh toán; cần BA/mentor xác nhận.
- `ve_may_bay` hiện trỏ tới `hanh_khach` và `dat_cho`. Tên nằm ở `hanh_khach.ho_ten`, email ở `dat_cho.email_nguoi_dat`; các bản ghi này có thể được nhiều vé dùng chung.
- `ve_may_bay` hiện không có cờ “đã sử dụng”. Ba trạng thái hiện có là `CHO_THANH_TOAN`, `DA_THANH_TOAN`, `DA_HUY`. Quy tắc chỉ sửa vé chờ thanh toán loại trừ vé đã thanh toán/hủy; nếu cần nhận biết vé đã sử dụng độc lập với thanh toán, phải đặc tả và bổ sung dữ liệu sau.
- Repository chỉ có `schema.sql` và `data.sql`, không có file Flyway hoặc dependency Flyway; `ddl-auto: none`, SQL init tắt. Không chạy `schema.sql` vì file có lệnh `DROP TABLE`.
- MySQL80 trên máy đang chạy nhưng không có credential trong phiên này; truy vấn metadata bằng root không mật khẩu bị từ chối. Vì vậy thiết kế dựa trên schema được quản lý trong repository, chưa xác minh trực tiếp database đang chạy.

## Thay đổi schema đề xuất

Thêm **hai cột vào `ve_may_bay`**:

| Cột | Kiểu | Nguồn khởi tạo | Mục đích |
|---|---|---|---|
| `ho_ten_nguoi_di` | `VARCHAR(100) NOT NULL` | `hanh_khach.ho_ten` theo `hanh_khach_id` | Tên hiển thị riêng của vé |
| `email_thanh_toan` | `VARCHAR(100) NOT NULL` | `dat_cho.email_nguoi_dat` theo `dat_cho_id` | Email hiển thị/liên hệ riêng của vé |

Độ dài cột giữ được dữ liệu cũ tối đa 100 ký tự. API sửa áp quy tắc SRS 10–50 ký tự cho tên và tối đa 50 ký tự cho email. Không thay đổi khóa ngoại, cột giá, trạng thái đặt chỗ, thanh toán hay dữ liệu hành khách gốc. Hai cột snapshot thuộc từng hàng vé; sửa một vé không cập nhật hàng vé khác.

Migration dự kiến theo quy tắc nhóm: `src/main/resources/db/migration/V<yyyyMMdd>_<HHmm>__them_snapshot_thong_tin_ve.sql` (chọn thời điểm version khi được duyệt). Các bước:

1. Kiểm tra tất cả vé có hành khách và đặt chỗ hợp lệ, và `hanh_khach.dat_cho_id` khớp `ve_may_bay.dat_cho_id`. Nếu có dữ liệu lệch, dừng để xử lý riêng; không tự xóa hoặc sửa dữ liệu.
2. Thêm hai cột nullable tạm thời.
3. Backfill toàn bộ vé hiện có bằng một `UPDATE ... JOIN` từ hành khách và đặt chỗ tương ứng.
4. Kiểm tra không còn `NULL`; sau đó đổi hai cột thành `NOT NULL`.

MySQL có thể auto-commit DDL; cần thử migration trên bản sao database trước khi áp dụng môi trường dùng chung và có phương án backup/khôi phục. **Chưa tạo file migration và chưa chạy bất kỳ lệnh DDL/DML nào.**

## Ảnh hưởng code và API

- Entity `VeMayBay` thêm hai thuộc tính snapshot. Đường tạo vé trong tương lai phải gán hai giá trị này; repository hiện chưa có service tạo vé. `data.sql` hiện liệt kê cột cụ thể khi chèn vé, nên cần cập nhật seed nếu nhóm dùng file đó để dựng database thử nghiệm.
- API sửa dự kiến: `PUT /api/v1/ve-may-bay/{id}` dành cho `ADMIN`/`NHAN_VIEN`, nhận `hoTenNguoiDi` và `emailThanhToan`; trả DTO chỉ gồm thông tin vé được sửa, không chứa `giaGoc`. Kiểm tra CSRF khi dùng cookie. Vé không tồn tại trả 404; trạng thái không cho phép trả 409 với mã lỗi cố định; dữ liệu đầu vào sai trả 400 theo RFC 9457.
- Service khóa vé trong transaction, kiểm tra cả vé và đặt chỗ đều chờ thanh toán, rồi chỉ cập nhật hai cột snapshot của vé đó. Không sửa `hanh_khach`, `dat_cho`, giá, chuyến bay hoặc bản ghi thanh toán.
- `GET /api/v1/ve-may-bay` hiện lấy `tenNguoiDi` từ `hanh_khach`; cần đổi sang `ve_may_bay.ho_ten_nguoi_di`. Tìm kiếm `TEN_NGUOI_DI` cũng phải tìm theo snapshot để kết quả khớp thông tin hiển thị. Các trường và bộ lọc khác của API danh sách giữ nguyên.
- Chức năng in vé/PDF chưa có trong backend; khi triển khai Task 4 phải đọc tên/email của chính vé từ snapshot và tuyệt đối không in `giaGoc`.
- Thanh toán hiện theo đặt chỗ; `email_thanh_toan` riêng của vé không tự thay thế email người đặt ở cổng thanh toán hoặc thông báo chung của đặt chỗ. Cách sử dụng email này cho giao tiếp thực tế cần BA/mentor xác nhận.

## Điểm cần duyệt trước khi code migration

1. Phê duyệt hai cột snapshot, quy trình backfill và thay đổi API nêu trên.
2. Chọn cơ chế áp dụng migration: bổ sung Flyway starter và `flyway-mysql` (cần duyệt dependency, đồng thời kiểm tra tương thích database hiện tại), hoặc giữ script SQL versioned để nhóm áp dụng thủ công. Hiện repository chưa có Flyway runtime nên chỉ thêm file `V...sql` sẽ không tự chạy. Nếu bật Flyway trên database đã có bảng nhưng chưa có `flyway_schema_history`, ứng dụng có thể không khởi động; cần kế hoạch baseline riêng, không tự bật `baselineOnMigrate` hoặc đánh dấu migration đã chạy.
