package com.team6.quanlyvemaybay.service;
import com.team6.quanlyvemaybay.dto.booking.*;
import com.team6.quanlyvemaybay.entity.*;
import com.team6.quanlyvemaybay.entity.enums.*;
import com.team6.quanlyvemaybay.exception.*;
import com.team6.quanlyvemaybay.repository.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID; import java.util.List;
@Service
public class DatChoService {
 private final HangVeChuyenBayRepository hangVeRepo; private final DatChoRepository datChoRepo; private final HanhKhachRepository hanhKhachRepo;
 public DatChoService(HangVeChuyenBayRepository h, DatChoRepository d, HanhKhachRepository hk){hangVeRepo=h;datChoRepo=d;hanhKhachRepo=hk;}
 @Transactional
 public BookingResponse create(BookingRequest req){
  HangVeChuyenBay hv=hangVeRepo.findById(req.hangVeId()).orElseThrow(()->new ResourceNotFoundException("FARE_NOT_FOUND","Không tìm thấy hạng vé"));
  if(hv.getChuyenBay().getThoiGianKhoiHanh().isBefore(LocalDateTime.now())) throw new BusinessException(org.springframework.http.HttpStatus.CONFLICT,"CHUYEN_BAY_DA_KHOI_HANH","Chuyến bay đã khởi hành");
  if(req.hanhKhach().size()!=req.soNguoiLon()+req.soTreEm()) throw new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST,"PASSENGER_COUNT_MISMATCH","Số hành khách không khớp");
  if(hangVeRepo.reserveSeats(hv.getId(), req.hanhKhach().size()) == 0) throw new BusinessException(org.springframework.http.HttpStatus.CONFLICT,"HET_CHO","Chuyến bay không đủ chỗ");
  DatCho dc=new DatCho(); dc.setMaDatCho(UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase()); dc.setEmailNguoiDat(req.emailNguoiDat()); dc.setSoDienThoaiNguoiDat(req.soDienThoaiNguoiDat()); dc.setHoTenNguoiDat(req.hoTenNguoiDat()); dc.setSoNguoiLon(req.soNguoiLon()); dc.setSoTreEm(req.soTreEm()); dc.setNgayDat(LocalDateTime.now()); dc.setTongGiaBan(hv.getGiaBan().add(hv.getThuePhi()).multiply(BigDecimal.valueOf(req.hanhKhach().size()))); dc=datChoRepo.save(dc);
  for(PassengerRequest p:req.hanhKhach()){HanhKhach hk=new HanhKhach(); hk.setDatChoId(dc.getId()); hk.setHoTen(p.hoTen()); hk.setNgaySinh(p.ngaySinh()); hk.setGioiTinh(p.gioiTinh()); hk.setEmail(p.email()); hk.setSoDienThoai(p.soDienThoai()); hk.setCmndPassport(p.cmndPassport()); hk.setLoaiHanhKhach(p.loaiHanhKhach()); hanhKhachRepo.save(hk);}
  return new BookingResponse(dc.getId(),dc.getMaDatCho(),dc.getTongGiaBan(),dc.getTrangThai().name(),req.hanhKhach().size());
 }
}
