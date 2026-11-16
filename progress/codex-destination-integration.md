# Tiến độ nhánh codex/destination-integration

## Task hiện tại

- Chức năng: hoàn thiện danh sách/thêm điểm đến và API liên kết FE (task 3).
- Nhánh: `codex/destination-integration`, từ `origin/develop`.
- Cập nhật: 16/11/2026, agent BE.

## Đã xong (chưa commit)

- `GET /api/v1/destinations/airports` trả danh mục sân bay thật, sắp theo thành phố/mã, DTO gồm mã/tên/thành phố/quốc gia.
- Tạo điểm đến khoá ghi sân bay trước khi kiểm tra trùng trong cùng transaction, xử lý tuần tự các request cùng sân bay mà không thêm migration.
- Giới hạn bộ lọc list: keyword <=100, mã sân bay <=10, page >=0, size 1..100.
- Validate liên kết ảnh tùy chọn <=255: root-relative hoặc HTTP(S), không nhận protocol-relative, credentials, backslash/control và đoạn đường dẫn quay lui kể cả mã hoá.
- Tests service/controller cho danh mục, ràng buộc, thứ tự khoá; tests định dạng ảnh; tests quyền GET công khai/POST staff hoặc admin + CSRF.
- Ghi `docs/QA_DIEM_DEN.md` để đối chiếu các trường hiện có, tách kiểm thử kỹ thuật với xác nhận thiết kế/nghiệp vụ còn chờ.
- Targeted destination/image tests: BUILD SUCCESSFUL 12 giây.
- Full `gradlew.bat build`: BUILD SUCCESSFUL 32 giây.

## Đang dở

- Chờ QA agent phản biện và parent kiểm thử API thực/two concurrent creates trên database QA riêng.
- FE agent đang triển khai danh sách và form theo hợp đồng đã thống nhất.

## Việc tiếp theo

1. Kiểm thử tích hợp và sửa finding (nếu có).
2. Commit task 3, xoá file tiến độ trước PR theo Git flow repo.
3. Push PR task 3 vào develop (parent thực hiện).

## Quyết định và lưu ý

- Không thay cấu trúc dữ liệu hoặc thêm dependency. Các trường lấy từ DTO/schema hiện có, chờ QA nghiệp vụ xác nhận thiết kế 99.7.3.
- Ảnh là liên kết, chưa triển khai upload Cloudinary trong task này.
- Duplicate locking bảo vệ đường tạo mới qua service này; database chưa có unique key tên/sân bay.

## Lỗi đang gặp

- Không có lỗi build tại lần kiểm tra cuối.

- QA reviewer approved. Real MySQL concurrent creation same airport:201+409, exactly1 row. Catalog5, filter/pagination, missingname400, unknownairport404, unsafeimage400, size101400 PASS. Browser FE creation/list PASS. Pending Sonar before push.
