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
   Database phải **rỗng** trước lần chạy đầu: Flyway sẽ tự tạo bảng, không chạy tay file SQL.
4. **Lấy code:** `git clone` repo, `git checkout develop`, rồi tạo nhánh làm việc từ đó.
5. **Chạy app** (trong cùng một cửa sổ Terminal, vì `export` chỉ có hiệu lực ở cửa sổ đó):
   ```bash
   export DB_PASSWORD='<mật khẩu của bạn>'
   ./gradlew bootRun
   ```
   Thành công khi log có `Successfully applied 2 migrations` và `Started QuanlyvemaybayApplication`.
6. **Kiểm tra:** `mysql -u veapp -p ve_may_bay -e "SHOW TABLES;"` phải ra 24 bảng (23 bảng dự án + `flyway_schema_history`).

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
   $env:DB_PASSWORD = '<mật khẩu của bạn>'
   .\gradlew bootRun
   ```
   Nếu dùng **cmd** thay vì PowerShell: `set DB_PASSWORD=<mật khẩu>` (không có dấu nháy, vì cmd giữ nguyên dấu nháy làm một phần mật khẩu) rồi `gradlew bootRun`. Nếu dùng **Git Bash** thì làm đúng như macOS (`export ...`, `./gradlew bootRun`).
5. Biến `$env:DB_PASSWORD` chỉ có hiệu lực trong cửa sổ PowerShell đó, mở cửa sổ mới phải đặt lại. Muốn cố định thì đặt bằng *Environment Variables* của Windows, nhưng chỉ làm trên máy cá nhân, không bao giờ ghi vào file trong repo.

## Docker cho test (Testcontainers)

Testcontainers + MySQL là hướng đã chốt cho test nhưng **chưa triển khai** (task riêng, xem `07-chua-chot.md`), nên hiện tại chưa cần Docker. Khi task đó được làm, máy cần có **Docker** (Docker Desktop trên macOS/Windows); kiểm tra: `docker version`. Chạy app bình thường (`./gradlew bootRun`) không cần Docker.

## Lỗi thường gặp khi dựng môi trường

| Lỗi | Nguyên nhân và cách xử lý |
|---|---|
| `Access denied for user 'veapp'` | User chưa được tạo, hoặc `DB_PASSWORD` sai/chưa `export` trong cửa sổ Terminal đang chạy. Thử `mysql -u veapp -p ve_may_bay` để kiểm tra mật khẩu |
| `Found non-empty schema(s)... without schema history table` | Database đã có bảng tạo tay. Xoá và tạo lại database rỗng |
| `Migration checksum mismatch` | File migration đã chạy bị sửa sau khi chạy (V1/V2 hoặc file khác). Khi dev: xoá và tạo lại database. Về lâu dài: không sửa file cũ, thêm file migration mới (đặt version theo thời gian, xem `03-database.md`) |
| `Detected resolved migration not applied to database` | Có file migration mới nhưng version nhỏ hơn file đã chạy (thường do merge nhánh cũ). File chưa merge thì đổi tên sang thời gian mới. Không tự bật `out-of-order`, hỏi nhóm trước |
| `contextLoads() FAILED` khi `./gradlew build` | Test khởi động cả app nên hiện cần MySQL đang chạy và `DB_PASSWORD` đúng (sau khi chuyển sang Testcontainers thì cần Docker đang chạy). Chạy `./gradlew build -x test` nếu chỉ cần build |
| `No matching toolchains found ... Java 21` | Chưa cài JDK 21. Cài xong chạy `./gradlew --stop` |
| `mysql: command not found` (macOS) hoặc `'mysql' is not recognized` (Windows) | Chưa thêm thư mục `bin` của MySQL vào `PATH`; trên Windows nhớ mở terminal mới sau khi sửa |
| `export : The term 'export' is not recognized` (Windows) | `export` là lệnh của macOS/Git Bash. Trên PowerShell dùng `$env:DB_PASSWORD = '...'` |
| `Can't connect to MySQL server on 'localhost'` (Windows) | Dịch vụ MySQL chưa chạy: mở `services.msc` và Start dịch vụ `MySQL...` |

Agent **không tự chạy** `DROP DATABASE`, `DROP USER` hay xoá dữ liệu. Nếu cần làm lại database rỗng, hướng dẫn người dùng tự chạy lệnh.
