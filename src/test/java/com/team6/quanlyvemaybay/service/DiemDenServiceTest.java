package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.destination.CreateDestinationRequest;
import com.team6.quanlyvemaybay.dto.destination.DestinationResponse;
import com.team6.quanlyvemaybay.entity.DiemDen;
import com.team6.quanlyvemaybay.entity.SanBay;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.DiemDenRepository;
import com.team6.quanlyvemaybay.repository.SanBayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử đơn vị cho DiemDenService (lấy danh sách phân trang, thêm mới và kiểm tra trùng lặp).
 */
@ExtendWith(MockitoExtension.class)
class DiemDenServiceTest {

    @Mock
    private DiemDenRepository diemDenRepository;

    @Mock
    private SanBayRepository sanBayRepository;

    private DiemDenService diemDenService;

    @BeforeEach
    void setUp() {
        diemDenService = new DiemDenService(diemDenRepository, sanBayRepository);
    }

    @Test
    void getDestinations_shouldNormalizeFiltersAndReturnPageResponse() {
        SanBay dad = new SanBay("DAD", "Sân bay Quốc tế Đà Nẵng", "Đà Nẵng", "Việt Nam");
        DiemDen diemDen = new DiemDen();
        diemDen.setId(1L);
        diemDen.setTenDiemDen("Bà Nà Hills & Cầu Vàng");
        diemDen.setSanBay(dad);
        diemDen.setMoTa("Khung cảnh mây trời ngoạn mục.");
        diemDen.setHinhAnh("/assets/destinations/bana.jpg");

        Page<DiemDen> page = new PageImpl<>(List.of(diemDen), PageRequest.of(0, 10), 1);
        when(diemDenRepository.findWithFilters(eq("Bà Nà"), eq("DAD"), eq(PageRequest.of(0, 10))))
                .thenReturn(page);

        PageResponse<DestinationResponse> result = diemDenService.getDestinations("  Bà Nà  ", " dad ", 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content().getFirst().tenDiemDen()).isEqualTo("Bà Nà Hills & Cầu Vàng");
        assertThat(result.content().getFirst().maSanBay()).isEqualTo("DAD");
        assertThat(result.content().getFirst().thanhPho()).isEqualTo("Đà Nẵng");
    }

    @Test
    void getDestinations_shouldReturnEmptyPageWhenNoMatch() {
        when(diemDenRepository.findWithFilters(any(), any(), any()))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        PageResponse<DestinationResponse> result = diemDenService.getDestinations("Không tồn tại", null, 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void createDestination_shouldNormalizeInputAndSaveSuccessfully() {
        SanBay dad = new SanBay("DAD", "Sân bay Quốc tế Đà Nẵng", "Đà Nẵng", "Việt Nam");
        CreateDestinationRequest request = new CreateDestinationRequest(
                "  Cầu   Rồng Đà Nẵng ",
                " dad ",
                "  Biểu tượng bên bờ sông Hàn  ",
                "  /assets/destinations/caurong.jpg  "
        );

        when(sanBayRepository.findById("DAD")).thenReturn(Optional.of(dad));
        when(diemDenRepository.existsDuplicateInAirport("Cầu Rồng Đà Nẵng", "DAD")).thenReturn(false);
        when(diemDenRepository.save(any(DiemDen.class))).thenAnswer(invocation -> {
            DiemDen entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        DestinationResponse response = diemDenService.createDestination(request);

        ArgumentCaptor<DiemDen> captor = ArgumentCaptor.forClass(DiemDen.class);
        verify(diemDenRepository).save(captor.capture());

        assertThat(captor.getValue().getTenDiemDen()).isEqualTo("Cầu Rồng Đà Nẵng");
        assertThat(captor.getValue().getMoTa()).isEqualTo("Biểu tượng bên bờ sông Hàn");
        assertThat(captor.getValue().getHinhAnh()).isEqualTo("/assets/destinations/caurong.jpg");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.maSanBay()).isEqualTo("DAD");
        assertThat(response.tenSanBay()).isEqualTo("Sân bay Quốc tế Đà Nẵng");
    }

    @Test
    void createDestination_shouldThrowResourceNotFoundWhenAirportDoesNotExist() {
        CreateDestinationRequest request = new CreateDestinationRequest(
                "Đảo Phú Quốc",
                "XYZ",
                null,
                null
        );
        when(sanBayRepository.findById("XYZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diemDenService.createDestination(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("XYZ");

        verify(diemDenRepository, never()).save(any());
    }

    @Test
    void createDestination_shouldThrowConflictWhenDuplicateInSameAirport() {
        SanBay dad = new SanBay("DAD", "Sân bay Quốc tế Đà Nẵng", "Đà Nẵng", "Việt Nam");
        CreateDestinationRequest request = new CreateDestinationRequest(
                "Bà Nà Hills & Cầu Vàng",
                "dad",
                null,
                null
        );

        when(sanBayRepository.findById("DAD")).thenReturn(Optional.of(dad));
        when(diemDenRepository.existsDuplicateInAirport("Bà Nà Hills & Cầu Vàng", "DAD")).thenReturn(true);

        assertThatThrownBy(() -> diemDenService.createDestination(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessEx = (BusinessException) ex;
                    assertThat(businessEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(businessEx.getCode()).isEqualTo("DESTINATION_ALREADY_EXISTS");
                    assertThat(businessEx.getDetails()).hasSize(1);
                    assertThat(businessEx.getDetails().getFirst().field()).isEqualTo("tenDiemDen");
                });

        verify(diemDenRepository, never()).save(any());
    }
}
