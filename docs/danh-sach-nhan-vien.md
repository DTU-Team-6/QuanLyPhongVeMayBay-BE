# API danh sách nhân viên

API chưa có xác thực và phân quyền; mặc định endpoint bị tắt. Không bật trong môi trường có thể truy cập công khai hoặc sử dụng dữ liệu thật. Chỉ bật tạm trong môi trường kiểm thử biệt lập bằng `APP_NHAN_VIEN_ALLOW_UNSECURED=true`. Cần tích hợp xác thực và kiểm tra quyền ADMIN trước khi sử dụng thực tế.

`GET /api/v1/nhan-vien` trả danh sách nhân viên đang hoạt động, gồm cả quản trị viên có bản ghi nhân viên, và không trả mật khẩu. `trang` bắt đầu từ 0; `kichThuoc` từ 1 đến 100, mặc định là 20.

Để tìm kiếm, truyền đồng thời `tieuChi` và `tuKhoa` (tối đa 40 ký tự). Các tiêu chí là `MA_NHAN_VIEN`, `HO_TEN`, `EMAIL`, `SO_DIEN_THOAI`. Kết quả rỗng là HTTP 200 với danh sách rỗng. Lỗi tham số trả HTTP 400 theo RFC 9457.
