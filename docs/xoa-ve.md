# API hủy vé chưa thanh toán

`DELETE /api/v1/ve-may-bay/{id}` dành cho `ADMIN` hoặc `NHAN_VIEN` đã đăng nhập. Khi dùng cookie `access_token`, client phải gửi thêm cookie `XSRF-TOKEN` và header `X-XSRF-TOKEN` tương ứng.

API chỉ nhận vé có `trangThai = CHO_THANH_TOAN` thuộc đặt chỗ cũng đang `CHO_THANH_TOAN`. Thành công trả HTTP 200 với `id`, `maVe`, `trangThai = DA_HUY`. Vé không tồn tại trả 404; vé đã hủy hoặc đã thanh toán, hoặc đặt chỗ không còn chờ thanh toán, trả 409.

Vé được hủy mềm để giữ liên kết với đặt chỗ, hành khách và dịch vụ; không xóa bản ghi hoặc thay đổi snapshot giá và trạng thái đặt chỗ. SRS v1.2 chỉ cho nhân viên xóa vé chưa thanh toán; schema hiện tại không có trạng thái vé đã sử dụng.
