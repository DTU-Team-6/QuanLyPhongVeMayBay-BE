# BE – Quy tắc về database (quan trọng nhất)

- Schema nằm ở `src/main/resources/db/migration/`. `V1__init_schema.sql` (23 bảng) và `V2__seed_reference_data.sql` (dữ liệu danh mục) **đã chạy, KHÔNG sửa**. Muốn đổi schema thì thêm file mới, **đặt version theo thời gian** để hai người thêm cùng lúc không trùng số: `V<yyyyMMdd>_<HHmm>__<mo_ta>.sql` (hai dấu gạch dưới trước phần mô tả), ví dụ `V20261110_1030__them_bang_khuyen_mai.sql`.
  - Dấu `_` trong phần version được Flyway hiểu như dấu chấm, nên thứ tự vẫn đúng và lớn hơn V1, V2.
  - Không đổi tên hay sửa file đã merge.
  - Nếu file của mình có thời gian nhỏ hơn một file đã được chạy ở nhánh khác, đổi tên file (chưa merge) sang thời gian mới. Không bật `spring.flyway.out-of-order` khi chưa hỏi nhóm.
  - **Trước khi tạo migration:** kiểm tra schema hiện tại, rồi báo người dùng các thay đổi schema và file migration dự kiến, đợi đồng ý mới thực hiện.
- `spring.jpa.hibernate.ddl-auto` luôn là `none`. Không để Hibernate tự tạo hay sửa bảng.
- Tên bảng/cột: `snake_case` tiếng Việt không dấu (`dat_cho`, `hanh_khach`, `gia_ban`). Khoá chính tên `id`, khoá ngoại dạng `<bảng>_id`.
- Ánh xạ JPA:
  - `nguoi` là lớp cha, `khach_hang` và `nhan_vien` là lớp con dùng `@Inheritance(strategy = InheritanceType.JOINED)`. Cột phân biệt là `loai`: khai báo `@DiscriminatorColumn(name = "loai")` trên lớp cha `Nguoi`, và `@DiscriminatorValue` ở mỗi lớp con với giá trị khớp cột `loai` trong SQL.
  - Enum dùng `@Enumerated(EnumType.STRING)`, giá trị khớp đúng `ENUM` trong SQL.
  - Tiền dùng `BigDecimal` (`DECIMAL(15,2)`). Thời gian: xem mục "Múi giờ và thời gian" bên dưới.
  - `dat_cho.phien_ban` ánh xạ `@Version` (khoá lạc quan).
  - `ve` có khoá ngoại ghép `(chuyen_bay_id, khoang_id)` tới `gia_chuyen_bay`.
- Chống bán trùng ghế: ràng buộc `UNIQUE(chuyen_bay_id, ghe_id)` trên `ve` là lớp bảo vệ cuối. Khi huỷ vé phải đặt `ghe_id = NULL`. Ghế phải thuộc máy bay của chuyến, kiểm tra ở tầng service.
- Không xoá cứng dữ liệu nghiệp vụ: nhân viên dùng `trang_thai`, đặt chỗ/vé dùng `DA_HUY`.
- Bảng lưu refresh token (lưu **hash**, hỗ trợ rotation, revoke, phát hiện reuse) phải thêm bằng file migration mới, xem `07-chua-chot.md`.
- Tài liệu thiết kế: ERD/EER nằm ở `docs/database/` (nếu chưa có thì hỏi nhóm, đừng tự suy diễn).

## Múi giờ và thời gian (đã chốt)

- Giờ bay lưu theo **UTC**, ánh xạ kiểu `Instant`.
- Lưu **ZoneId riêng** cho sân bay đi và sân bay đến (múi giờ IANA, ví dụ `Asia/Ho_Chi_Minh`). Nơi lưu (cột ở bảng sân bay hay bảng chuyến bay) quyết định sau khi kiểm tra schema hiện tại.
- API trả mỗi mốc giờ gồm `dateTime` (ISO 8601 có offset) và `timeZone` (IANA ZoneId).
- Schema `V1` có thể đang dùng kiểu cột thời gian khác và chưa có cột múi giờ. Không sửa `V1`; nếu cần đổi, kiểm tra schema hiện tại, báo migration dự kiến, rồi tạo migration mới.
- `dateTime` trong API là giờ địa phương của sân bay kèm offset; `timeZone` là ZoneId IANA tương ứng. Xem quyết định chi tiết ở `07-chua-chot.md`.
