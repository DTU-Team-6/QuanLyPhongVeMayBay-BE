CREATE TABLE phien_lam_moi (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nguoi_dung_id BIGINT NOT NULL,
    nhom_phien CHAR(36) NOT NULL,
    token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    het_han DATETIME(6) NOT NULL,
    da_su_dung BOOLEAN NOT NULL DEFAULT FALSE,
    da_thu_hoi BOOLEAN NOT NULL DEFAULT FALSE,
    ghi_nho BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phien_lam_moi_token_hash (token_hash),
    KEY idx_phien_lam_moi_nhom (nhom_phien),
    KEY idx_phien_lam_moi_nguoi_dung (nguoi_dung_id),
    CONSTRAINT fk_phien_lam_moi_nguoi_dung FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id)
) ENGINE=InnoDB;
