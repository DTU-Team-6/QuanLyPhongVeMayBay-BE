# API Chat với Admin

Backend chỉ dùng REST và bảng `tin_nhan_chat` hiện có. File SRS v1.2 hiện không có đặc tả Chat chi tiết; API này đáp ứng yêu cầu gửi/nhận, lưu lịch sử và phân quyền trong nhiệm vụ 99.3. Không có WebSocket, SSE hoặc Frontend.

## Endpoint

| Vai trò | Method | URL | Ý nghĩa |
|---|---|---|---|
| Khách hàng | POST | `/api/v1/chat/messages` | Gửi tới Admin đang hoạt động đầu tiên theo ID |
| Khách hàng | GET | `/api/v1/chat/messages?trang=0&kichThuoc=20` | Xem tin của chính mình với Admin |
| Admin | GET | `/api/v1/chat/conversations?trang=0&kichThuoc=20` | Danh sách khách có hội thoại |
| Admin | GET | `/api/v1/chat/conversations/{khachHangId}/messages` | Lịch sử với một khách |
| Admin | POST | `/api/v1/chat/conversations/{khachHangId}/messages` | Trả lời một khách |

Request POST:

```json
{"noiDung":"Xin chào Admin, tôi cần hỗ trợ vé DN001."}
```

Response gửi thành công (HTTP 201):

```json
{"statusCode":201,"message":"Gửi tin nhắn thành công","data":{"id":3,"nguoiGuiId":3,"nguoiGuiVaiTro":"KHACH_HANG","nguoiNhanId":1,"noiDung":"Xin chào Admin, tôi cần hỗ trợ vé DN001.","guiLuc":"2026-11-12T04:58:52.177744500Z"}}
```

GET lịch sử trả `ApiResponse<PageResponse<TinNhanChatResponse>>`, mới nhất trước. Trường `nguoiGuiVaiTro` phân biệt tin khách và Admin. Tin nhắn được lưu thời gian UTC. Nội dung bắt buộc, tối đa 2000 ký tự. Đăng nhập bằng JWT cookie `access_token` hoặc Bearer token. Khi dùng cookie, POST cần `X-XSRF-TOKEN` lấy từ `GET /api/v1/auth/csrf`; sau một lần POST có thể cần lấy CSRF token mới trước lần POST tiếp theo.

Khách chỉ đọc lịch sử theo ID trong JWT, không nhận ID khách từ request. Đường dẫn quản trị kiểm tra vai trò `ADMIN` ở Spring Security và service. Không trả lịch sử khách này cho khách khác. Trạng thái `da_doc` sẵn có trong schema chưa được cập nhật vì yêu cầu hiện chưa mô tả quy tắc đánh dấu đã đọc.

## Kết quả thực tế trên database thử nghiệm

Ngày 2026-11-12, Spring Boot chạy cổng 18082 trên database mới `ticket_task5_test` trong MySQL 8.4 container chỉ dùng để thử. Schema được nạp sau khi loại bỏ `DROP TABLE`, `CREATE DATABASE`, `USE`; hai migration snapshot từ develop được chạy **chỉ trên database thử nghiệm**. Không kết nối database hiện có của nhóm.

`gradlew build --no-daemon --rerun-tasks`: BUILD SUCCESSFUL, 125 test, 0 lỗi, 0 bỏ qua.

- Khách đăng nhập 200, gửi tin 201 và xem lịch sử 200.
- Admin đăng nhập 200, xem danh sách hội thoại 200, xem lịch sử 200, trả lời 201.
- Khách xem lại phản hồi của Admin: 200, có 2 tin trong hội thoại.
- MySQL xác nhận hai tin mới có đúng `nguoi_gui_id`, `nguoi_nhan_id`, nội dung UTF-8.
- Khách khác xem lịch sử riêng: 200 và `content: []`; gọi đường dẫn lịch sử Admin của khách đầu tiên: 403.
- Không đăng nhập: 401; nhân viên không phải Admin: 403; POST không có CSRF: 403; nội dung trống: 400.
- Tham số `trang=-1`: HTTP 400 với `code: VALIDATION_ERROR`.
