-- ============================================================
-- DỮ LIỆU MẪU KHỞI TẠO (data.sql) - Phiên bản v2.2
-- ============================================================

USE `quanlyvemaybay`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. SÂN BAY
INSERT INTO `san_bay` (`ma_san_bay`, `ten_san_bay`, `thanh_pho`, `quoc_gia`) VALUES
('DAD', 'Sân bay Quốc tế Đà Nẵng', 'Đà Nẵng', 'Việt Nam'),
('SGN', 'Sân bay Quốc tế Tân Sơn Nhất', 'TP Hồ Chí Minh', 'Việt Nam'),
('HAN', 'Sân bay Quốc tế Nội Bài', 'Hà Nội', 'Việt Nam'),
('HPH', 'Sân bay Quốc tế Cát Bi', 'Hải Phòng', 'Việt Nam'),
('CXR', 'Sân bay Quốc tế Cam Ranh', 'Khánh Hòa', 'Việt Nam')
ON DUPLICATE KEY UPDATE `ten_san_bay` = VALUES(`ten_san_bay`);

-- 2. HÃNG HÀNG KHÔNG
INSERT INTO `hang_hang_khong` (`ma_hang`, `ten_hang`, `logo_url`, `trang_thai`) VALUES
('VN', 'Vietnam Airlines', '/assets/logos/vietnam-airlines.png', 'HOAT_DONG'),
('VJ', 'Vietjet Air', '/assets/logos/vietjet.png', 'HOAT_DONG'),
('BL', 'Jetstar Pacific', '/assets/logos/jetstar.png', 'HOAT_DONG'),
('QH', 'Bamboo Airways', '/assets/logos/bamboo.png', 'HOAT_DONG')
ON DUPLICATE KEY UPDATE `ten_hang` = VALUES(`ten_hang`);

-- 3. GÓI HÀNH LÝ KÝ GỬI MUA THÊM
INSERT INTO `goi_hanh_ly` (`id`, `so_kg`, `gia_tien`, `mo_ta`) VALUES
(1, 15, 170000.00, 'Mua thêm 15kg - 170.000 VNĐ'),
(2, 20, 220000.00, 'Mua thêm 20kg - 220.000 VNĐ'),
(3, 23, 260000.00, 'Mua thêm 23kg - 260.000 VNĐ'),
(4, 30, 350000.00, 'Mua thêm 30kg - 350.000 VNĐ')
ON DUPLICATE KEY UPDATE `gia_tien` = VALUES(`gia_tien`);

-- 4. DANH MỤC DỊCH VỤ CHUYẾN BAY (DICH_VU_CHUYEN_BAY)
INSERT INTO `dich_vu_chuyen_bay` (`id`, `ten_dich_vu`, `mo_ta`, `gia_tien`, `trang_thai`) VALUES
(1, 'Suất ăn nóng (Cơm gà sốt nấm)', 'Phục vụ suất ăn nóng kèm nước ngọt', 85000.00, 'HOAT_DONG'),
(2, 'Chọn chỗ ngồi phía trước', 'Chỗ ngồi rộng rãi, ưu tiên cất cánh/hạ cánh', 50000.00, 'HOAT_DONG'),
(3, 'Phòng chờ hạng thương gia (Lounge VIP)', 'Nghỉ ngơi tại phòng chờ VIP trước giờ bay', 350000.00, 'HOAT_DONG')
ON DUPLICATE KEY UPDATE `gia_tien` = VALUES(`gia_tien`);

