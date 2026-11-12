-- CHƯA THỰC THI: kiểm tra trước mọi vé có hành khách và đặt chỗ khớp nhau.
-- Chỉ điền ô còn NULL, không ghi đè snapshot đã có trên từng vé.
UPDATE ve_may_bay v
JOIN hanh_khach hk ON hk.id = v.hanh_khach_id AND hk.dat_cho_id = v.dat_cho_id
JOIN dat_cho dc ON dc.id = v.dat_cho_id
SET v.ho_ten_nguoi_di = COALESCE(v.ho_ten_nguoi_di, hk.ho_ten),
    v.email_thanh_toan = COALESCE(v.email_thanh_toan, dc.email_nguoi_dat)
WHERE v.ho_ten_nguoi_di IS NULL OR v.email_thanh_toan IS NULL;
