# BE – Dựng môi trường từ đầu

Mỗi người làm trên máy mình.

1. **JDK 21:** `java -version` phải ra `21.x`. Chưa có thì cài Temurin 21 (macOS: `brew install --cask temurin@21`), rồi `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`.
2. **MySQL 9.7.4 LTS:** cài và bật server. Trên macOS cài bằng file `.dmg` thì thêm `/usr/local/mysql/bin` vào `PATH`. Kiểm tra: `mysql --version`.
3. **Tạo database và user** (đăng nhập bằng `mysql -u root -p`). Mật khẩu do mỗi người tự đặt, không dùng chung, không commit:
   ```sql
   CREATE DATABASE ve_may_bay CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   CREATE USER 'veapp'@'localhost' IDENTIFIED BY '<mật khẩu của bạn>';
   GRANT ALL PRIVILEGES ON ve_may_bay.* TO 'veapp'@'localhost';
   ```
   Ở trạng thái repository hiện tại chưa có migration Flyway; `ddl-auto` cũng tắt nên ứng dụng không tự tạo bảng. Cần dùng database đã được chuẩn bị đúng schema. Không chạy trực tiếp `schema.sql` vì file này có lệnh `DROP TABLE`.
4. **Lấy code:** `git clone` repo, `git checkout develop`, rồi tạo nhánh làm việc từ đó.
5. **Chạy app** (trong cùng một cửa sổ Terminal, vì `export` chỉ có hiệu lực ở cửa sổ đó):
   ```bash
   export DB_NAME='ve_may_bay'
   export DB_USERNAME='veapp'
   export DB_PASSWORD='<mật khẩu của bạn>'
   export JWT_SECRET="$(openssl rand -base64 32)"
   export COOKIE_SECURE=false
   ./gradlew bootRun
   ```
   Thành công khi log có `Started QuanlyvemaybayApplication`.
6. **Kiểm tra:** `mysql -u veapp -p ve_may_bay -e "SHOW TABLES;"` để xác nhận database đã có schema trước khi dùng API.

## Chạy BE để FE kiểm thử tài khoản

- Chọn phiên bản BE có đăng ký, đăng nhập, API hồ sơ và đổi mật khẩu, cùng phiên bản FE tương thích. Khi code chỉ có ở nhánh cục bộ chưa được đẩy, thành viên khác chưa thể lấy về; xác nhận commit/nhánh với người phụ trách hoặc dùng `develop` sau khi merge.
- Sao chép `.env.example` thành `.env` nếu chưa có; điền `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` theo database kiểm thử đã chuẩn bị. Điền `JWT_SECRET` ngẫu nhiên riêng cho môi trường, dài tối thiểu 32 byte; đặt `COOKIE_SECURE=false` cho HTTP local và `APP_FRONTEND_URL` trùng origin Vite, ví dụ `http://localhost:5173`. Không chia sẻ hoặc commit `.env` và secret.
- Spring Boot không tự đọc file `.env`. Trên macOS/Linux, trong thư mục BE, chỉ nạp file `.env` do mình tin cậy, có cú pháp shell hợp lệ; đặt giá trị chứa ký tự đặc biệt trong dấu nháy phù hợp:

  ```bash
  set -a
  . ./.env
  set +a
  ./gradlew bootRun
  ```

  Giữ terminal mở và đợi log `Started QuanlyvemaybayApplication`. Kiểm tra JDBC URL trong log trỏ đúng database. Trên PowerShell, đặt các biến trong terminal như phần Windows bên dưới; không chạy `. ./.env` kiểu Bash.
- Database phải có schema nền tương thích; database mới tạo rỗng chưa đủ. BE hiện không tự chạy migration và `ddl-auto` là `none`. Trên đúng database kiểm thử, kiểm tra `SHOW TABLES LIKE 'phien_lam_moi';` và `SHOW COLUMNS FROM nguoi_dung LIKE 'anh_dai_dien_public_id';`. Nếu thiếu, báo và xin phép người dùng trước khi áp dụng thủ công tương ứng `src/main/resources/db/migration/V20261117_1331__them_phien_lam_moi.sql` và `V20261118_1246__them_public_id_avatar.sql`; sao lưu trước, không chạy lại migration đã áp dụng. Không dùng `schema.sql` để làm lại database vì có lệnh `DROP TABLE`.
- Khi đã được phép áp dụng, mở MySQL bằng user có quyền với database kiểm thử, chọn đúng database (`USE ten_database;`, thay bằng tên thực tế) và kiểm tra `SELECT DATABASE();`. Từ thư mục gốc repo BE, dùng lệnh `SOURCE src/main/resources/db/migration/V20261117_1331__them_phien_lam_moi.sql;` hoặc `SOURCE src/main/resources/db/migration/V20261118_1246__them_public_id_avatar.sql;` **chỉ cho mục còn thiếu**, rồi kiểm tra lại bảng/cột bằng các lệnh trên.
- FE sao chép `.env.example` của FE, đặt `VITE_API_BASE_URL=http://localhost:8080`, chạy `npm install` và `npm run dev`. Kiểm thử tạo tài khoản, đăng nhập, lưu hồ sơ và đổi mật khẩu qua FE. Mật khẩu mới cần đăng nhập lại sau khi access token hiện tại hết hạn; nút quên mật khẩu chưa kết nối API.
- Đăng nhập Google/Facebook cần bật và cấu hình OAuth cùng migration `V20261117_1552__them_lien_ket_oauth.sql`; thay avatar cần cấu hình Cloudinary và signed upload preset. Hai phần này không bắt buộc để kiểm thử đăng nhập bằng mật khẩu và hồ sơ. Xem `docs/auth-api.md`; không đưa secret Cloudinary/OAuth vào FE.
- Nếu gặp lỗi thiếu bảng/cột hoặc 500, kiểm tra tên database và migration trước; 401 có thể do chưa đăng nhập hoặc phiên hết hạn; 403 có thể do thiếu CSRF/quyền; lỗi CORS cần đối chiếu `APP_FRONTEND_URL` với origin FE. Không tắt CSRF hoặc thử ghi/xoá dữ liệu trong database của người khác.

