CREATE TABLE xac_thuc_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    loai_token ENUM('XAC_THUC_EMAIL', 'QUEN_MAT_KHAU') NOT NULL,
    da_su_dung BOOLEAN NOT NULL DEFAULT FALSE,
    het_han DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_token_hash (token_hash),
    KEY idx_token_user (user_id),
    CONSTRAINT fk_token_nguoidung FOREIGN KEY (user_id) REFERENCES nguoi_dung (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
