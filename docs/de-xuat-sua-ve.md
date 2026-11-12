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

Độ dài cột giữ được dữ liệu cũ tối đa 100 ký tự. API sửa áp quy tắc SRS 10–50 ký tự cho tên; email phải đúng định dạng và tối đa 100 ký tự theo cột hiện có. Không thay đổi khóa ngoại, cột giá, trạng thái đặt chỗ, thanh toán hay dữ liệu hành khách gốc. Hai cột snapshot thuộc từng hàng vé; sửa một vé không cập nhật hàng vé khác.

## Kế hoạch Flyway và baseline (chưa thực hiện)

`agents-docs/be/03-database.md` ghi V1/V2 Flyway đã chạy, nhưng cây nguồn hiện tại không có `db/migration`, Flyway dependency hay `flyway_schema_history` được xác nhận. Không được giả định database đang chạy đúng với `schema.sql` hoặc đã có V1/V2. `schema.sql` chứa `DROP TABLE`, nên không thể dùng nguyên file làm baseline.

1. Dùng tài khoản **chỉ đọc** trên đúng database đích để ghi nhận `SELECT DATABASE(), VERSION(), @@hostname`, danh sách bảng/cột/kiểu/khóa/index/enum từ `information_schema`, trạng thái `flyway_schema_history`, số vé, giá trị null/lệch khóa và dữ liệu danh mục. So sánh với `schema.sql` và entity. Nếu khác biệt hoặc đã có history, dừng để lập kế hoạch riêng; không ghi đè hoặc `repair` tùy tiện.
2. Sau khi xác nhận database hiện hữu chưa có Flyway và khớp schema đã duyệt, chọn một version baseline theo thời gian, ví dụ `20261112_0000` **chỉ là minh họa**, version thực tế phải mới và không trùng vào ngày triển khai. Tạo `V<baseline>__baseline_hien_trang.sql` từ schema **đã xác minh**, bỏ `CREATE DATABASE`, `USE`, `DROP`, thao tác tắt khóa ngoại và dữ liệu thử nghiệm. File này dùng dựng database mới; với database hiện hữu, chạy lệnh `flyway baseline` thủ công tại đúng version sau khi duyệt và sao lưu. Baseline chỉ tạo mốc trong history, không chạy lại DDL cũ.
3. Không bật `baselineOnMigrate` hoặc auto-migration khi chưa hoàn tất kiểm tra và baseline; giữ `ddl-auto: none`, SQL init tắt. Chỉ sau khi được duyệt mới bổ sung `spring-boot-starter-flyway` và `flyway-mysql` vào Gradle, kiểm tra phiên bản tương thích. Giữ `spring.flyway.enabled=false` trong giai đoạn chuẩn bị. Không gọi `clean`, không bật `outOfOrder`.

   `build.gradle` đang dùng Spring Boot **4.1.2** và Java **21**. BOM của Spring Boot 4.1.2 quản lý `flyway-mysql` ở **12.4.0**; dùng version do BOM quản lý, không ghim riêng hai module. Chưa thêm dependency vì khi có Flyway trên classpath, Spring Boot có thể chạy migration lúc khởi động nếu chưa cấu hình tắt rõ ràng. Phiên bản **MySQL server thực tế** vẫn cần xác minh bằng quyền chỉ đọc trước khi quyết định chạy.
4. Đã chuẩn bị **nhưng chưa chạy** `V20261112_1100__them_snapshot_thong_tin_ve.sql` (hai cột nullable) và `V20261112_1110__backfill_snapshot_thong_tin_ve.sql` (`UPDATE ... JOIN`, chỉ điền ô còn null của từng vé). Version baseline đề xuất `20261112_1000` đứng trước hai file này; chỉ chốt sau khi đọc schema/history thực tế và bảo đảm không có version trùng. Migration đổi sang `NOT NULL` sẽ được tạo ở version sau, khi mọi đường ghi vé đã điền snapshot và bản sao database đã qua kiểm thử. Kiểm tra trước rằng `hanh_khach.dat_cho_id` khớp `ve_may_bay.dat_cho_id`, nguồn không null và độ dài vừa cột. Nếu có dữ liệu lệch, dừng và báo riêng.
5. Chạy thử baseline/migration trên **bản sao database** với ứng dụng phiên bản cũ và mới; kiểm tra row count, giá trị snapshot, insert vé, API danh sách/sửa/xóa. Khi đưa lên môi trường dùng chung, sao lưu nhất quán và thử khôi phục trước, dừng hoặc khóa writer trong cửa sổ chuyển đổi, ghi lại version/checksum và chỉ chạy migration sau khi được duyệt.

