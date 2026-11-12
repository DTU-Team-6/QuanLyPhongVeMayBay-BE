package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeRequest;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeResponse;
import com.team6.quanlyvemaybay.service.SuaVeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ve-may-bay")
public class SuaVeController {
    private final SuaVeService service;

    public SuaVeController(SuaVeService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    public ApiResponse<SuaVeResponse> suaVe(@PathVariable @Min(1) Long id,
                                             @Valid @RequestBody SuaVeRequest request) {
        return ApiResponse.ok(service.suaVe(id, request), "Sửa thông tin vé thành công");
    }
}
