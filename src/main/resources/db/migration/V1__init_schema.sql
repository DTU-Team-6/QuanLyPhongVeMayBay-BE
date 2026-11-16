-- =====================================================================
-- V1: BASELINE theo đúng cấu trúc database hiện có (23 bảng) – MySQL 8.0+
-- Sinh từ mysqldump của database ve_may_bay; đã:
--   * sắp lại bảng theo thứ tự phụ thuộc khoá ngoại (dump xếp theo ABC nên không chạy thẳng được)
--   * bỏ AUTO_INCREMENT=n và COLLATE của bảng (lấy mặc định của database)
-- Chạy bằng Flyway trên database RỖNG (không có CREATE DATABASE / USE ở đây).
-- =====================================================================

CREATE TABLE `hang_bay` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ma_hang` varchar(5) NOT NULL,
  `ten_hang` varchar(100) NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_hang` (`ma_hang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hang_thanh_vien` (
  `id` tinyint NOT NULL AUTO_INCREMENT,
  `ten_hang` varchar(30) NOT NULL,
  `diem_toi_thieu` int NOT NULL DEFAULT '0',
  `ty_le_giam` decimal(5,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ten_hang` (`ten_hang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `khoang` (
  `id` tinyint NOT NULL AUTO_INCREMENT,
  `ma_khoang` varchar(20) NOT NULL,
  `ten_khoang` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_khoang` (`ma_khoang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `may_bay` (
  `id` int NOT NULL AUTO_INCREMENT,
  `so_dang_ky` varchar(20) NOT NULL,
  `loai_may_bay` varchar(50) NOT NULL,
  `hang_bay_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `so_dang_ky` (`so_dang_ky`),
  KEY `fk_mb_hang` (`hang_bay_id`),
  CONSTRAINT `fk_mb_hang` FOREIGN KEY (`hang_bay_id`) REFERENCES `hang_bay` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ghe` (
  `id` int NOT NULL AUTO_INCREMENT,
  `may_bay_id` int NOT NULL,
  `so_ghe` varchar(5) NOT NULL,
  `khoang_id` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ghe` (`may_bay_id`,`so_ghe`),
  KEY `fk_ghe_khoang` (`khoang_id`),
  CONSTRAINT `fk_ghe_khoang` FOREIGN KEY (`khoang_id`) REFERENCES `khoang` (`id`),
  CONSTRAINT `fk_ghe_mb` FOREIGN KEY (`may_bay_id`) REFERENCES `may_bay` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `quoc_tich` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ma_iso` char(2) NOT NULL,
  `ten` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_iso` (`ma_iso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `nguoi` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ho_ten` varchar(50) NOT NULL,
  `ngay_sinh` date DEFAULT NULL,
  `gioi_tinh` enum('NAM','NU','KHAC') DEFAULT NULL,
  `email` varchar(40) NOT NULL,
  `sdt` varchar(15) DEFAULT NULL,
  `dia_chi` varchar(250) DEFAULT NULL,
  `quoc_tich_id` int DEFAULT NULL,
  `so_giay_to` varchar(20) DEFAULT NULL,
  `loai` enum('KHACH_HANG','NHAN_VIEN') NOT NULL,
  `ngay_tao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_nguoi_quoc_tich` (`quoc_tich_id`),
  CONSTRAINT `fk_nguoi_quoc_tich` FOREIGN KEY (`quoc_tich_id`) REFERENCES `quoc_tich` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `khach_hang` (
  `nguoi_id` bigint NOT NULL,
  `loai_khach_hang` enum('CA_NHAN','DAI_LY') NOT NULL DEFAULT 'CA_NHAN',
  `hang_thanh_vien_id` tinyint DEFAULT NULL,
  `diem_tich_luy` int NOT NULL DEFAULT '0',
  `anh_dai_dien` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`nguoi_id`),
  KEY `fk_kh_hang` (`hang_thanh_vien_id`),
  CONSTRAINT `fk_kh_hang` FOREIGN KEY (`hang_thanh_vien_id`) REFERENCES `hang_thanh_vien` (`id`),
  CONSTRAINT `fk_kh_nguoi` FOREIGN KEY (`nguoi_id`) REFERENCES `nguoi` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `nhan_vien` (
  `nguoi_id` bigint NOT NULL,
  `ma_nhan_vien` varchar(10) NOT NULL,
  `trang_thai` enum('DANG_LAM','NGHI_VIEC') NOT NULL DEFAULT 'DANG_LAM',
  PRIMARY KEY (`nguoi_id`),
  UNIQUE KEY `ma_nhan_vien` (`ma_nhan_vien`),
  CONSTRAINT `fk_nv_nguoi` FOREIGN KEY (`nguoi_id`) REFERENCES `nguoi` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dat_cho` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ma_dat_cho` varchar(10) NOT NULL,
  `khach_hang_id` bigint NOT NULL,
  `nhan_vien_id` bigint DEFAULT NULL,
  `kenh_dat` enum('ONLINE','QUAY') NOT NULL,
  `loai_hanh_trinh` enum('MOT_CHIEU','KHU_HOI') NOT NULL,
  `email_thanh_toan` varchar(50) NOT NULL,
  `tong_gia_goc` decimal(15,2) NOT NULL DEFAULT '0.00',
  `tong_gia_ban` decimal(15,2) NOT NULL DEFAULT '0.00',
  `trang_thai` enum('CHO_THANH_TOAN','DA_THANH_TOAN','DA_HUY','HET_HAN') NOT NULL DEFAULT 'CHO_THANH_TOAN',
  `ngay_dat` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `han_thanh_toan` datetime DEFAULT NULL,
  `phien_ban` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_dat_cho` (`ma_dat_cho`),
  KEY `idx_dc_kh` (`khach_hang_id`,`ngay_dat`),
  KEY `idx_dc_nv` (`nhan_vien_id`,`ngay_dat`),
  CONSTRAINT `fk_dc_kh` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang` (`nguoi_id`),
  CONSTRAINT `fk_dc_nv` FOREIGN KEY (`nhan_vien_id`) REFERENCES `nhan_vien` (`nguoi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hanh_khach` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dat_cho_id` bigint NOT NULL,
  `loai_hanh_khach` enum('NGUOI_LON','TRE_EM','EM_BE') NOT NULL,
  `ho_ten` varchar(50) NOT NULL,
  `gioi_tinh` enum('NAM','NU','KHAC') NOT NULL,
  `email` varchar(40) DEFAULT NULL,
  `sdt` varchar(15) DEFAULT NULL,
  `so_giay_to` varchar(20) DEFAULT NULL,
  `nguoi_lon_di_kem_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_hk_dc` (`dat_cho_id`),
  KEY `fk_hk_nl` (`nguoi_lon_di_kem_id`),
  CONSTRAINT `fk_hk_dc` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hk_nl` FOREIGN KEY (`nguoi_lon_di_kem_id`) REFERENCES `hanh_khach` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lich_su_diem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `khach_hang_id` bigint NOT NULL,
  `dat_cho_id` bigint DEFAULT NULL,
  `so_diem` int NOT NULL,
  `ly_do` varchar(200) DEFAULT NULL,
  `ngay_tao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_diem_kh` (`khach_hang_id`),
  KEY `fk_diem_dc` (`dat_cho_id`),
  CONSTRAINT `fk_diem_dc` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`),
  CONSTRAINT `fk_diem_kh` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang` (`nguoi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `san_bay` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ma_iata` char(3) NOT NULL,
  `ten_san_bay` varchar(150) NOT NULL,
  `thanh_pho` varchar(100) NOT NULL,
  `quoc_gia` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_iata` (`ma_iata`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `chuyen_bay` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ma_chuyen_bay` varchar(10) NOT NULL,
  `hang_bay_id` int NOT NULL,
  `may_bay_id` int NOT NULL,
  `san_bay_di_id` int NOT NULL,
  `san_bay_den_id` int NOT NULL,
  `gio_di` datetime NOT NULL,
  `gio_den` datetime NOT NULL,
  `trang_thai` enum('MO_BAN','DONG_BAN','HUY','DA_BAY') NOT NULL DEFAULT 'MO_BAN',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cb` (`ma_chuyen_bay`,`gio_di`),
  KEY `idx_cb_tim_kiem` (`san_bay_di_id`,`san_bay_den_id`,`gio_di`),
  KEY `fk_cb_hang` (`hang_bay_id`),
  KEY `fk_cb_mb` (`may_bay_id`),
  KEY `fk_cb_den` (`san_bay_den_id`),
  CONSTRAINT `fk_cb_den` FOREIGN KEY (`san_bay_den_id`) REFERENCES `san_bay` (`id`),
  CONSTRAINT `fk_cb_di` FOREIGN KEY (`san_bay_di_id`) REFERENCES `san_bay` (`id`),
  CONSTRAINT `fk_cb_hang` FOREIGN KEY (`hang_bay_id`) REFERENCES `hang_bay` (`id`),
  CONSTRAINT `fk_cb_mb` FOREIGN KEY (`may_bay_id`) REFERENCES `may_bay` (`id`),
  CONSTRAINT `ck_cb_gio` CHECK ((`gio_den` > `gio_di`)),
  CONSTRAINT `ck_cb_san_bay` CHECK ((`san_bay_di_id` <> `san_bay_den_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `gia_chuyen_bay` (
  `chuyen_bay_id` bigint NOT NULL,
  `khoang_id` tinyint NOT NULL,
  `gia_goc` decimal(15,2) NOT NULL,
  `gia_niem_yet` decimal(15,2) NOT NULL,
  `mien_cuoc_kg` smallint NOT NULL DEFAULT '0',
  PRIMARY KEY (`chuyen_bay_id`,`khoang_id`),
  KEY `fk_gia_khoang` (`khoang_id`),
  CONSTRAINT `fk_gia_cb` FOREIGN KEY (`chuyen_bay_id`) REFERENCES `chuyen_bay` (`id`),
  CONSTRAINT `fk_gia_khoang` FOREIGN KEY (`khoang_id`) REFERENCES `khoang` (`id`),
  CONSTRAINT `ck_gia` CHECK (((`gia_goc` >= 0) and (`gia_niem_yet` >= `gia_goc`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `thanh_toan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `khach_hang_id` bigint NOT NULL,
  `phuong_thuc` enum('PAYPAL','TIEN_MAT','CHUYEN_KHOAN') NOT NULL DEFAULT 'PAYPAL',
  `paypal_order_id` varchar(64) DEFAULT NULL,
  `tong_tien` decimal(15,2) NOT NULL,
  `tien_te` char(3) NOT NULL DEFAULT 'USD',
  `trang_thai` enum('KHOI_TAO','THANH_CONG','THAT_BAI','HOAN_TIEN') NOT NULL DEFAULT 'KHOI_TAO',
  `thoi_gian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `paypal_order_id` (`paypal_order_id`),
  KEY `fk_tt_kh` (`khach_hang_id`),
  CONSTRAINT `fk_tt_kh` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang` (`nguoi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `thanh_toan_dat_cho` (
  `thanh_toan_id` bigint NOT NULL,
  `dat_cho_id` bigint NOT NULL,
  `so_tien` decimal(15,2) NOT NULL,
  PRIMARY KEY (`thanh_toan_id`,`dat_cho_id`),
  KEY `fk_ttdc_dc` (`dat_cho_id`),
  CONSTRAINT `fk_ttdc_dc` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`),
  CONSTRAINT `fk_ttdc_tt` FOREIGN KEY (`thanh_toan_id`) REFERENCES `thanh_toan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tin_khuyen_mai` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tieu_de` varchar(200) NOT NULL,
  `noi_dung` text NOT NULL,
  `anh_banner` varchar(255) DEFAULT NULL,
  `ngay_bat_dau` date DEFAULT NULL,
  `ngay_ket_thuc` date DEFAULT NULL,
  `nhan_vien_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_km_nv` (`nhan_vien_id`),
  CONSTRAINT `fk_km_nv` FOREIGN KEY (`nhan_vien_id`) REFERENCES `nhan_vien` (`nguoi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `vai_tro` (
  `id` tinyint NOT NULL,
  `ma_vai_tro` varchar(20) NOT NULL,
  `ten` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_vai_tro` (`ma_vai_tro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tai_khoan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ten_dang_nhap` varchar(40) NOT NULL,
  `mat_khau_hash` varchar(100) NOT NULL,
  `vai_tro_id` tinyint NOT NULL,
  `nguoi_id` bigint DEFAULT NULL,
  `da_xac_thuc_email` tinyint(1) NOT NULL DEFAULT '0',
  `trang_thai` enum('HOAT_DONG','KHOA') NOT NULL DEFAULT 'HOAT_DONG',
  `ngay_tao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ten_dang_nhap` (`ten_dang_nhap`),
  UNIQUE KEY `nguoi_id` (`nguoi_id`),
  KEY `fk_tk_vai_tro` (`vai_tro_id`),
  CONSTRAINT `fk_tk_nguoi` FOREIGN KEY (`nguoi_id`) REFERENCES `nguoi` (`id`),
  CONSTRAINT `fk_tk_vai_tro` FOREIGN KEY (`vai_tro_id`) REFERENCES `vai_tro` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lien_ket_oauth` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tai_khoan_id` bigint NOT NULL,
  `nha_cung_cap` enum('GOOGLE','FACEBOOK') NOT NULL,
  `provider_user_id` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_oauth` (`nha_cung_cap`,`provider_user_id`),
  KEY `fk_oauth_tk` (`tai_khoan_id`),
  CONSTRAINT `fk_oauth_tk` FOREIGN KEY (`tai_khoan_id`) REFERENCES `tai_khoan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `token_xac_thuc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tai_khoan_id` bigint NOT NULL,
  `token` varchar(100) NOT NULL,
  `loai` enum('XAC_THUC_EMAIL','DAT_LAI_MAT_KHAU') NOT NULL,
  `het_han` datetime NOT NULL,
  `da_dung` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_token_tk` (`tai_khoan_id`),
  CONSTRAINT `fk_token_tk` FOREIGN KEY (`tai_khoan_id`) REFERENCES `tai_khoan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ve` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `so_ve` varchar(20) NOT NULL,
  `dat_cho_id` bigint NOT NULL,
  `hanh_khach_id` bigint NOT NULL,
  `chuyen_bay_id` bigint NOT NULL,
  `khoang_id` tinyint NOT NULL,
  `ghe_id` int DEFAULT NULL,
  `chieu` enum('DI','VE') NOT NULL,
  `gia_goc` decimal(15,2) NOT NULL,
  `gia_ban` decimal(15,2) NOT NULL,
  `mien_cuoc_kg` smallint NOT NULL DEFAULT '0',
  `trang_thai` enum('DA_DAT','DA_XUAT','DA_HUY') NOT NULL DEFAULT 'DA_DAT',
  PRIMARY KEY (`id`),
  UNIQUE KEY `so_ve` (`so_ve`),
  UNIQUE KEY `uq_ve_hk_cb` (`hanh_khach_id`,`chuyen_bay_id`),
  UNIQUE KEY `uq_ve_ghe` (`chuyen_bay_id`,`ghe_id`),
  KEY `idx_ve_dc` (`dat_cho_id`),
  KEY `fk_ve_gia` (`chuyen_bay_id`,`khoang_id`),
  KEY `fk_ve_ghe` (`ghe_id`),
  CONSTRAINT `fk_ve_dc` FOREIGN KEY (`dat_cho_id`) REFERENCES `dat_cho` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ve_ghe` FOREIGN KEY (`ghe_id`) REFERENCES `ghe` (`id`),
  CONSTRAINT `fk_ve_gia` FOREIGN KEY (`chuyen_bay_id`, `khoang_id`) REFERENCES `gia_chuyen_bay` (`chuyen_bay_id`, `khoang_id`),
  CONSTRAINT `fk_ve_hk` FOREIGN KEY (`hanh_khach_id`) REFERENCES `hanh_khach` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
