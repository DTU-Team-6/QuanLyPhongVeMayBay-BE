# API danh sách vé

`GET /api/v1/ve-may-bay` trả danh sách từng vé với mã vé, tên người đi, mã đặt chỗ, mã chuyến bay, sân bay đi/đến, ngày đi, tổng giá bán và trạng thái. Không trả `gia_goc`, thông tin tài khoản hoặc giấy tờ hành khách.

Tham số tùy chọn: `trangThai` (`CHO_THANH_TOAN`, `DA_THANH_TOAN`, `DA_HUY`); `trang` bắt đầu từ 0; `kichThuoc` từ 1 đến 100, mặc định 20. Muốn tìm kiếm phải truyền cùng lúc `tieuChi` và `tuKhoa` (tối đa 40 ký tự). `tieuChi` nhận `MA_VE`, `MA_DAT_CHO`, `TEN_NGUOI_DI`, `MA_CHUYEN_BAY`. Mặc định trả tất cả trạng thái; không có kết quả trả HTTP 200 với `content: []`. Tham số sai trả HTTP 400 theo RFC 9457.

Ví dụ thử trong Postman: `GET http://localhost:8080/api/v1/ve-may-bay?trangThai=DA_THANH_TOAN&tieuChi=MA_DAT_CHO&tuKhoa=TESTPNR1&trang=0&kichThuoc=20` (không có body).

Endpoint mặc định tắt vì chưa có xác thực và phân quyền nhân viên. Chỉ bật tạm trên database thử nghiệm biệt lập bằng `APP_VE_ALLOW_UNSECURED=true` và giới hạn lắng nghe ở `SERVER_ADDRESS=127.0.0.1`. Không bật trên dữ liệu thật hoặc môi trường công khai.

Theo quyết định cho mục 6.6.1.6, danh sách gồm mọi trạng thái vé, tìm theo bốn tiêu chí trên và hiển thị `ve_may_bay.tong_gia` thay cho `gia_goc`.