## Windows

Các bước 3, 4, 6 và lệnh SQL giống hệt macOS. Chỉ khác cách cài đặt và cách đặt biến môi trường. Dùng **PowerShell** (terminal mặc định của VS Code trên Windows).

1. **JDK 21:** cài Temurin 21 bằng `winget install EclipseAdoptium.Temurin.21.JDK`, hoặc tải file `.msi` ở adoptium.net (khi cài nhớ tick "Set JAVA_HOME variable"). Mở **terminal mới** rồi kiểm tra `java -version` phải ra `21.x`.
2. **MySQL 9.7.4 LTS:** tải **MySQL Installer** ở dev.mysql.com, chọn cài *MySQL Server* (có thể thêm *MySQL Shell*/*Workbench* nếu muốn). Khác macOS: installer **hỏi đặt mật khẩu root** ngay lúc cài, hãy nhớ nó. Server chạy dưới dạng dịch vụ Windows, nếu không kết nối được thì mở `services.msc`, tìm dịch vụ tên bắt đầu bằng `MySQL` và bấm Start.
3. **Thêm lệnh `mysql` vào PATH:** thư mục thường là `C:\Program Files\MySQL\MySQL Server <phiên bản>\bin` (xem đúng tên thư mục trong máy). Thêm vĩnh viễn qua *Edit the system environment variables → Environment Variables → Path → New*, rồi mở terminal mới. Chỉ dùng tạm trong cửa sổ hiện tại:
   ```powershell
   $env:Path += ";C:\Program Files\MySQL\MySQL Server 9.7\bin"
   mysql --version
   ```
4. **Chạy app:** `export` và `./gradlew` kiểu macOS không dùng được trên PowerShell:
   ```powershell
   $env:DB_HOST = 'localhost'
   $env:DB_PORT = '3306'
   $env:DB_NAME = '<tên database kiểm thử>'
   $env:DB_USERNAME = '<tên user MySQL>'
   $env:DB_PASSWORD = '<mật khẩu của bạn>'
   $env:JWT_SECRET = '<chuỗi ngẫu nhiên riêng dài tối thiểu 32 byte>'
   $env:COOKIE_SECURE = 'false'
   $env:APP_FRONTEND_URL = 'http://localhost:5173'
   .\gradlew bootRun
   ```
   Nếu dùng **cmd** thay vì PowerShell: đặt các biến tương ứng bằng `set` (ví dụ `set DB_PASSWORD=<mật khẩu>`, không có dấu nháy, vì cmd giữ nguyên dấu nháy làm một phần mật khẩu) rồi `gradlew bootRun`. Nếu dùng **Git Bash** thì làm đúng như macOS (`export ...`, `./gradlew bootRun`).
5. Các biến `$env:...` chỉ có hiệu lực trong cửa sổ PowerShell đó, mở cửa sổ mới phải đặt lại. Muốn cố định thì đặt bằng *Environment Variables* của Windows, nhưng chỉ làm trên máy cá nhân, không bao giờ ghi vào file trong repo.

## Docker cho test (Testcontainers)

Testcontainers + MySQL là hướng đã chốt cho test nhưng **chưa triển khai** (task riêng, xem `07-chua-chot.md`), nên hiện tại chưa cần Docker. Khi task đó được làm, máy cần có **Docker** (Docker Desktop trên macOS/Windows); kiểm tra: `docker version`. Chạy app bình thường (`./gradlew bootRun`) không cần Docker.

## Lỗi thường gặp khi dựng môi trường

| Lỗi | Nguyên nhân và cách xử lý |
|---|---|
| `Access denied for user 'veapp'` | User chưa được tạo, hoặc `DB_PASSWORD` sai/chưa `export` trong cửa sổ Terminal đang chạy. Thử `mysql -u veapp -p ve_may_bay` để kiểm tra mật khẩu |
| Không có bảng khi gọi API | Repository hiện chưa có migration Flyway và Hibernate không tự tạo schema. Xác nhận đã cấu hình đúng database đã được chuẩn bị; không chạy trực tiếp `schema.sql` vì file có lệnh `DROP TABLE` |
| `contextLoads() FAILED` khi `./gradlew build` | Test khởi động cả app nên hiện cần MySQL đang chạy và `DB_PASSWORD` đúng (sau khi chuyển sang Testcontainers thì cần Docker đang chạy). Chạy `./gradlew build -x test` nếu chỉ cần build |
| `No matching toolchains found ... Java 21` | Chưa cài JDK 21. Cài xong chạy `./gradlew --stop` |
| `mysql: command not found` (macOS) hoặc `'mysql' is not recognized` (Windows) | Chưa thêm thư mục `bin` của MySQL vào `PATH`; trên Windows nhớ mở terminal mới sau khi sửa |
| `export : The term 'export' is not recognized` (Windows) | `export` là lệnh của macOS/Git Bash. Trên PowerShell dùng `$env:DB_PASSWORD = '...'` |
| `Can't connect to MySQL server on 'localhost'` (Windows) | Dịch vụ MySQL chưa chạy: mở `services.msc` và Start dịch vụ `MySQL...` |

Agent **không tự chạy** `DROP DATABASE`, `DROP USER` hay xoá dữ liệu. Nếu cần làm lại database rỗng, hướng dẫn người dùng tự chạy lệnh.
