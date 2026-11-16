# Tiến độ nhánh codex/report-integration

## Task hiện tại

- Chức năng: hoàn thiện số liệu API báo cáo và tích hợp FE (task 2).
- Nhánh: `codex/report-integration`, bắt đầu từ `origin/develop`.
- Cập nhật: 16/11/2026, agent BE.

## Đã xong (chưa commit)

- KPI tổng số đơn của báo cáo theo hãng dùng COUNT DISTINCT toàn bộ tập lọc để đơn nhiều hãng chỉ được tính một lần.
- Khi lọc hãng trong báo cáo theo nhân viên hoặc thời gian, chỉ cộng doanh thu/giá gốc của vé thuộc hãng đó; giữ đúng bộ lọc ngày, nhân viên và trạng thái.
- Bổ sung kiểm tra mã hãng tối đa 10 ký tự và ID nhân viên phải dương.
- Thêm regression tests cho đơn nhiều hãng, tổng tiền lọc hãng theo nhân viên, cả ba kỳ ngày/tháng/năm và lỗi bộ lọc API.
- Ghi rõ hợp đồng ngày/trạng thái/cách tổng hợp số tiền tại `agents-docs/be/04-nghiep-vu-srs.md`.
- Targeted tests báo cáo/thống kê: BUILD SUCCESSFUL 34 giây.
- Full `gradlew.bat build` sau cùng: BUILD SUCCESSFUL 51 giây.

## Đang dở

- Parent kiểm tra API thực với fixture đơn nhiều hãng trên MySQL QA riêng; không sửa dữ liệu dev.
- Chờ phản biện độc lập trước commit/push.

## Việc tiếp theo

1. Parent xác nhận số liệu trên API và FE bằng fixture QA.
2. Khắc phục finding nếu có, commit riêng task 2.
3. Xoá file tiến độ trước mở PR theo quy tắc repo; push task 2 trước khi bắt đầu task 3.

## Quyết định và lưu ý

- Không thêm dependency hoặc sửa schema/migration.
- Giữ semantics có sẵn: ngày báo cáo là ngày đặt, mặc định đơn/vé đã thanh toán; ngày kết thúc được tính đủ cả ngày.
- Không lọc hãng giữ tổng tiền ở cấp đơn cho báo cáo theo nhân viên/thời gian; lọc hãng lấy tiền từ vé tương ứng. Báo cáo theo hãng luôn lấy tiền ở cấp vé.
- SRS chưa định nghĩa ngày thanh toán hoặc công thức doanh thu mới; không tự đổi các quy tắc ngoài phạm vi sửa lỗi.

## Lỗi đang gặp

- Không có lỗi build ở lần chạy cuối.

- QA reviewer approved. Real isolated MySQL fixture: 9 API checks (3 types x ALL/VN/VJ) PASS: 3m/1m/2m VND, 1 distinct booking, 2/1 tickets. Reversed date400 PASS.
