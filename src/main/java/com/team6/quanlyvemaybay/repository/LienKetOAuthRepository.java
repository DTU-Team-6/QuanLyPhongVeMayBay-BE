package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.LienKetOAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LienKetOAuthRepository extends JpaRepository<LienKetOAuth, Long> {
    Optional<LienKetOAuth> findByNhaCungCapAndMaDinhDanh(String nhaCungCap, String maDinhDanh);
}
