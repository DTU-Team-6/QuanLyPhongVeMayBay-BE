package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.flight.ChuyenBayCuaToiResponse;
import com.team6.quanlyvemaybay.dto.flight.TraCuuChuyenBayCuaToiRequest;
import com.team6.quanlyvemaybay.service.ChuyenBayCuaToiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chuyen-bay/cua-toi")
public class ChuyenBayCuaToiController {
    private final ChuyenBayCuaToiService service;

    public ChuyenBayCuaToiController(ChuyenBayCuaToiService service) {
        this.service = service;
    }

    @PostMapping("/tra-cuu")
    public ApiResponse<ChuyenBayCuaToiResponse> traCuu(
            @Valid @RequestBody TraCuuChuyenBayCuaToiRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok(service.traCuu(request, servletRequest.getRemoteAddr()),
                "Tra cứu chuyến bay thành công");
    }
}
