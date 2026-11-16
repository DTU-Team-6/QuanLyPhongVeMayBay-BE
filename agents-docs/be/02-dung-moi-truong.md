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
   V1/V2 trong `src/main/resources/db/migration/` dựng schema và dữ liệu danh mục cho database rỗng. Mỗi thành viên dùng database MySQL local riêng; Flyway đồng bộ schema qua các migration trong repo, không chia sẻ dữ liệu giữa máy. `ddl-auto` vẫn là `none`; không chạy trực tiếp `schema.sql` vì file này có lệnh `DROP TABLE`. Với database đã có Flyway history, kiểm tra history, sao lưu và xin duyệt trước khi repair/migrate.
4. **Lấy code:** `git clone` repo, `git checkout develop`, rồi tạo nhánh làm việc từ đó.
5. **Chạy app** (trong cùng một cửa sổ Terminal, vì `export` chỉ có hiệu lực ở cửa sổ đó):
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=ve_may_bay
   export DB_USERNAME=veapp
   export DB_PASSWORD='<mật khẩu của bạn>'
   export JWT_SECRET="$(openssl rand -base64 32)"
   export COOKIE_SECURE=false
   ./gradlew bootRun
   ```
   Thành công khi log có `Started QuanlyvemaybayApplication`.
6. **Chạy/kiểm tra migration riêng** (không dùng `bootRun` để thay cho repair):
   ```bash
   export FLYWAY_URL='jdbc:mysql://localhost:3306/ve_may_bay'
   export FLYWAY_USER=veapp
   read -s "FLYWAY_PASSWORD?Nhập mật khẩu MySQL: "
   export FLYWAY_PASSWORD
   ./gradlew flywayInfo
   ```
   Chỉ chạy `./gradlew flywayRepair` sau khi sao lưu và được duyệt; sau đó `./gradlew flywayMigrate`. Không đưa mật khẩu vào lệnh hoặc commit.
7. **Kiểm tra:** `mysql -u veapp -p ve_may_bay -e "SHOW TABLES;"` để xác nhận database đã có schema trước khi dùng API.

## Windows

Các lệnh SQL giống macOS, nhưng lệnh shell và biến môi trường khác. Dùng **PowerShell** (terminal mặc định của VS Code trên Windows).

1. **JDK 21:** cài Temurin 21 bằng `winget install EclipseAdoptium.Temurin.21.JDK`, hoặc tải file `.msi` ở adoptium.net (khi cài nhớ chọn thiết lập `JAVA_HOME`). Mở **terminal mới** rồi kiểm tra `java -version` phải ra `21.x`.
2. **MySQL 9.7.4 LTS:** cài MySQL Server theo phiên bản dự án. Trình cài đặt yêu cầu đặt mật khẩu `root`; hãy nhớ mật khẩu đó. Server chạy dưới dạng dịch vụ Windows; nếu không kết nối được, mở `services.msc`, tìm dịch vụ tên bắt đầu bằng `MySQL` và bấm Start.
3. **Thêm lệnh `mysql` vào PATH:** thư mục thường là `C:\Program Files\MySQL\MySQL Server <phiên bản>\bin` (xem đúng tên thư mục trong máy). Thêm vĩnh viễn qua *Edit the system environment variables → Environment Variables → Path → New*, rồi mở terminal mới. Chỉ dùng tạm trong cửa sổ hiện tại:
   ```powershell
   $env:Path += ";C:\Program Files\MySQL\MySQL Server 9.7\bin"
   mysql --version
   ```
4. **Tạo database và user:** mở PowerShell, đăng nhập MySQL bằng mật khẩu `root`:
   ```powershell
   mysql -u root -p
   ```
   Sau đó chạy SQL:
   ```sql
   CREATE DATABASE ve_may_bay CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   CREATE USER 'veapp'@'localhost' IDENTIFIED BY '<mật khẩu tự đặt>';
   GRANT ALL PRIVILEGES ON ve_may_bay.* TO 'veapp'@'localhost';
   ```
   Thoát MySQL bằng `exit`. Mật khẩu `veapp` do mỗi người tự đặt, không dùng chung và không commit.
5. **Lấy code:** dùng `git clone`, chuyển sang nhánh `develop`, rồi tạo nhánh làm việc từ đó như hướng dẫn chung.
6. **Đặt cấu hình trong PowerShell:** dùng cùng một cửa sổ cho các lệnh Flyway và chạy app. Đoạn dưới đây nhập mật khẩu ẩn, tạo JWT secret ngẫu nhiên và chỉ đặt biến cho cửa sổ hiện tại:
   ```powershell
   $env:DB_HOST = 'localhost'
   $env:DB_PORT = '3306'
   $env:DB_NAME = 've_may_bay'
   $env:DB_USERNAME = 'veapp'
   $securePassword = Read-Host 'Nhập mật khẩu MySQL của veapp' -AsSecureString
   $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
   try {
       $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
   } finally {
       [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
   }
   $env:FLYWAY_URL = 'jdbc:mysql://localhost:3306/ve_may_bay'
   $env:FLYWAY_USER = 'veapp'
   $env:FLYWAY_PASSWORD = $env:DB_PASSWORD
   $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
   $jwtBytes = New-Object byte[] 32
   $rng.GetBytes($jwtBytes)
   $env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
   $rng.Dispose()
   $env:COOKIE_SECURE = 'false'
   ```
7. **Kiểm tra và chạy Flyway:** `.\gradlew.bat flywayInfo` để xem trạng thái. Với database local mới tạo và chưa có bảng, chạy `.\gradlew.bat flywayMigrate`. Nếu database đã có dữ liệu hoặc Flyway history, dừng lại để kiểm tra và sao lưu; chỉ chạy `.\gradlew.bat flywayRepair` khi đã được duyệt.
8. **Chạy ứng dụng:** `.\gradlew.bat bootRun`. Thành công khi log có `Started QuanlyvemaybayApplication`. Kiểm tra bảng bằng `mysql -u veapp -p ve_may_bay -e "SHOW TABLES;"`.

Các biến `$env:...` chỉ có hiệu lực trong cửa sổ PowerShell đó; cửa sổ mới phải thiết lập lại. Không lưu mật khẩu vào file trong repo. Nếu dùng **cmd**, cú pháp đặt biến khác; nếu dùng **Git Bash**, dùng lệnh trong phần macOS.

## Docker cho test (Testcontainers)

Testcontainers + MySQL là hướng đã chốt cho test nhưng **chưa triển khai** (task riêng, xem `07-chua-chot.md`), nên hiện tại chưa cần Docker. Khi task đó được làm, máy cần có **Docker** (Docker Desktop trên macOS/Windows); kiểm tra: `docker version`. Chạy app bình thường (`./gradlew bootRun`) không cần Docker.

## Lỗi thường gặp khi dựng môi trường

| Lỗi | Nguyên nhân và cách xử lý |
|---|---|
| `Access denied for user 'veapp'` | User chưa được tạo, hoặc `DB_PASSWORD` sai/chưa `export` trong cửa sổ Terminal đang chạy. Thử `mysql -u veapp -p ve_may_bay` để kiểm tra mật khẩu |
| Flyway báo checksum không khớp | Database đã áp dụng migration có nội dung khác source trong repo. Sao lưu database, xác minh schema và history, rồi xin duyệt cách đồng bộ; không tự chạy `repair` hoặc sửa history |
| Flyway báo bảng/cột không tồn tại | Kiểm tra database/schema đích và các migration đang chờ. Không chạy trực tiếp `schema.sql` vì file có lệnh `DROP TABLE` |
| `contextLoads() FAILED` khi `./gradlew build` | Test khởi động cả app nên hiện cần MySQL đang chạy và `DB_PASSWORD` đúng (sau khi chuyển sang Testcontainers thì cần Docker đang chạy). Chạy `./gradlew build -x test` nếu chỉ cần build |
| `No matching toolchains found ... Java 21` | Chưa cài JDK 21. Cài xong chạy `./gradlew --stop` |
| `mysql: command not found` (macOS) hoặc `'mysql' is not recognized` (Windows) | Chưa thêm thư mục `bin` của MySQL vào `PATH`; trên Windows nhớ mở terminal mới sau khi sửa |
| `export : The term 'export' is not recognized` (Windows) | `export` là lệnh của macOS/Git Bash. Trên PowerShell dùng `$env:DB_PASSWORD = '...'` |
| `Can't connect to MySQL server on 'localhost'` (Windows) | Dịch vụ MySQL chưa chạy: mở `services.msc` và Start dịch vụ `MySQL...` |

Agent **không tự chạy** `DROP DATABASE`, `DROP USER` hay xoá dữ liệu. Nếu cần làm lại database rỗng, hướng dẫn người dùng tự chạy lệnh.
