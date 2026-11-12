# API danh sách vé

`GET /api/v1/ve-may-bay` trả danh sách từng vé với mã vé, tên người đi, mã đặt chỗ, mã chuyến bay, sân bay đi/đến, ngày đi, tổng giá bán và trạng thái. Không trả `gia_goc`, thông tin tài khoản hoặc giấy tờ hành khách.

Tham số tùy chọn: `trangThai` (`CHO_THANH_TOAN`, `DA_THANH_TOAN`, `DA_HUY`); `trang` bắt đầu từ 0; `kichThuoc` từ 1 đến 100, mặc định 20. Muốn tìm kiếm phải truyền cùng lúc `tieuChi` và `tuKhoa` (tối đa 40 ký tự). `tieuChi` nhận `MA_VE`, `MA_DAT_CHO`, `TEN_NGUOI_DI`, `MA_CHUYEN_BAY`. Mặc định trả tất cả trạng thái; không có kết quả trả HTTP 200 với `content: []`. Tham số sai trả HTTP 400 theo RFC 9457.

Ví dụ thử trong Postman: `GET http://localhost:8080/api/v1/ve-may-bay?trangThai=DA_THANH_TOAN&tieuChi=MA_DAT_CHO&tuKhoa=TESTPNR1&trang=0&kichThuoc=20` (không có body).

Endpoint yêu cầu đăng nhập với vai trò `ADMIN` hoặc `NHAN_VIEN`. Không cần cấu hình bật riêng.

Theo quyết định cho mục 6.6.1.6, danh sách gồm mọi trạng thái vé, tìm theo bốn tiêu chí trên và hiển thị `ve_may_bay.tong_gia` thay cho `gia_goc`.
