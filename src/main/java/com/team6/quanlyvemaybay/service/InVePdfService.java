package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.config.CommonInfoProperties;
import com.team6.quanlyvemaybay.entity.ChuyenBay;
import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.VeMayBayRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InVePdfService {
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final Locale TIENG_VIET = Locale.forLanguageTag("vi-VN");

    private final VeMayBayRepository repository;
    private final CommonInfoProperties commonInfo;

    public InVePdfService(VeMayBayRepository repository, CommonInfoProperties commonInfo) {
        this.repository = repository;
        this.commonInfo = commonInfo;
    }

    @Transactional(readOnly = true)
    public byte[] xuatPdf(Long id) {
        VeMayBay ve = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TICKET_NOT_FOUND", "Không tìm thấy vé."));
        if (ve.getHoTenNguoiDi() == null || ve.getHoTenNguoiDi().isBlank()
                || ve.getEmailThanhToan() == null || ve.getEmailThanhToan().isBlank()) {
            throw new BusinessException(HttpStatus.CONFLICT, "TICKET_SNAPSHOT_MISSING",
                    "Vé chưa có thông tin riêng của người đi và email; cần hoàn tất backfill trước khi in.");
        }

        HangVeChuyenBay hangVe = ve.getHangVe();
        ChuyenBay chuyenBay = hangVe.getChuyenBay();
        try (PDDocument pdf = new PDDocument();
             InputStream fontStream = new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType0Font font = PDType0Font.load(pdf, fontStream, true);
            PdfWriter writer = new PdfWriter(pdf, font);
            try {
                writer.line("VÉ MÁY BAY ĐIỆN TỬ", 19);
                writer.line(commonInfo.getBrandName(), 11);
                writer.space(13);
                writer.line("Mã vé: " + ve.getMaVe(), 11);
                writer.line("Mã đặt chỗ: " + ve.getDatCho().getMaDatCho(), 11);
                writer.line("Người đi: " + ve.getHoTenNguoiDi(), 11);
                writer.line("Email thanh toán trên vé: " + ve.getEmailThanhToan(), 11);
                writer.space(10);
                writer.line("CHẶNG BAY", 13);
                writer.line("Từ: " + chuyenBay.getSanBayDi().getThanhPho()
                        + " (" + chuyenBay.getSanBayDi().getMaSanBay() + ")", 11);
                writer.line("Đến: " + chuyenBay.getSanBayDen().getThanhPho()
                        + " (" + chuyenBay.getSanBayDen().getMaSanBay() + ")", 11);
                writer.line("Khởi hành: " + NGAY_GIO.format(chuyenBay.getThoiGianKhoiHanh()), 11);
                writer.line("Hạ cánh: " + NGAY_GIO.format(chuyenBay.getThoiGianHaCanh()), 11);
                writer.line("Chuyến bay: " + chuyenBay.getMaChuyenBay()
                        + " - " + chuyenBay.getHangHangKhong().getTenHang(), 11);
                writer.line("Ghế: " + (ve.getSoGhe() == null ? "Chưa phân ghế" : ve.getSoGhe()), 11);
                writer.line("Khoang: " + tenKhoang(hangVe.getLoaiKhoang()), 11);
                writer.line("Hành lý xách tay: " + hangVe.getHanhLyXachTayKg() + " kg", 11);
                writer.line("Hành lý ký gửi: " + hangVe.getHanhLyKyGuiKg() + " kg", 11);
                writer.space(10);
                writer.line("GIÁ VÉ", 13);
                writer.line("Giá bán: " + tien(ve.getGiaBan()), 11);
                writer.line("Thuế và phí: " + tien(ve.getThuePhi()), 11);
                writer.line("Cước hành lý: " + tien(ve.getCuocHanhLy()), 11);
                writer.line("Tổng giá vé: " + tien(ve.getTongGia()), 12);
                writer.line("Trạng thái vé: " + tenTrangThai(ve.getTrangThai()), 11);
                writer.space(13);
                writer.line("Liên hệ: " + commonInfo.getContact().getHotline()
                        + " - " + commonInfo.getContact().getEmail(), 10);
                writer.line(commonInfo.getContact().getAddress(), 10);
            } finally {
                writer.close();
            }
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể tạo file PDF cho vé " + id, ex);
        }
    }

    private static String tien(BigDecimal gia) {
        return NumberFormat.getNumberInstance(TIENG_VIET).format(gia) + " VND";
    }

    private static String tenKhoang(LoaiKhoang khoang) {
        return switch (khoang) {
            case PHO_THONG -> "Phổ thông";
            case THUONG_GIA -> "Thương gia";
        };
    }

    private static String tenTrangThai(TrangThaiVe trangThai) {
        return switch (trangThai) {
            case CHO_THANH_TOAN -> "Chờ thanh toán";
            case DA_THANH_TOAN -> "Đã thanh toán";
            case DA_HUY -> "Đã hủy";
        };
    }

    private static final class PdfWriter implements AutoCloseable {
        private static final float LEFT = 55;
        private static final float RIGHT = 55;
        private static final float BOTTOM = 55;
        private final PDDocument document;
        private final PDType0Font font;
        private PDPageContentStream stream;
        private float y;

        private PdfWriter(PDDocument document, PDType0Font font) throws IOException {
            this.document = document;
            this.font = font;
            nextPage();
        }

        private void nextPage() throws IOException {
            if (stream != null) stream.close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PDRectangle.A4.getHeight() - 55;
        }

        private void line(String value, float size) throws IOException {
            String remaining = value == null ? "" : value.replaceAll("\\p{Cc}+", " ").strip();
            float maxWidth = PDRectangle.A4.getWidth() - LEFT - RIGHT;
            do {
                int end = remaining.length();
                while (end > 1 && font.getStringWidth(remaining.substring(0, end)) * size / 1000 > maxWidth) {
                    end--;
                }
                String part = remaining.substring(0, end);
                if (y < BOTTOM + size) nextPage();
                stream.beginText();
                stream.setFont(font, size);
                stream.newLineAtOffset(LEFT, y);
                stream.showText(part);
                stream.endText();
                y -= size + 7;
                remaining = remaining.substring(end);
            } while (!remaining.isEmpty());
        }

        private void space(float points) {
            y -= points;
        }

        @Override
        public void close() throws IOException {
            if (stream != null) stream.close();
        }
    }
}
