# BE – Git và quy trình làm việc

## Git

- Nhánh tích hợp của nhóm là **`develop`**. Không đẩy thẳng lên `develop` hay `main`. Tạo nhánh làm việc từ `develop` (`feature/...`, `fix/...`, `chore/...`), xong thì mở Pull Request vào `develop`.
- **Trước khi merge** phải làm đủ ba bước:
  1. Pull `develop` mới nhất về nhánh của mình (`git pull origin develop`) và **xử lý hết conflict**.
  2. Sau khi hết conflict, **pull lần nữa** để chắc chắn không có thay đổi mới chen vào, rồi chạy `./gradlew build`.
  3. Chỉ merge khi bước 1 và 2 đã sạch.
- Agent không được tự ý giải conflict bằng cách xoá code của người khác. Nếu conflict hoặc lỗi liên quan đến file Flyway (`db/migration/`), dừng lại và hỏi. Version đặt theo thời gian nên hiếm khi trùng, nhưng vẫn có thể gặp: trùng đúng phút, hoặc file nhánh mình có thời gian nhỏ hơn file đã chạy ở nhánh khác (xem `03-database.md`).
- Commit theo kiểu `feat: ...`, `fix: ...`, `chore: ...`.
- Không commit `build/`, `.gradle/`, `.vscode/`, `.env` (đã có trong `.gitignore`).

## Khi làm việc

- **Task dài thì chia nhỏ ra từng chức năng rồi làm lần lượt**, không làm một mạch cả khối lớn:
  1. Trước khi code, liệt kê ngắn gọn các chức năng nhỏ cần làm và thứ tự, rồi để người dùng xác nhận.
  2. Làm xong một chức năng (entity, repository, service, controller, validate của chức năng đó) thì chạy `./gradlew build` và báo kết quả, rồi mới sang chức năng kế tiếp.
  3. Mỗi chức năng một commit riêng, để dễ review và dễ hoàn tác.
  - Ví dụ: "đặt vé" gồm các chức năng nhỏ như tìm chuyến bay, chọn khoang và ghế, nhập thông tin hành khách, tạo đặt chỗ, rồi mới đến thanh toán.
- Làm đúng phạm vi được yêu cầu, thay đổi nhỏ và dễ review.
- **Trước khi code:** kiểm tra các file migration, schema hiện tại, cấu trúc project và `05-bao-mat-va-cau-truc.md`.
- Trước khi sửa schema (thêm migration) hoặc đổi thư viện, nói rõ lý do và hỏi lại: báo cáo các thay đổi schema và file migration dự kiến **trước khi thực hiện**. Không sửa migration đã áp dụng. Không tự thêm dependency vào `build.gradle` khi chưa được phê duyệt.
- Sau khi thêm code, chạy `./gradlew build` và báo kết quả thật, không đoán.