-- 5. NGƯỜI DÙNG (vai_tro tập trung duy nhất)
INSERT INTO `nguoi_dung` (`id`, `tai_khoan_email`, `mat_khau`, `ho_ten`, `ngay_sinh`, `gioi_tinh`, `so_dien_thoai`, `dia_chi`, `vai_tro`, `trang_thai`) VALUES
(1, 'admin@phongve.com', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'Admin Quản Trị Viên', '1985-07-17', 'Nam', '0935847238', '295 Nguyễn Tất Thành, Quận Hải Châu, Đà Nẵng', 'ADMIN', 1),
(2, 'a.nguyen@phongve.com', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'Nguyễn Văn Nhân Viên', '1990-02-01', 'Nam', '0123456789', 'Hải Châu, Đà Nẵng', 'NHAN_VIEN', 1),
(3, 'nguyenvana@gmail.com', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'Nguyễn Văn A', '1985-07-17', 'Nam', '0935847238', '295 Nguyễn Tất Thành, Quận Hải Châu, Thành Phố Đà Nẵng', 'KHACH_HANG', 1),
(4, 'linh@gmail.com', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'Linh Doan', '1995-01-01', 'Nu', '0900000011', 'Đà Nẵng', 'KHACH_HANG', 1),
(5, 'offline_customer@gmail.com', NULL, 'Trần Văn Khách Tại Quầy', '1992-05-10', 'Nam', '0912345678', 'Hà Nội', 'KHACH_HANG', 1)
ON DUPLICATE KEY UPDATE `ho_ten` = VALUES(`ho_ten`);

-- 6. NHÂN VIÊN & KHÁCH HÀNG (Dùng nguoi_dung_id làm PK/FK)
INSERT INTO `nhan_vien` (`nguoi_dung_id`, `ma_nhan_vien`) VALUES
(1, 'ADMIN1'),
(2, 'NV1')
ON DUPLICATE KEY UPDATE `ma_nhan_vien` = VALUES(`ma_nhan_vien`);

INSERT INTO `khach_hang` (`nguoi_dung_id`, `cmnd_passport`, `quoc_tich`, `xep_hang`, `diem_tich_luy`) VALUES
(3, '201584723812', 'Việt Nam', 'Diamond', 1500),
(4, '205891234567', 'Việt Nam', 'Silver', 300),
(5, '201999888777', 'Việt Nam', 'Member', 0)
ON DUPLICATE KEY UPDATE `xep_hang` = VALUES(`xep_hang`);

-- 7. CHUYẾN BAY (Thời gian DATETIME)
INSERT INTO `chuyen_bay` (`id`, `ma_chuyen_bay`, `ma_hang`, `ma_san_bay_di`, `ma_san_bay_den`, `thoi_gian_khoi_hanh`, `thoi_gian_ha_canh`, `trang_thai`) VALUES
(1, 'VN236', 'VN', 'SGN', 'HAN', '2026-11-15 05:00:00', '2026-11-15 07:00:00', 'DUNG_GIO'),
(2, 'VN237', 'VN', 'HAN', 'SGN', '2026-11-20 07:00:00', '2026-11-20 09:00:00', 'DUNG_GIO'),
(3, 'VJ202', 'VJ', 'SGN', 'HAN', '2026-11-15 13:30:00', '2026-11-15 15:35:00', 'DUNG_GIO'),
(4, 'BL521', 'BL', 'DAD', 'SGN', '2026-11-15 06:30:00', '2026-11-15 08:30:00', 'DUNG_GIO'),
(5, 'BL520', 'BL', 'SGN', 'DAD', '2026-11-20 08:55:00', '2026-11-20 10:55:00', 'DUNG_GIO'),
(6, 'VJ999', 'VJ', 'HAN', 'SGN', '2026-11-25 23:30:00', '2026-11-26 01:45:00', 'DUNG_GIO')
ON DUPLICATE KEY UPDATE `ma_chuyen_bay` = VALUES(`ma_chuyen_bay`);

