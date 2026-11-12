package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeRequest;
import com.team6.quanlyvemaybay.dto.ticket.SuaVeResponse;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.service.SuaVeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ve-may-bay")
public class SuaVeController {
    private final SuaVeService service;

    public SuaVeController(SuaVeService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    public ApiResponse<SuaVeResponse> suaVe(@PathVariable Long id,
                                             @Valid @RequestBody SuaVeRequest request) {
        if (id < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "ID vé phải là số nguyên dương.",
                    List.of(new ErrorDetail("id", "INVALID_PARAMETER", "ID vé phải là số nguyên dương.")));
        }
        return ApiResponse.ok(service.suaVe(id, request), "Sửa thông tin vé thành công");
    }
}
