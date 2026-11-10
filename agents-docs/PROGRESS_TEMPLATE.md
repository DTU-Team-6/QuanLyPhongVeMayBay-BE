# Mẫu tiến độ cho từng nhánh

> **Cách dùng:** mỗi nhánh làm việc có **một file riêng** `progress/<ten-nhanh>.md` ở thư mục gốc repo. Tên nhánh thay `/` bằng `-` (nhánh `feature/tim-chuyen-bay` → `progress/feature-tim-chuyen-bay.md`). Mỗi nhánh một file nên khi merge không bị conflict.
> File được commit trên chính nhánh đó, để agent khác (kể cả ở máy khác) checkout nhánh là đọc được.
> Agent cập nhật sau **mỗi chức năng nhỏ**, không đợi đến cuối. Chỉ ghi những gì đã xảy ra hoặc đã chốt, không ghi phỏng đoán.
> **Trước khi mở Pull Request:** xoá file này trong commit cuối (`git rm progress/<ten-nhanh>.md`) để không đưa file tiến độ vào `develop`.

## Task hiện tại

- Chức năng: (ví dụ: tìm chuyến bay)
- Nhánh: (ví dụ: `feature/tim-chuyen-bay`)
- Cập nhật lần cuối: (ngày giờ, agent nào)

## Đã xong (đã commit)

- (ví dụ: entity `ChuyenBay`, repository)

## Đang dở (chưa commit hoặc chưa chạy được)

- (ví dụ: controller `GET /api/v1/chuyen-bay` mới có khung, chưa validate)

## Việc tiếp theo (theo thứ tự)

1. (việc 1)
2. (việc 2)
3. Chạy build/chạy thử rồi commit

## Quyết định đã chốt và lưu ý

- (ví dụ: nhóm vừa chốt dùng JWT)

## Lỗi đang gặp (nếu có)

- (dán nguyên văn thông báo lỗi và lệnh gây ra lỗi)