-- 8. HẠNG VÉ CHUYẾN BAY (Tách riêng hành lý xách tay 10kg, ký gửi 23kg)
INSERT INTO `hang_ve_chuyen_bay` (`id`, `chuyen_bay_id`, `loai_khoang`, `gia_goc`, `gia_ban`, `thue_phi`, `hanh_ly_xach_tay_kg`, `hanh_ly_ky_gui_kg`, `tong_so_ghe`, `so_ghe_trong`) VALUES
(1, 1, 'PHO_THONG', 1109000.00, 1109000.00, 681000.00, 10, 23, 150, 145),
(2, 1, 'THUONG_GIA', 2800000.00, 3100000.00, 681000.00, 14, 32, 20, 18),
(3, 2, 'PHO_THONG', 1109000.00, 1109000.00, 681000.00, 10, 23, 150, 142),
(4, 3, 'PHO_THONG', 950000.00, 1500000.00, 500000.00, 7, 20, 180, 170),
(5, 4, 'PHO_THONG', 4500000.00, 4600000.00, 0.00, 10, 20, 150, 140),
(6, 5, 'PHO_THONG', 4456000.00, 4556000.00, 0.00, 10, 20, 150, 140)
ON DUPLICATE KEY UPDATE `gia_ban` = VALUES(`gia_ban`);

-- 9. ĐẶT CHỖ (DAT_CHO)
INSERT INTO `dat_cho` (`id`, `ma_dat_cho`, `khach_hang_id`, `nhan_vien_id`, `email_nguoi_dat`, `so_dien_thoai_nguoi_dat`, `ho_ten_nguoi_dat`, `loai_chuyen_bay`, `so_nguoi_lon`, `so_tre_em`, `so_em_be`, `ngay_dat`, `tong_gia_goc`, `tong_gia_ban`, `trang_thai`) VALUES
(1, 'MH002', 3, 2, 'nguyenvana@gmail.com', '0935847238', 'Nguyễn Văn A', 'KHU_HOI', 2, 0, 0, '2026-11-09 10:00:00', 3800000.00, 4200000.00, 'CHO_THANH_TOAN'),
(2, 'MBSNHJ', 3, 2, 'van_a@gmail.com', '0935847238', 'VAN DIEN TRAN, QUANG SON CAO', 'KHU_HOI', 2, 0, 0, '2026-11-09 14:30:00', 8956000.00, 9156000.00, 'DA_THANH_TOAN')
ON DUPLICATE KEY UPDATE `tong_gia_ban` = VALUES(`tong_gia_ban`);

-- 10. HÀNH KHÁCH BAY (Bổ sung ngay_sinh & nguoi_lon_di_kem_id)
INSERT INTO `hanh_khach` (`id`, `dat_cho_id`, `ho_ten`, `ngay_sinh`, `gioi_tinh`, `email`, `so_dien_thoai`, `cmnd_passport`, `loai_hanh_khach`, `nguoi_lon_di_kem_id`) VALUES
(1, 1, 'Nguyễn Văn A', '1985-07-17', 'Nam', 'nguyenvana@gmail.com', '0903354560', '2158745354', 'NGUOI_LON', NULL),
(2, 1, 'Nguyễn Văn B', '1990-08-20', 'Nam', 'nguyenvanb@gmail.com', '0903354560', '2015478501', 'NGUOI_LON', NULL),
(3, 2, 'Trần Văn Điền', '1988-03-12', 'Nam', 'van_a@gmail.com', '0935847238', '201111222333', 'NGUOI_LON', NULL),
(4, 2, 'Cao Quang Sơn', '1992-11-25', 'Nam', 'son_cao@gmail.com', '0935847239', '201444555666', 'NGUOI_LON', NULL)
ON DUPLICATE KEY UPDATE `ho_ten` = VALUES(`ho_ten`);

