package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.ticket.XoaVeResponse;
import com.team6.quanlyvemaybay.service.XoaVeService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ve-may-bay")
public class XoaVeController {
    private final XoaVeService service;

    public XoaVeController(XoaVeService service) {
        this.service = service;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<XoaVeResponse> xoaVe(@PathVariable @Min(1) Long id) {
        return ApiResponse.ok(service.xoaVe(id), "Hủy vé thành công");
    }
}
