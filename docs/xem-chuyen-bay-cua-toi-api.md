# API xem chuyến bay của tôi không cần đăng nhập

SRS v1.2 không nêu phương thức xác minh cho chức năng này. Người dùng đã chốt nhập **mã đặt chỗ + email + số điện thoại** của cùng một đặt chỗ. API chỉ trả dữ liệu chuyến bay sau khi cả ba khớp; không thay schema và không kết nối database chung của nhóm khi thử.

## Hợp đồng API

`POST /api/v1/chuyen-bay/cua-toi/tra-cuu` được phép gọi không có JWT. Vì Spring Security vẫn bật CSRF, trước khi POST qua cookie client cần GET `/api/v1/auth/csrf`, giữ cookie `XSRF-TOKEN` và gửi cùng giá trị trong header `X-XSRF-TOKEN`.

Request JSON:

```json
{"maDatCho":"MH002","email":"nguyenvana@gmail.com","soDienThoai":"0935847238"}
```

Response HTTP 200 mẫu (rút gọn từ kết quả thật):

```json
{"statusCode":200,"message":"Tra cứu chuyến bay thành công","data":{"chuyenBay":[{"chuyenBayId":4,"maChuyenBay":"BL521","tenHangHangKhong":"Jetstar Pacific","sanBayDi":{"maSanBay":"DAD","tenSanBay":"Sân bay Quốc tế Đà Nẵng","thanhPho":"Đà Nẵng","quocGia":"Việt Nam"},"sanBayDen":{"maSanBay":"SGN","tenSanBay":"Sân bay Quốc tế Tân Sơn Nhất","thanhPho":"TP Hồ Chí Minh","quocGia":"Việt Nam"},"thoiGianKhoiHanh":{"dateTime":"2026-11-15T06:30:00+07:00","timeZone":"Asia/Ho_Chi_Minh"},"thoiGianHaCanh":{"dateTime":"2026-11-15T08:30:00+07:00","timeZone":"Asia/Ho_Chi_Minh"},"trangThaiChuyenBay":"DUNG_GIO"}]}}
```

API gộp các vé cùng chuyến bay thành một mục. Không trả tên hành khách, email, số điện thoại, mã vé, giá hoặc trạng thái thanh toán. Mã không tồn tại, email sai hoặc số điện thoại sai cùng trả HTTP 404 `BOOKING_LOOKUP_FAILED` với thông báo chung. Validation sai trả 400. Thiếu CSRF trả 403. Mỗi địa chỉ IP được tra cứu tối đa 10 lần trong 15 phút; lần tiếp theo trả 429 `LOOKUP_RATE_LIMITED`.

Giới hạn tra cứu hiện lưu trong bộ nhớ từng instance Spring Boot. Nếu triển khai nhiều instance, cần chuyển sang bộ giới hạn chia sẻ để giữ cùng chính sách trên toàn hệ thống. Schema hiện không lưu múi giờ IANA từng sân bay; API dùng mã sân bay quốc tế đã biết và mặc định `Asia/Ho_Chi_Minh` cho các sân bay còn lại, theo cách xử lý hiện có của module chuyến bay.

## Kiểm thử thực tế

Ngày 2026-11-12, Spring Boot chạy cổng 18083 với MySQL 8.4 container và database mới `ticket_task6_test`. Bản `schema.sql` thử nghiệm đã bỏ các câu `DROP TABLE`, `CREATE DATABASE`, `USE`; migration snapshot Task 3 chỉ được chạy trên database thử nghiệm này. Không dùng database có dữ liệu của nhóm.

`gradlew build --no-daemon --rerun-tasks`: BUILD SUCCESSFUL, 124 test, 0 lỗi, 0 bỏ qua.

- Không đăng nhập, nhập đúng ba thông tin của `MH002`: HTTP 200; sau khi gộp vé có hai chuyến `BL521`, `BL520`. SQL trên test DB xác nhận hai chuyến thuộc đúng đặt chỗ này.
- Sai mã, sai email, sai số điện thoại: đều HTTP 404 `BOOKING_LOOKUP_FAILED`.
- Thiếu CSRF: HTTP 403. Email/số điện thoại sai định dạng: HTTP 400.
- Sau 10 lần tra cứu cùng IP, lần thứ 11: HTTP 429 `LOOKUP_RATE_LIMITED`.
- Dữ liệu mẫu có mã `MH002` dài 5 ký tự, nên validation không áp đặt độ dài tối thiểu không phù hợp với dữ liệu hiện có.