-- 11. VÉ MÁY BAY CHI TIẾT (Chuẩn 3NF, bỏ chuyen_bay_id, liên kết qua hang_ve_id)
INSERT INTO `ve_may_bay` (`id`, `ma_ve`, `dat_cho_id`, `hanh_khach_id`, `hang_ve_id`, `goi_hanh_ly_id`, `chieu_bay`, `so_ghe`, `gia_goc`, `gia_ban`, `thue_phi`, `cuoc_hanh_ly`, `tong_gia`, `da_in_ve`, `trang_thai`) VALUES
-- Khách 1 (Nguyễn Văn A): Vé đi và Vé về
(1, 'DN001', 1, 1, 5, 1, 'CHIEU_DI', '42D', 1300000.00, 1500000.00, 0.00, 170000.00, 1670000.00, 1, 'CHO_THANH_TOAN'),
(2, 'HCM001', 1, 1, 6, 1, 'CHIEU_VE', '15A', 1300000.00, 1500000.00, 0.00, 170000.00, 1670000.00, 0, 'CHO_THANH_TOAN'),
-- Khách 2 (Nguyễn Văn B): Vé đi và Vé về
(3, 'DN002', 1, 2, 5, NULL, 'CHIEU_DI', '42E', 1300000.00, 1500000.00, 0.00, 0.00, 1500000.00, 0, 'CHO_THANH_TOAN'),
(4, 'HCM002', 1, 2, 6, NULL, 'CHIEU_VE', '15B', 1300000.00, 1500000.00, 0.00, 0.00, 1500000.00, 0, 'CHO_THANH_TOAN')
ON DUPLICATE KEY UPDATE `tong_gia` = VALUES(`tong_gia`);

-- 12. CHI TIẾT ĐẶT DỊCH VỤ THEO VÉ (VE_DICH_VU)
INSERT INTO `ve_dich_vu` (`id`, `ve_id`, `dich_vu_id`, `so_luong`, `thanh_tien`) VALUES
(1, 1, 1, 1, 85000.00), -- Khách A mua suất ăn nóng cho vé chiều đi DN001
(2, 1, 3, 1, 350000.00) -- Khách A mua phòng chờ VIP cho vé chiều đi DN001
ON DUPLICATE KEY UPDATE `thanh_tien` = VALUES(`thanh_tien`);

-- 13. GIAO DỊCH THANH TOÁN (Mặc định CHO_XU_LY, cập nhật qua IPN/Webhook)
INSERT INTO `thanh_toan` (`id`, `ma_giao_dich`, `dat_cho_id`, `phuong_thuc`, `so_tien`, `thoi_gian_thanh_toan`, `ma_giao_dich_doi_tac`, `trang_thai`, `ghi_chu`) VALUES
(1, 'PAY-MBSNHJ-001', 2, 'PAYPAL', 9156000.00, '2026-11-09 14:35:00', 'PAYPAL-TXN-99887766', 'THANH_CONG', 'Thanh toán thành công qua PayPal Checkout'),
(2, 'PAY-MH002-001', 1, 'VNPAY', 4200000.00, '2026-11-09 10:05:00', NULL, 'CHO_XU_LY', 'Đang chờ khách thanh toán qua cổng VNPAY')
ON DUPLICATE KEY UPDATE `so_tien` = VALUES(`so_tien`);

-- 14. BẢNG TOKEN XÁC THỰC (Băm token SHA-256, kiểm soát dùng 1 lần)
INSERT INTO `xac_thuc_token` (`id`, `user_id`, `token_hash`, `loai_token`, `da_su_dung`, `het_han`) VALUES
(1, 3, 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'XAC_THUC_EMAIL', 1, '2026-11-10 10:00:00'),
(2, 4, '73864f7b6058e37603c40217ec8eb222a76ae4cfbb66c9fc954b42c678a99479', 'QUEN_MAT_KHAU', 0, '2026-11-12 12:00:00')
ON DUPLICATE KEY UPDATE `da_su_dung` = VALUES(`da_su_dung`);

-- 15. NHẬT KÝ HỆ THỐNG (AUDIT_LOG)
INSERT INTO `audit_log` (`id`, `user_id`, `hanh_dong`, `chi_tiet`, `thoi_gian`, `ip`) VALUES
(1, 1, 'DANG_NHAP', 'Quản trị viên đăng nhập hệ thống', '2026-11-09 08:00:00', '192.168.1.10'),
(2, 2, 'LAP_DON_HANG', 'Nhân viên tạo đơn đặt chỗ PNR MBSNHJ tại quầy', '2026-11-09 14:30:00', '192.168.1.15')
ON DUPLICATE KEY UPDATE `hanh_dong` = VALUES(`hanh_dong`);

