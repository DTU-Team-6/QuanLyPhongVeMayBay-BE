-- V2: dữ liệu danh mục ban đầu (khớp số dòng của database hiện có: AUTO_INCREMENT
-- hang_bay=4, hang_thanh_vien=5, khoang=3, quoc_tich=5, san_bay=4; vai_tro có 3 dòng id cố định)
INSERT INTO vai_tro VALUES (1,'ADMIN','Quản trị viên'),(2,'NHAN_VIEN','Nhân viên bán vé'),(3,'KHACH_HANG','Khách hàng');
INSERT INTO khoang (ma_khoang, ten_khoang) VALUES ('PHO_THONG','Phổ thông'),('THUONG_GIA','Thương gia');
INSERT INTO hang_thanh_vien (ten_hang, diem_toi_thieu, ty_le_giam) VALUES ('Thường',0,0),('Bạc',1000,2),('Vàng',5000,5),('Kim cương',20000,8);
INSERT INTO quoc_tich (ma_iso, ten) VALUES ('VN','Việt Nam'),('US','Hoa Kỳ'),('KR','Hàn Quốc'),('JP','Nhật Bản');
INSERT INTO hang_bay (ma_hang, ten_hang) VALUES ('VN','Vietnam Airlines'),('VJ','Vietjet Air'),('QH','Bamboo Airways');
INSERT INTO san_bay (ma_iata, ten_san_bay, thanh_pho, quoc_gia) VALUES
  ('DAD','Sân bay quốc tế Đà Nẵng','Đà Nẵng','Việt Nam'),
  ('SGN','Sân bay quốc tế Tân Sơn Nhất','TP. Hồ Chí Minh','Việt Nam'),
  ('HAN','Sân bay quốc tế Nội Bài','Hà Nội','Việt Nam');
