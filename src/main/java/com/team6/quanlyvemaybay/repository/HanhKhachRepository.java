package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.HanhKhach;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HanhKhachRepository extends JpaRepository<HanhKhach, Long> {
    List<HanhKhach> findByDatChoId(Long datChoId);
}