**Rollback:** MySQL DDL không thể trông cậy vào `ROLLBACK` của transaction. Nếu bản mới lỗi, quay lại ứng dụng tương thích với các cột nullable đã thêm, giữ dữ liệu và sửa bằng migration tiến tới. Nếu migration dở dang hoặc dữ liệu sai, dừng ghi, kiểm tra schema/history, khôi phục từ bản sao lưu đã thử trên môi trường tách biệt rồi mới quyết định khôi phục database đích. Không tự `DROP COLUMN`, `flyway clean` hoặc sửa history để che lỗi. Đã chạy baseline/migration **chỉ trên container test tách biệt**; chưa chạy trên database hiện có của máy hoặc nhóm.

## Ảnh hưởng code và API

- Entity `VeMayBay` thêm hai thuộc tính snapshot. Đường tạo vé trong tương lai phải gán hai giá trị này; repository hiện chưa có service tạo vé. `data.sql` hiện liệt kê cột cụ thể khi chèn vé, nên cần cập nhật seed nếu nhóm dùng file đó để dựng database thử nghiệm. Chưa sửa `schema.sql`/`data.sql` khi chưa đối chiếu database thật.
- API sửa dự kiến: `PUT /api/v1/ve-may-bay/{id}` dành cho `ADMIN`/`NHAN_VIEN`, nhận `hoTenNguoiDi` và `emailThanhToan`; trả DTO chỉ gồm thông tin vé được sửa, không chứa `giaGoc`. Kiểm tra CSRF khi dùng cookie. Vé không tồn tại trả 404; trạng thái không cho phép trả 409 với mã lỗi cố định; dữ liệu đầu vào sai trả 400 theo RFC 9457.
- Service khóa vé trong transaction, kiểm tra cả vé và đặt chỗ đều chờ thanh toán, rồi chỉ cập nhật hai cột snapshot của vé đó. Không sửa `hanh_khach`, `dat_cho`, giá, chuyến bay hoặc bản ghi thanh toán.
- `GET /api/v1/ve-may-bay` hiện lấy `tenNguoiDi` từ `hanh_khach`; cần đổi sang `ve_may_bay.ho_ten_nguoi_di`. Tìm kiếm `TEN_NGUOI_DI` cũng phải tìm theo snapshot để kết quả khớp thông tin hiển thị. Các trường và bộ lọc khác của API danh sách giữ nguyên.
- Chức năng in vé/PDF chưa có trong backend; khi triển khai Task 4 phải đọc tên/email của chính vé từ snapshot và tuyệt đối không in `giaGoc`.
- Thanh toán hiện theo đặt chỗ; `email_thanh_toan` riêng của vé không tự thay thế email người đặt ở cổng thanh toán hoặc thông báo chung của đặt chỗ. Cách sử dụng email này cho giao tiếp thực tế cần BA/mentor xác nhận.

## Điểm cần duyệt trước khi code migration

1. Phê duyệt hai cột snapshot, quy trình backfill và thay đổi API nêu trên.
2. Phê duyệt bổ sung Flyway starter và `flyway-mysql`, version baseline thực tế và kế hoạch backup/khôi phục sau khi đã có kết quả kiểm tra database chỉ đọc. Chỉ thêm file `V...sql` sẽ không tự chạy vì repository hiện chưa có Flyway runtime.
3. BA/mentor xác nhận quy tắc tạm thời chỉ sửa khi cả vé và đặt chỗ `CHO_THANH_TOAN`, và ý nghĩa email riêng từng vé đối với thanh toán/email thông báo.
