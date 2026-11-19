CREATE TABLE dinh_danh_oauth (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nguoi_dung_id BIGINT NOT NULL,
    nha_cung_cap VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    ma_dinh_danh VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dinh_danh_oauth (nha_cung_cap, ma_dinh_danh),
    KEY idx_dinh_danh_oauth_nguoi_dung (nguoi_dung_id),
    CONSTRAINT fk_dinh_danh_oauth_nguoi_dung FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id)
) ENGINE=InnoDB;
