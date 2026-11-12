package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.service.InVePdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ve-may-bay")
public class InVePdfController {
    private final InVePdfService service;

    public InVePdfController(InVePdfService service) {
        this.service = service;
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> inVe(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_TICKET_ID", "ID vé phải lớn hơn 0.");
        }
        byte[] pdf = service.xuatPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("ve-" + id + ".pdf").build().toString())
                .contentLength(pdf.length)
                .body(pdf);
    }
}
