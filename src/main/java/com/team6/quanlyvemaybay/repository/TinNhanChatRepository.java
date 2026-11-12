package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.TinNhanChat;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TinNhanChatRepository extends JpaRepository<TinNhanChat, Long> {
    @Query(value = """
            select m from TinNhanChat m
            join fetch m.nguoiGui g join fetch m.nguoiNhan n
            where (g.id = :khachHangId and n.vaiTro = :adminRole)
               or (n.id = :khachHangId and g.vaiTro = :adminRole)
            order by m.guiLuc desc, m.id desc
            """, countQuery = """
            select count(m) from TinNhanChat m
            where (m.nguoiGui.id = :khachHangId and m.nguoiNhan.vaiTro = :adminRole)
               or (m.nguoiNhan.id = :khachHangId and m.nguoiGui.vaiTro = :adminRole)
            """)
    Page<TinNhanChat> lichSu(@Param("khachHangId") Long khachHangId,
                             @Param("adminRole") VaiTro adminRole, Pageable pageable);

    @Query(value = """
            select c from NguoiDung c
            where c.vaiTro = :khachRole and
              (exists (select m.id from TinNhanChat m
                       where m.nguoiGui = c and m.nguoiNhan.vaiTro = :adminRole)
               or exists (select m.id from TinNhanChat m
                          where m.nguoiNhan = c and m.nguoiGui.vaiTro = :adminRole))
            order by c.id desc
            """, countQuery = """
            select count(c) from NguoiDung c
            where c.vaiTro = :khachRole and
              (exists (select m.id from TinNhanChat m
                       where m.nguoiGui = c and m.nguoiNhan.vaiTro = :adminRole)
               or exists (select m.id from TinNhanChat m
                          where m.nguoiNhan = c and m.nguoiGui.vaiTro = :adminRole))
            """)
    Page<NguoiDung> hoiThoai(@Param("khachRole") VaiTro khachRole,
                             @Param("adminRole") VaiTro adminRole, Pageable pageable);
}
