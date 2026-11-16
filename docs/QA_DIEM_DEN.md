# QA trường thông tin điểm đến

Phạm vi: màn hình 99.7.1 danh sách và 99.7.3 thêm điểm đến. Bảng dưới ghi lại hợp đồng đã có trong `CreateDestinationRequest`, `DestinationResponse` và bảng `diem_den`, được giữ nguyên trong lần tích hợp FE/BE này. Không coi việc kiểm tra kỹ thuật là xác nhận nội dung thiết kế từ QA/nghiệp vụ.

| Trường | Quy tắc hiện có | Cách nhập hoặc hiển thị |
| --- | --- | --- |
| `tenDiemDen` | Bắt buộc, tối đa 100 ký tự | Chuẩn hoá khoảng trắng; không trùng tên trong cùng sân bay, không phân biệt hoa thường theo truy vấn hiện có |
| `maSanBay` | Bắt buộc, tối đa 10 ký tự; sân bay phải tồn tại | Chọn từ API danh mục sân bay; lưu mã viết hoa |
| `moTa` | Không bắt buộc, tối đa 5000 ký tự | Văn bản, khoảng trắng ở đầu/cuối được loại bỏ; rỗng lưu null |
| `hinhAnh` | Không bắt buộc, tối đa 255 ký tự | Đường dẫn từ gốc `/path` hoặc URL HTTP/HTTPS có host; không nhận protocol-relative, tài khoản/mật khẩu trong URL, ký tự điều khiển, dấu backslash hoặc đoạn quay lui `.`/`..` (kể cả mã hoá) |
| `tenSanBay`, `thanhPho`, `quocGia` | Đọc từ danh mục sân bay | Hiển thị trong danh sách, không là trường tạo mới độc lập |
| `id` | Database sinh tự động | Không nhập trong form |

Không bổ sung trường, cột hoặc migration mới. User yêu cầu QA xác nhận lại các item màn hình thêm điểm đến trước khi chốt cấu trúc dữ liệu; hiện chưa có biên bản QA nghiệp vụ xác nhận các trường trong 99.7.3 được cung cấp cùng task. Vì vậy, cấu trúc trên là hợp đồng API đang có, không phải cấu trúc mới tự suy diễn từ SRS.

## API tích hợp

- `GET /api/v1/destinations/airports`: công khai; `ApiResponse<List<AirportSummaryDto>>` gồm `maSanBay`, `tenSanBay`, `thanhPho`, `quocGia`, sắp theo thành phố rồi mã sân bay.
- `GET /api/v1/destinations`: công khai; `keyword` tối đa 100 ký tự, `maSanBay` tối đa 10, `page >= 0`, `size` từ 1 tới 100 (mặc định 10). Phản hồi `ApiResponse<PageResponse<DestinationResponse>>`.
- `POST /api/v1/destinations`: ADMIN/NHAN_VIEN có CSRF; thành công 201, trùng điểm đến 409 `DESTINATION_ALREADY_EXISTS`, sân bay không tồn tại 404 `AIRPORT_NOT_FOUND`, sai trường 400 `VALIDATION_ERROR` với `details` theo tên trường.
- Giao dịch tạo mới khoá ghi dòng sân bay trước khi kiểm tra trùng và lưu. Hai request tạo điểm đến cùng sân bay được xử lý lần lượt; mọi đường ghi khác cần tuân thủ cùng cơ chế vì database hiện chưa có unique key trên cặp tên/sân bay.

## Checklist QA

- [x] Đối chiếu tên trường và giới hạn giữa DTO, entity và schema hiện có.
- [x] Kiểm thử lấy danh mục sân bay từ repository, phân trang và giới hạn bộ lọc.
- [x] Kiểm thử normalize tên/mã sân bay, trùng lặp và thứ tự khoá trước kiểm tra trùng.
- [x] Kiểm thử định dạng ảnh hợp lệ/không hợp lệ và lỗi field `hinhAnh` qua controller.
- [x] Kiểm thử GET công khai; POST yêu cầu đăng nhập, vai trò ADMIN/NHAN_VIEN và CSRF.
- [ ] QA/nghiệp vụ xác nhận tên hiển thị, bắt buộc/tuỳ chọn và nguồn hình ảnh trên bản thiết kế 99.7.3.
- [ ] Kiểm thử tích hợp FE/BE tạo thành công, tìm kiếm/lọc/phân trang và hai request trùng đồng thời trên database QA riêng.
