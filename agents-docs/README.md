# Tài liệu hướng dẫn agent – QuanLyPhongVeMayBay (DTU-Team-6)

Hai file `AGENTS.md` (backend) và `AGENTS_FE.md` (frontend) được tách nhỏ theo chủ đề để dễ tìm và dễ sửa.

## Cách tổ chức thư mục

```
<gốc repo>/
├── AGENTS.md              # repo BE (ở repo FE: đổi tên AGENTS_FE.md thành AGENTS.md)
├── AGENTS_FE.md           # bản dành cho repo FE
└── agents-docs/
    ├── README.md          # file này
    ├── PROGRESS_TEMPLATE.md
    ├── be/                # 01 … 07
    └── fe/                # 01 … 06
```

**Quy ước tham chiếu:** trong các file con, tên file không kèm thư mục (ví dụ `03-database.md` trong một file của `be/`) là file cùng thư mục với file đang đọc. Tham chiếu sang bên kia luôn ghi đủ đường dẫn (`agents-docs/fe/02-ket-noi-backend.md`). Vì backend và frontend là hai repo riêng, khi một quyết định thuộc phần chung (xác thực, lỗi API, giờ bay...) được chốt thì **phải cập nhật ở cả hai bên**.

## File dùng chung

| File | Nội dung |
|---|---|
| [AGENTS.md](../AGENTS.md) | Bản ngắn cho repo BE, trỏ tới `agents-docs/be/` |
| [AGENTS_FE.md](../AGENTS_FE.md) | Bản ngắn cho repo FE (đổi tên thành `AGENTS.md` khi đặt vào repo FE), trỏ tới `agents-docs/fe/` |
| [PROGRESS_TEMPLATE.md](PROGRESS_TEMPLATE.md) | Mẫu file tiến độ, mỗi nhánh một file `progress/<ten-nhanh>.md` để agent khác làm tiếp khi agent trước bị ngắt |

## Backend (`be/`)

| File | Nội dung |
|---|---|
| [01-stack-va-lenh.md](be/01-stack-va-lenh.md) | Stack (kể cả Jackson 3, `flyway-mysql`), lệnh Gradle, thông tin database |
| [02-dung-moi-truong.md](be/02-dung-moi-truong.md) | Dựng môi trường macOS/Windows, bảng lỗi thường gặp |
| [03-database.md](be/03-database.md) | Flyway (version theo thời gian), quy ước đặt tên bảng, ánh xạ JPA, chống bán trùng ghế |
| [04-nghiep-vu-srs.md](be/04-nghiep-vu-srs.md) | Nghiệp vụ từ SRS: `gia_goc`, vai trò, báo cáo, lỗi ghế 409, tách DTO, PayPal |
| [05-bao-mat-va-cau-truc.md](be/05-bao-mat-va-cau-truc.md) | Bảo mật, quy ước đặt tên class và API, cấu trúc package |
| [06-git-va-quy-trinh.md](be/06-git-va-quy-trinh.md) | Quy tắc Git và cách làm việc theo từng chức năng nhỏ |
| [07-chua-chot.md](be/07-chua-chot.md) | Quyết định đã chốt (cookie/CSRF, email, Cloudinary, endpoint xác thực, lỗi RFC 9457, giờ bay UTC, refresh token, test) và các chi tiết cần đặc tả khi triển khai |

## Frontend (`fe/`)

| File | Nội dung |
|---|---|
| [01-stack-va-lenh.md](fe/01-stack-va-lenh.md) | Stack React + Vite + Tailwind CSS + React Router, fetch, state React, lệnh npm |
| [02-ket-noi-backend.md](fe/02-ket-noi-backend.md) | `VITE_API_BASE_URL`, PayPal, Cloudinary signed upload, xác thực và CORS |
| [03-man-hinh-va-nghiep-vu.md](fe/03-man-hinh-va-nghiep-vu.md) | Phạm vi màn hình theo SRS, nghiệp vụ giao diện, xử lý lỗi ghế 409 |
| [04-cau-truc-code.md](fe/04-cau-truc-code.md) | Cấu trúc code theo chức năng, React Router, API client, Tailwind CSS và Cloudinary upload |
| [05-git-va-quy-trinh.md](fe/05-git-va-quy-trinh.md) | Quy tắc Git và cách làm việc theo từng chức năng nhỏ |
| [06-dung-moi-truong.md](fe/06-dung-moi-truong.md) | Dựng môi trường FE (Node, npm), lỗi thường gặp, cảnh báo không chạy lại `npm create vite` |
