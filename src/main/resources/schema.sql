-- ============================================================
-- HỆ THỐNG QUẢN LÝ PHÒNG BÁN VÉ MÁY BAY
-- Database DDL Script (schema.sql) - Phiên bản chuẩn hóa v2.2
-- Cập nhật:
-- 1. thanh_toan.trang_thai mặc định là CHO_XU_LY
-- 2. xac_thuc_token có da_su_dung và lưu token_hash bảo mật
-- 3. tin_tuc có loai_tin (Menu Figma) và trang_thai (HIEN_THI / AN)
-- 4. Nâng độ dài so_dien_thoai lên VARCHAR(20) hỗ trợ chuẩn E.164 (+)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `quanlyvemaybay` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `quanlyvemaybay`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `tin_nhan_chat`;
DROP TABLE IF EXISTS `tin_tuc`;
DROP TABLE IF EXISTS `diem_den`;
DROP TABLE IF EXISTS `ve_dich_vu`;
DROP TABLE IF EXISTS `dich_vu_chuyen_bay`;
DROP TABLE IF EXISTS `audit_log`;
DROP TABLE IF EXISTS `xac_thuc_token`;
DROP TABLE IF EXISTS `thanh_toan`;
DROP TABLE IF EXISTS `ve_may_bay`;
DROP TABLE IF EXISTS `hanh_khach`;
DROP TABLE IF EXISTS `goi_hanh_ly`;
DROP TABLE IF EXISTS `dat_cho`;
DROP TABLE IF EXISTS `hang_ve_chuyen_bay`;
DROP TABLE IF EXISTS `chuyen_bay`;
DROP TABLE IF EXISTS `nhan_vien`;
DROP TABLE IF EXISTS `khach_hang`;
DROP TABLE IF EXISTS `nguoi_dung`;
DROP TABLE IF EXISTS `hang_hang_khong`;
DROP TABLE IF EXISTS `san_bay`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. BẢNG SÂN BAY (SAN_BAY)
CREATE TABLE `san_bay` (
    `ma_san_bay` VARCHAR(10) NOT NULL COMMENT 'Mã IATA sân bay (DAD, SGN, HAN...)',
    `ten_san_bay` VARCHAR(100) NOT NULL COMMENT 'Tên sân bay',
    `thanh_pho` VARCHAR(100) NOT NULL COMMENT 'Tỉnh/Thành phố',
    `quoc_gia` VARCHAR(100) NOT NULL DEFAULT 'Việt Nam',
    PRIMARY KEY (`ma_san_bay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG HÃNG HÀNG KHÔNG (HANG_HANG_KHONG)
CREATE TABLE `hang_hang_khong` (
    `ma_hang` VARCHAR(10) NOT NULL COMMENT 'Mã hãng (VN, VJ, QH, BL...)',
    `ten_hang` VARCHAR(100) NOT NULL COMMENT 'Vietnam Airlines, Vietjet Air...',
    `logo_url` VARCHAR(255) DEFAULT NULL,
    `trang_thai` ENUM('HOAT_DONG', 'TAM_NGUNG') NOT NULL DEFAULT 'HOAT_DONG',
    PRIMARY KEY (`ma_hang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG NGƯỜI DÙNG (NGUOI_DUNG)
CREATE TABLE `nguoi_dung` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tai_khoan_email` VARCHAR(100) NOT NULL COMMENT 'Email đăng nhập hệ thống',
    `mat_khau` VARCHAR(100) DEFAULT NULL COMMENT 'Mật khẩu băm (NULL nếu nhân viên tạo khách hàng tại quầy)',
    `ho_ten` VARCHAR(100) NOT NULL COMMENT 'Họ và tên',
    `ngay_sinh` DATE DEFAULT NULL,
    `gioi_tinh` ENUM('Nam', 'Nu', 'Khac') NOT NULL DEFAULT 'Khac',
    `so_dien_thoai` VARCHAR(20) DEFAULT NULL COMMENT 'Chuẩn quốc tế E.164 (+84...)',
    `dia_chi` VARCHAR(255) DEFAULT NULL,
    `anh_dai_dien` VARCHAR(255) DEFAULT NULL,
    `vai_tro` ENUM('ADMIN', 'NHAN_VIEN', 'KHACH_HANG') NOT NULL COMMENT 'Vai trò duy nhất toàn hệ thống',
    `trang_thai` TINYINT NOT NULL DEFAULT 1 COMMENT '1: Hoạt động, 0: Tạm khóa/Chờ kích hoạt',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_nguoidung_email` (`tai_khoan_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG KHÁCH HÀNG (KHACH_HANG) - SUBTYPE CỦA NGUOI_DUNG
CREATE TABLE `khach_hang` (
    `nguoi_dung_id` BIGINT NOT NULL,
    `cmnd_passport` VARCHAR(20) DEFAULT NULL COMMENT 'Số CMND/CCCD hoặc Hộ chiếu',
    `quoc_tich` VARCHAR(50) NOT NULL DEFAULT 'Việt Nam',
    `xep_hang` ENUM('Member', 'Silver', 'Gold', 'Diamond') NOT NULL DEFAULT 'Member',
    `diem_tich_luy` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`nguoi_dung_id`),
    CONSTRAINT `fk_khachhang_nguoidung` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG NHÂN VIÊN (NHAN_VIEN) - SUBTYPE CỦA NGUOI_DUNG
CREATE TABLE `nhan_vien` (
    `nguoi_dung_id` BIGINT NOT NULL,
    `ma_nhan_vien` VARCHAR(20) NOT NULL COMMENT 'Mã NV quản lý (NV1, NV2, ADMIN1...)',
    PRIMARY KEY (`nguoi_dung_id`),
    UNIQUE KEY `uk_nhanvien_manv` (`ma_nhan_vien`),
    CONSTRAINT `fk_nhanvien_nguoidung` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG CHUYẾN BAY (CHUYEN_BAY) - THỜI GIAN BAY DATETIME HỖ TRỢ QUA ĐÊM
CREATE TABLE `chuyen_bay` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ma_chuyen_bay` VARCHAR(20) NOT NULL COMMENT 'Số hiệu chuyến bay (VN236, VJ202...)',
    `ma_hang` VARCHAR(10) NOT NULL COMMENT 'Hãng bay khai thác',
    `ma_san_bay_di` VARCHAR(10) NOT NULL COMMENT 'Sân bay cất cánh',
    `ma_san_bay_den` VARCHAR(10) NOT NULL COMMENT 'Sân bay hạ cánh',
    `thoi_gian_khoi_hanh` DATETIME NOT NULL COMMENT 'Ngày giờ cất cánh',
    `thoi_gian_ha_canh` DATETIME NOT NULL COMMENT 'Ngày giờ hạ cánh (hỗ trợ qua đêm)',
    `trang_thai` ENUM('DUNG_GIO', 'DELAY', 'DA_HUY') NOT NULL DEFAULT 'DUNG_GIO',
    PRIMARY KEY (`id`),
    KEY `idx_chuyenbay_search` (`ma_san_bay_di`, `ma_san_bay_den`, `thoi_gian_khoi_hanh`, `trang_thai`),
    CONSTRAINT `fk_chuyenbay_hang` FOREIGN KEY (`ma_hang`) REFERENCES `hang_hang_khong` (`ma_hang`) ON UPDATE CASCADE,
    CONSTRAINT `fk_chuyenbay_sb_di` FOREIGN KEY (`ma_san_bay_di`) REFERENCES `san_bay` (`ma_san_bay`) ON UPDATE CASCADE,
    CONSTRAINT `fk_chuyenbay_sb_den` FOREIGN KEY (`ma_san_bay_den`) REFERENCES `san_bay` (`ma_san_bay`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. BẢNG HẠNG VÉ CHUYẾN BAY (HANG_VE_CHUYEN_BAY) - TÁCH RIÊNG 2 CỘT HÀNH LÝ
CREATE TABLE `hang_ve_chuyen_bay` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `chuyen_bay_id` BIGINT NOT NULL,
    `loai_khoang` ENUM('PHO_THONG', 'THUONG_GIA') NOT NULL DEFAULT 'PHO_THONG',
    `gia_goc` DECIMAL(12,2) NOT NULL COMMENT 'Giá gốc nhập từ hãng',
    `gia_ban` DECIMAL(12,2) NOT NULL COMMENT 'Giá bán niêm yết',
    `thue_phi` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Thuế và phí sân bay',
    `hanh_ly_xach_tay_kg` INT NOT NULL DEFAULT 10 COMMENT 'Hành lý xách tay miễn cước (10kg)',
    `hanh_ly_ky_gui_kg` INT NOT NULL DEFAULT 23 COMMENT 'Hành lý ký gửi miễn cước (23kg)',
    `tong_so_ghe` INT NOT NULL DEFAULT 150,
    `so_ghe_trong` INT NOT NULL DEFAULT 150,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chuyenbay_khoang` (`chuyen_bay_id`, `loai_khoang`),
    CONSTRAINT `fk_hangve_chuyenbay` FOREIGN KEY (`chuyen_bay_id`) REFERENCES `chuyen_bay` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_ghe_hop_le` CHECK (`so_ghe_trong` >= 0 AND `so_ghe_trong` <= `tong_so_ghe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. BẢNG GÓI HÀNH LÝ KÝ GỬI MUA THÊM (GOI_HANH_LY)
CREATE TABLE `goi_hanh_ly` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `so_kg` INT NOT NULL COMMENT '15, 20, 23, 30 kg',
    `gia_tien` DECIMAL(12,2) NOT NULL COMMENT 'Cước phí mua thêm',
    `mo_ta` VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. BẢNG ĐẶT CHỖ (DAT_CHO) - SNAPSHOT ĐƠN HÀNG LỊCH SỬ
CREATE TABLE `dat_cho` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ma_dat_cho` VARCHAR(10) NOT NULL COMMENT 'Mã PNR đặt chỗ 6-10 ký tự',
    `khach_hang_id` BIGINT DEFAULT NULL COMMENT 'NULL nếu là khách vãng lai mua tại quầy',
    `nhan_vien_id` BIGINT DEFAULT NULL COMMENT 'Nhân viên lập phiếu (NULL nếu khách tự đặt)',
    `email_nguoi_dat` VARCHAR(100) NOT NULL,
    `so_dien_thoai_nguoi_dat` VARCHAR(20) NOT NULL COMMENT 'Chuẩn quốc tế E.164 (+84...)',
    `ho_ten_nguoi_dat` VARCHAR(100) NOT NULL,
    `loai_chuyen_bay` ENUM('MOT_CHIEU', 'KHU_HOI') NOT NULL DEFAULT 'MOT_CHIEU',
    `so_nguoi_lon` INT NOT NULL DEFAULT 1 COMMENT 'Snapshot số người lớn',
    `so_tre_em` INT NOT NULL DEFAULT 0 COMMENT 'Snapshot số trẻ em',
    `so_em_be` INT NOT NULL DEFAULT 0 COMMENT 'Snapshot số em bé',
    `ngay_dat` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `tong_gia_goc` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT 'Snapshot tổng giá gốc mua hãng',
    `tong_gia_ban` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT 'Snapshot tổng tiền bán cho khách',
    `trang_thai` ENUM('CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DA_HUY') NOT NULL DEFAULT 'CHO_THANH_TOAN',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_datcho_mapnr` (`ma_dat_cho`),
    KEY `idx_datcho_khachhang` (`khach_hang_id`, `ngay_dat`),
    KEY `idx_datcho_baocao` (`ngay_dat`, `trang_thai`, `nhan_vien_id`),
    CONSTRAINT `fk_datcho_khachhang` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang` (`nguoi_dung_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_datcho_nhanvien` FOREIGN KEY (`nhan_vien_id`) REFERENCES `nhan_vien` (`nguoi_dung_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. BẢNG HÀNH KHÁCH BAY (HANH_KHACH)
CREATE TABLE `hanh_khach` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `dat_cho_id` BIGINT NOT NULL COMMENT 'Thuộc đơn đặt chỗ nào',
    `ho_ten` VARCHAR(100) NOT NULL COMMENT 'Họ và tên hành khách',
    `ngay_sinh` DATE NOT NULL COMMENT 'Bắt buộc để kiểm tra độ tuổi Trẻ em/Em bé',
    `gioi_tinh` ENUM('Nam', 'Nu', 'Khac') NOT NULL DEFAULT 'Nam',
    `email` VARCHAR(100) DEFAULT NULL,
    `so_dien_thoai` VARCHAR(20) DEFAULT NULL COMMENT 'Chuẩn quốc tế E.164 (+84...)',
    `cmnd_passport` VARCHAR(20) DEFAULT NULL COMMENT 'Bắt buộc với người lớn; Trẻ em/Em bé để NULL',
    `loai_hanh_khach` ENUM('NGUOI_LON', 'TRE_EM', 'EM_BE') NOT NULL DEFAULT 'NGUOI_LON',
    `nguoi_lon_di_kem_id` BIGINT DEFAULT NULL COMMENT 'Self-FK trỏ tới người lớn bảo hộ em bé / trẻ em',
    PRIMARY KEY (`id`),
    KEY `idx_hanhkhach_datcho` (`dat_cho_id`),
    KEY `idx_hanhkhach_nguoilon` (`nguoi_lon_di_kem_id`),
    CONSTRAINT `fk_hanhkhach_datcho` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_hanhkhach_nguoilon` FOREIGN KEY (`nguoi_lon_di_kem_id`) REFERENCES `hanh_khach` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. BẢNG VÉ MÁY BAY CHI TIẾT (VE_MAY_BAY) - CHUẨN 3NF (BỎ chuyen_bay_id, so_ghe NULL CHO EM BÉ)
CREATE TABLE `ve_may_bay` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ma_ve` VARCHAR(20) NOT NULL COMMENT 'Mã vé định danh (DN001, HCM001...)',
    `dat_cho_id` BIGINT NOT NULL COMMENT 'Thuộc đơn đặt chỗ',
    `hanh_khach_id` BIGINT NOT NULL COMMENT 'Thuộc về hành khách nào',
    `hang_ve_id` BIGINT NOT NULL COMMENT 'Hạng vé khoang chuyến bay (đã chứa chuyen_bay_id)',
    `goi_hanh_ly_id` INT DEFAULT NULL COMMENT 'Gói hành lý ký gửi mua thêm cho chiều này',
    `chieu_bay` ENUM('CHIEU_DI', 'CHIEU_VE') NOT NULL DEFAULT 'CHIEU_DI',
    `so_ghe` VARCHAR(10) DEFAULT NULL COMMENT 'Vị trí ghế (NULL nếu là em bé ngồi cùng người lớn)',
    `gia_goc` DECIMAL(12,2) NOT NULL COMMENT 'Snapshot giá gốc',
    `gia_ban` DECIMAL(12,2) NOT NULL COMMENT 'Snapshot giá bán',
    `thue_phi` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Snapshot thuế phí',
    `cuoc_hanh_ly` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Snapshot cước phí hành lý mua thêm',
    `tong_gia` DECIMAL(12,2) NOT NULL COMMENT 'Snapshot tổng giá = gia_ban + thue_phi + cuoc_hanh_ly',
    `da_in_ve` BOOLEAN NOT NULL DEFAULT FALSE,
    `trang_thai` ENUM('CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DA_HUY') NOT NULL DEFAULT 'CHO_THANH_TOAN',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ve_mave` (`ma_ve`),
    KEY `idx_ve_datcho` (`dat_cho_id`),
    KEY `idx_ve_hanhkhach` (`hanh_khach_id`),
    KEY `idx_ve_hangve` (`hang_ve_id`),
    CONSTRAINT `fk_ve_datcho` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ve_hanhkhach` FOREIGN KEY (`hanh_khach_id`) REFERENCES `hanh_khach` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ve_hangve` FOREIGN KEY (`hang_ve_id`) REFERENCES `hang_ve_chuyen_bay` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_ve_hanhly` FOREIGN KEY (`goi_hanh_ly_id`) REFERENCES `goi_hanh_ly` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. BẢNG DANH MỤC DỊCH VỤ CHUYẾN BAY (DICH_VU_CHUYEN_BAY)
CREATE TABLE `dich_vu_chuyen_bay` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ten_dich_vu` VARCHAR(100) NOT NULL COMMENT 'Suất ăn nóng, Chọn chỗ ngồi sớm, Lounge VIP...',
    `mo_ta` TEXT DEFAULT NULL,
    `gia_tien` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `trang_thai` ENUM('HOAT_DONG', 'TAM_NGUNG') NOT NULL DEFAULT 'HOAT_DONG',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. BẢNG TRUNG GIAN ĐẶT DỊCH VỤ THEO VÉ (VE_DICH_VU)
CREATE TABLE `ve_dich_vu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ve_id` BIGINT NOT NULL COMMENT 'Gắn với vé máy bay cụ thể (chiều đi hoặc chiều về)',
    `dich_vu_id` BIGINT NOT NULL COMMENT 'Thuộc dịch vụ nào trong danh mục',
    `so_luong` INT NOT NULL DEFAULT 1,
    `thanh_tien` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Snapshot giá tiền dịch vụ',
    PRIMARY KEY (`id`),
    KEY `idx_vedichvu_ve` (`ve_id`),
    KEY `idx_vedichvu_dichvu` (`dich_vu_id`),
    CONSTRAINT `fk_vedichvu_ve` FOREIGN KEY (`ve_id`) REFERENCES `ve_may_bay` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_vedichvu_dichvu` FOREIGN KEY (`dich_vu_id`) REFERENCES `dich_vu_chuyen_bay` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. BẢNG GIAO DỊCH THANH TOÁN (THANH_TOAN) - MẶC ĐỊNH LÀ CHO_XU_LY
CREATE TABLE `thanh_toan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ma_giao_dich` VARCHAR(50) NOT NULL COMMENT 'Mã giao dịch nội bộ',
    `dat_cho_id` BIGINT NOT NULL,
    `phuong_thuc` ENUM('PAYPAL', 'VNPAY', 'MOMO', 'THE_NOI_DIA', 'TIEN_MAT') NOT NULL DEFAULT 'PAYPAL',
    `so_tien` DECIMAL(14,2) NOT NULL,
    `thoi_gian_thanh_toan` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ma_giao_dich_doi_tac` VARCHAR(100) DEFAULT NULL COMMENT 'Mã phản hồi từ cổng thanh toán đối tác',
    `trang_thai` ENUM('CHO_XU_LY', 'THANH_CONG', 'THAT_BAI', 'DA_HOAN_TIEN') NOT NULL DEFAULT 'CHO_XU_LY' COMMENT 'Mặc định là CHO_XU_LY, cập nhật qua IPN/Webhook',
    `ghi_chu` TEXT DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_thanhtoan_magd` (`ma_giao_dich`),
    KEY `idx_thanhtoan_datcho` (`dat_cho_id`),
    CONSTRAINT `fk_thanhtoan_datcho` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. BẢNG TOKEN XÁC THỰC & BẢO MẬT (XAC_THUC_TOKEN) - BĂM TOKEN & KIỂM SOÁT DÙNG 1 LẦN
CREATE TABLE `xac_thuc_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `token_hash` CHAR(64) NOT NULL COMMENT 'Lưu SHA-256 dạng hex của token gốc (không lưu token rõ)',
    `loai_token` ENUM('XAC_THUC_EMAIL', 'QUEN_MAT_KHAU') NOT NULL,
    `da_su_dung` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Chống replay attack: Mỗi token chỉ dùng 1 lần',
    `het_han` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_token_user` (`user_id`),
    CONSTRAINT `fk_token_nguoidung` FOREIGN KEY (`user_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. BẢNG NHẬT KÝ HỆ THỐNG (AUDIT_LOG)
CREATE TABLE `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT 'Người dùng thực hiện (FK)',
    `hanh_dong` VARCHAR(100) NOT NULL COMMENT 'LOGIN, THEM_NHAN_VIEN, XOA_VE, HUY_DAT_CHO...',
    `chi_tiet` TEXT DEFAULT NULL,
    `thoi_gian` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_audit_user` (`user_id`),
    KEY `idx_audit_thoigian` (`thoi_gian`),
    CONSTRAINT `fk_audit_nguoidung` FOREIGN KEY (`user_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. BẢNG TIN TỨC & BÀI VIẾT (TIN_TUC) - CÓ PHÂN LOẠI THEO MENU FIGMA VÀ TRẠNG THÁI HIỂN THỊ
CREATE TABLE `tin_tuc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nguoi_dung_id` BIGINT NOT NULL COMMENT 'Tác giả bài viết (Admin / Nhân viên)',
    `loai_tin` ENUM('KHUYEN_MAI', 'QUY_DINH_HANH_LY', 'GIOI_THIEU', 'THONG_BAO') NOT NULL DEFAULT 'KHUYEN_MAI' COMMENT 'Phân loại theo menu website',
    `tieu_de` VARCHAR(255) NOT NULL,
    `noi_dung` LONGTEXT NOT NULL,
    `anh_dai_dien` VARCHAR(255) DEFAULT NULL,
    `trang_thai` ENUM('HIEN_THI', 'AN') NOT NULL DEFAULT 'HIEN_THI' COMMENT 'Trạng thái hiển thị bài viết',
    `thoi_gian_tao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_tintuc_loaitin` (`loai_tin`, `trang_thai`),
    KEY `idx_tintuc_tacgia` (`nguoi_dung_id`),
    CONSTRAINT `fk_tintuc_nguoidung` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. BẢNG ĐIỂM ĐẾN DU LỊCH (DIEM_DEN)
CREATE TABLE `diem_den` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ten_diem_den` VARCHAR(100) NOT NULL COMMENT 'Bà Nà Hills, Phố cổ Hội An, Vịnh Hạ Long...',
    `ma_san_bay` VARCHAR(10) NOT NULL COMMENT 'Sân bay lân cận',
    `mo_ta` TEXT DEFAULT NULL,
    `hinh_anh` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_diemden_sanbay` (`ma_san_bay`),
    CONSTRAINT `fk_diemden_sanbay` FOREIGN KEY (`ma_san_bay`) REFERENCES `san_bay` (`ma_san_bay`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. BẢNG TIN NHẮN TƯ VẤN (TIN_NHAN_CHAT)
CREATE TABLE `tin_nhan_chat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nguoi_gui_id` BIGINT NOT NULL,
    `nguoi_nhan_id` BIGINT NOT NULL,
    `noi_dung` TEXT NOT NULL,
    `gui_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `da_doc` BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    KEY `idx_chat_gui` (`nguoi_gui_id`),
    KEY `idx_chat_nhan` (`nguoi_nhan_id`),
    CONSTRAINT `fk_chat_nguoigui` FOREIGN KEY (`nguoi_gui_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_chat_nguoinhan` FOREIGN KEY (`nguoi_nhan_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