-- 16. TIN TỨC & BÀI VIẾT (TIN_TUC - Đầy đủ loai_tin theo menu Figma và trang_thai)
INSERT INTO `tin_tuc` (`id`, `nguoi_dung_id`, `loai_tin`, `tieu_de`, `noi_dung`, `anh_dai_dien`, `trang_thai`, `thoi_gian_tao`) VALUES
(1, 1, 'KHUYEN_MAI', 'Chào hè rực rỡ - Giảm ngay 20% các chặng bay nội địa', 'Chương trình áp dụng từ 01/06 đến 31/08 cho tất cả các chặng bay của Vietnam Airlines và Vietjet Air.', '/assets/news/promo1.jpg', 'HIEN_THI', '2026-11-01 08:00:00'),
(2, 1, 'QUY_DINH_HANH_LY', 'Quy định hành lý mới nhất năm 2026', 'Chi tiết các quy định về vật phẩm nguy hiểm, kích thước hành lý xách tay và miễn cước ký gửi.', '/assets/news/baggage.jpg', 'HIEN_THI', '2026-11-05 09:30:00'),
(3, 1, 'GIOI_THIEU', 'Giới thiệu về Phòng vé máy bay DTU Airlines', 'Hệ thống cung cấp dịch vụ đặt vé trực tuyến và hỗ trợ chuyên nghiệp 24/7.', '/assets/news/intro.jpg', 'HIEN_THI', '2026-11-06 10:00:00'),
(4, 1, 'THONG_BAO', 'Thông báo bảo trì hệ thống cổng thanh toán', 'Hệ thống bảo trì định kỳ từ 00:00 đến 02:00 ngày 15/11.', '/assets/news/notice.jpg', 'AN', '2026-11-08 14:00:00')
ON DUPLICATE KEY UPDATE `tieu_de` = VALUES(`tieu_de`);

-- 17. ĐIỂM ĐẾN DU LỊCH (DIEM_DEN)
INSERT INTO `diem_den` (`id`, `ten_diem_den`, `ma_san_bay`, `mo_ta`, `hinh_anh`) VALUES
(1, 'Bà Nà Hills & Cầu Vàng', 'DAD', 'Thánh địa du lịch nổi tiếng với khung cảnh mây trời ngoạn mục.', '/assets/destinations/bana.jpg'),
(2, 'Phố cổ Hội An', 'DAD', 'Di sản văn hóa thế giới với vẻ đẹp cổ kính ven sông Hoài.', '/assets/destinations/hoian.jpg'),
(3, 'Hồ Hoàn Kiếm & Phố Cổ Hà Nội', 'HAN', 'Trái tim của thủ đô nghìn năm văn hiến.', '/assets/destinations/hanoi.jpg')
ON DUPLICATE KEY UPDATE `ten_diem_den` = VALUES(`ten_diem_den`);

-- 18. TIN NHẮN TƯ VẤN (TIN_NHAN_CHAT)
INSERT INTO `tin_nhan_chat` (`id`, `nguoi_gui_id`, `nguoi_nhan_id`, `noi_dung`, `gui_luc`, `da_doc`) VALUES
(1, 3, 2, 'Chào bạn, cho mình hỏi vé khứ hồi Đà Nẵng - Sài Gòn đã bao gồm hành lý ký gửi chưa?', '2026-11-09 15:00:00', 1),
(2, 2, 3, 'Dạ chào anh A, hạng vé Phổ thông của anh đã bao gồm 10kg xách tay và 23kg ký gửi tiêu chuẩn ạ.', '2026-11-09 15:02:00', 1)
ON DUPLICATE KEY UPDATE `noi_dung` = VALUES(`noi_dung`);

SET FOREIGN_KEY_CHECKS = 1;
