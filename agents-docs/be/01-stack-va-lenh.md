# BE – Stack và lệnh thường dùng

Backend của **Hệ thống quản lý phòng bán vé máy bay** (đồ án nhóm DTU-Team-6, theo tài liệu SRS v1.2).
Hãy trả lời và viết comment bằng **tiếng Việt**; tên class, biến, bảng giữ đúng quy ước trong các file `agents-docs/be/`.

## Stack (đừng tự ý đổi)

- Java **21**, Spring Boot **4.1.2**, build bằng **Gradle** (`./gradlew`), KHÔNG dùng Maven.
- MySQL **9.7.4 LTS**, quản lý schema bằng **Flyway**. Frontend dùng ReactJS ở repo riêng `QuanLyPhongVeMayBay-FE` (file hướng dẫn của frontend nằm trong repo đó, không phải ở đây).
- Base package: `com.team6.quanlyvemaybay`.
- Spring Boot 4 khác bản 3: starter web là `spring-boot-starter-webmvc` (không phải `starter-web`), Flyway có starter riêng. Nếu ví dụ trên mạng viết cho Spring Boot 3 mà lỗi, kiểm tra lại tên package/starter trước khi sửa logic.
- Spring Boot 4 dùng **Jackson 3**: phần lõi và databind nằm ở package `tools.jackson.*` (ví dụ `tools.jackson.databind`), khác Jackson 2 là `com.fasterxml.jackson.*`. **Ngoại lệ: annotation** (`@JsonProperty`, `@JsonInclude`, `@JsonIgnore`...) vẫn ở `com.fasterxml.jackson.annotation`, không đổi. Vì vậy **không thay hàng loạt** `com.fasterxml` thành `tools.jackson`, làm vậy sẽ hỏng import của annotation. Ví dụ trên mạng viết cho Jackson 2 mà lỗi import thì chỉ đổi các import lõi/databind; xem `build.gradle` và import thực tế trong project trước khi sửa.
- Flyway cho MySQL cần thêm thư viện **`flyway-mysql`** (ngoài starter của Flyway). Nếu khi chạy app báo Flyway không nhận ra hoặc không hỗ trợ MySQL, kiểm tra `build.gradle` đã có `flyway-mysql` chưa.
- Xác thực và phân quyền: **Spring Security**, **OAuth2 Client**, **OAuth2 Resource Server** (JWT, đăng nhập Google/Facebook bằng OAuth2). Chi tiết ở `07-chua-chot.md`. Không tự thêm dependency vào `build.gradle` khi chưa được phê duyệt.
- Test: hiện giữ nguyên cấu hình database test đang có. Testcontainers + MySQL là hướng đã chốt nhưng **chưa triển khai** (task riêng, xem `07-chua-chot.md`).

## Lệnh thường dùng

```bash
export DB_PASSWORD='...'      # bắt buộc, mỗi cửa sổ Terminal
./gradlew bootRun             # chạy app (cổng 8080)
./gradlew build               # build + test (test cần MySQL đang chạy)
./gradlew build -x test       # build nhanh, bỏ test
```

Database: `ve_may_bay`, user `veapp`, kết nối `localhost:3306`. Cấu hình ở `src/main/resources/application.yml`. Test hiện giữ nguyên cấu hình đang có; không viết test ghi hoặc xoá dữ liệu trên database dev (xem `07-chua-chot.md`).
