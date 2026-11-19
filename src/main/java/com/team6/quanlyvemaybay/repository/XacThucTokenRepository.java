package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.XacThucToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface XacThucTokenRepository extends JpaRepository<XacThucToken, Long> {
    Optional<XacThucToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update XacThucToken t set t.daSuDung = true where t.tokenHash = :hash and t.userId = :userId and t.loaiToken = :loai and t.daSuDung = false and t.hetHan > :now")
    int suDungNeuHopLe(@Param("hash") String hash, @Param("userId") Long userId,
                       @Param("loai") XacThucToken.Loai loai, @Param("now") Instant now);

    @Modifying
    @Query("update XacThucToken t set t.daSuDung = true where t.userId = :userId and t.loaiToken = :loai and t.daSuDung = false")
    int thuHoiTheoNguoiDung(@Param("userId") Long userId, @Param("loai") XacThucToken.Loai loai);
}
