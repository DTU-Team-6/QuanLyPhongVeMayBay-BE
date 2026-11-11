package com.team6.quanlyvemaybay.dto.flight;

import java.util.List;

public record FlightDetailResponse(Long chuyenBayId, List<FlightSearchResponse> hangVe) {
}
