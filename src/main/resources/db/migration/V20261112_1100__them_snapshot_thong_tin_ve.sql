-- CHƯA THỰC THI: chỉ áp dụng sau khi xác minh schema MySQL, baseline và backup.
-- Giữ nullable để phiên bản ứng dụng cũ vẫn có thể ghi vé trong giai đoạn chuyển đổi.
ALTER TABLE ve_may_bay
    ADD COLUMN ho_ten_nguoi_di VARCHAR(100) NULL COMMENT 'Tên người đi riêng của vé',
    ADD COLUMN email_thanh_toan VARCHAR(100) NULL COMMENT 'Email snapshot riêng của vé, không thay email đặt chỗ';
