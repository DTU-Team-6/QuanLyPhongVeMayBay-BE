package com.team6.quanlyvemaybay.controller;
import com.team6.quanlyvemaybay.dto.booking.*; import com.team6.quanlyvemaybay.dto.common.ApiResponse; import com.team6.quanlyvemaybay.service.DatChoService; import jakarta.validation.Valid; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/dat-cho")
public class DatChoController { private final DatChoService service; public DatChoController(DatChoService s){service=s;} @PostMapping public ResponseEntity<ApiResponse<BookingResponse>> create(@Valid @RequestBody BookingRequest request){return ResponseEntity.ok(ApiResponse.ok(service.create(request),"Tạo đặt chỗ và lưu thông tin hành khách thành công"));} }
