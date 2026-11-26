package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.news.CreateNewsRequest;
import com.team6.quanlyvemaybay.dto.news.NewsDetailDto;
import com.team6.quanlyvemaybay.dto.news.NewsItemDto;
import com.team6.quanlyvemaybay.dto.news.UpdateNewsRequest;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.TinTuc;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.TinTucRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service xử lý các nghiệp vụ CRUD quản lý tin tức và bài viết.
 */
@Service
public class TinTucService {

    public static final String MORE_DELIMITER = "\n<!--more-->\n";
    private static final DateTimeFormatter FORMATTER_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TinTucRepository tinTucRepo;
    private final NguoiDungRepository nguoiDungRepo;

    public TinTucService(TinTucRepository tinTucRepo, NguoiDungRepository nguoiDungRepo) {
        this.tinTucRepo = tinTucRepo;
        this.nguoiDungRepo = nguoiDungRepo;
    }

    /**
     * Lấy danh sách tin tức công khai (luôn chỉ trả bài viết trạng thái HIEN_THI).
     */
    @Transactional(readOnly = true)
    public PageResponse<NewsItemDto> getPublicNewsList(String keyword, String chuyenMuc, LoaiTinTuc loaiTin,
                                                       int page, int size) {
        return getNewsList(keyword, chuyenMuc, loaiTin, TrangThaiTinTuc.HIEN_THI, page, size);
    }

    /**
     * Lấy danh sách tin tức có tìm kiếm, lọc theo chuyên mục và phân trang (Admin / Quản trị).
     */
    @Transactional(readOnly = true)
    public PageResponse<NewsItemDto> getNewsList(String keyword, String chuyenMuc, LoaiTinTuc loaiTin,
                                                 TrangThaiTinTuc trangThai, int page, int size) {
        LoaiTinTuc effectiveLoaiTin = loaiTin;
        if (effectiveLoaiTin == null && chuyenMuc != null && !chuyenMuc.isBlank() && !chuyenMuc.equalsIgnoreCase("Tất cả chuyên mục")) {
            effectiveLoaiTin = LoaiTinTuc.fromString(chuyenMuc);
        }

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);

        Page<TinTuc> newsPage = tinTucRepo.findWithFilters(searchKeyword, effectiveLoaiTin, trangThai, pageable);

        // Chỉ truy vấn các tác giả xuất hiện trong trang hiện tại (tránh full-table scan & NPE)
        Set<Long> authorIds = newsPage.getContent().stream()
                .map(TinTuc::getNguoiDungId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> authorMap = authorIds.isEmpty()
                ? Map.of()
                : nguoiDungRepo.findAllById(authorIds).stream()
                .collect(Collectors.toMap(
                        NguoiDung::getId,
                        nd -> (nd.getHoTen() != null && !nd.getHoTen().isBlank()) ? nd.getHoTen() : "Vân Việt",
                        (a, b) -> a
                ));

        Page<NewsItemDto> dtoPage = newsPage.map(t -> mapToItemDto(t, authorMap.getOrDefault(t.getNguoiDungId(), "Vân Việt")));
        return PageResponse.of(dtoPage);
    }

    /**
     * Lấy chi tiết bài viết công khai (chặn bài viết đang ở trạng thái AN).
     */
    @Transactional(readOnly = true)
    public NewsDetailDto getPublicNewsDetail(Long id) {
        TinTuc t = tinTucRepo.findById(id)
                .filter(item -> item.getTrangThai() == TrangThaiTinTuc.HIEN_THI)
                .orElseThrow(() -> new ResourceNotFoundException("NEWS_NOT_FOUND", "Không tìm thấy bài viết tin tức với ID: " + id));

        String authorName = nguoiDungRepo.findById(t.getNguoiDungId())
                .map(NguoiDung::getHoTen)
                .filter(name -> !name.isBlank())
                .orElse("Vân Việt");

        return mapToDetailDto(t, authorName);
    }

    /**
     * Lấy chi tiết bài viết theo ID cho quản trị (xem được cả bài ẩn).
     */
    @Transactional(readOnly = true)
    public NewsDetailDto getNewsDetail(Long id) {
        TinTuc t = tinTucRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NEWS_NOT_FOUND", "Không tìm thấy bài viết tin tức với ID: " + id));

        String authorName = nguoiDungRepo.findById(t.getNguoiDungId())
                .map(NguoiDung::getHoTen)
                .filter(name -> !name.isBlank())
                .orElse("Vân Việt");

        return mapToDetailDto(t, authorName);
    }

    /**
     * Thêm bài viết tin tức mới (Ảnh 1).
     */
    @Transactional
    public NewsDetailDto createNews(CreateNewsRequest req) {
        LoaiTinTuc loaiTin = req.resolveLoaiTin();
        Long authorId = resolveAuthenticatedUserId(req.nguoiDungId());

        String rawContent = req.noiDung() != null ? req.noiDung().trim() : "";
        String shortDesc = req.moTaNgan() != null ? req.moTaNgan().trim() : "";
        String combinedContent = shortDesc + MORE_DELIMITER + rawContent;

        TinTuc tinTuc = new TinTuc();
        tinTuc.setTieuDe(req.tieuDe().trim());
        tinTuc.setNoiDung(combinedContent);
        tinTuc.setAnhDaiDien(req.anhDaiDien() != null ? req.anhDaiDien().trim() : null);
        tinTuc.setLoaiTin(loaiTin);
        tinTuc.setNguoiDungId(authorId);
        tinTuc.setTrangThai(req.trangThai() != null ? req.trangThai() : TrangThaiTinTuc.HIEN_THI);
        tinTuc.setThoiGianTao(LocalDateTime.now());

        tinTuc = tinTucRepo.save(tinTuc);

        String authorName = nguoiDungRepo.findById(authorId)
                .map(NguoiDung::getHoTen)
                .filter(name -> !name.isBlank())
                .orElse("Vân Việt");

        return mapToDetailDto(tinTuc, authorName);
    }

    private Long resolveAuthenticatedUserId(Long fallbackId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                Long jwtUserId = Long.valueOf(jwt.getSubject());
                if (nguoiDungRepo.existsById(jwtUserId)) {
                    return jwtUserId;
                }
            }
        } catch (Exception ignored) {
        }
        if (fallbackId != null && nguoiDungRepo.existsById(fallbackId)) {
            return fallbackId;
        }
        return nguoiDungRepo.findAll(PageRequest.of(0, 1)).stream()
                .map(NguoiDung::getId)
                .findFirst()
                .orElse(1L);
    }

    /**
     * Cập nhật bài viết tin tức (Ảnh 2).
     */
    @Transactional
    public NewsDetailDto updateNews(Long id, UpdateNewsRequest req) {
        TinTuc tinTuc = tinTucRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NEWS_NOT_FOUND", "Không tìm thấy bài viết tin tức với ID: " + id));

        if (req.tieuDe() != null) {
            if (req.tieuDe().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_NEWS_DATA", "Tiêu đề tin tức không được để trống");
            }
            tinTuc.setTieuDe(req.tieuDe().trim());
        }
        if (req.moTaNgan() != null && req.moTaNgan().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_NEWS_DATA", "Mô tả ngắn không được để trống");
        }
        if (req.noiDung() != null && req.noiDung().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_NEWS_DATA", "Nội dung bài viết không được để trống");
        }
        if (req.anhDaiDien() != null) {
            tinTuc.setAnhDaiDien(req.anhDaiDien().trim());
        }
        LoaiTinTuc updatedLoaiTin = req.resolveLoaiTin();
        if (updatedLoaiTin != null) {
            tinTuc.setLoaiTin(updatedLoaiTin);
        }
        if (req.trangThai() != null) {
            tinTuc.setTrangThai(req.trangThai());
        }

        // Xử lý cập nhật nội dung và mô tả ngắn
        String currentShortDesc = extractShortDescription(tinTuc.getNoiDung());
        String currentBody = extractBodyContent(tinTuc.getNoiDung());

        String newShortDesc = (req.moTaNgan() != null) ? req.moTaNgan().trim() : currentShortDesc;
        String newBody = (req.noiDung() != null) ? req.noiDung().trim() : currentBody;

        tinTuc.setNoiDung(newShortDesc + MORE_DELIMITER + newBody);

        tinTuc = tinTucRepo.save(tinTuc);

        String authorName = nguoiDungRepo.findById(tinTuc.getNguoiDungId())
                .map(NguoiDung::getHoTen)
                .orElse("Vân Việt");

        return mapToDetailDto(tinTuc, authorName);
    }

    /**
     * Xóa bài viết tin tức (Ảnh 4).
     */
    @Transactional
    public String deleteNews(Long id) {
        TinTuc tinTuc = tinTucRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NEWS_NOT_FOUND", "Không tìm thấy bài viết tin tức với ID: " + id));

        String maTin = String.format("TT%03d", tinTuc.getId());
        String tieuDe = tinTuc.getTieuDe();

        tinTucRepo.delete(tinTuc);
        return String.format("Xóa thành công tin %s – %s", maTin, tieuDe);
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH HELPER
    // ==========================================

    private NewsItemDto mapToItemDto(TinTuc t, String authorName) {
        String maTin = String.format("TT%03d", t.getId());
        String shortDesc = extractShortDescription(t.getNoiDung());
        String ngayDang = t.getThoiGianTao() != null ? t.getThoiGianTao().format(FORMATTER_NGAY) : "";

        return new NewsItemDto(
                t.getId(),
                maTin,
                t.getTieuDe(),
                shortDesc,
                t.getAnhDaiDien(),
                t.getLoaiTin().getTenHienThi(),
                t.getLoaiTin(),
                ngayDang,
                t.getThoiGianTao(),
                authorName,
                t.getTrangThai()
        );
    }

    private NewsDetailDto mapToDetailDto(TinTuc t, String authorName) {
        String maTin = String.format("TT%03d", t.getId());
        String shortDesc = extractShortDescription(t.getNoiDung());
        String body = extractBodyContent(t.getNoiDung());
        String ngayDang = t.getThoiGianTao() != null ? t.getThoiGianTao().format(FORMATTER_NGAY) : "";

        return new NewsDetailDto(
                t.getId(),
                maTin,
                t.getTieuDe(),
                shortDesc,
                body,
                t.getAnhDaiDien(),
                t.getLoaiTin().getTenHienThi(),
                t.getLoaiTin(),
                t.getNguoiDungId(),
                authorName,
                ngayDang,
                t.getThoiGianTao(),
                t.getTrangThai()
        );
    }

    private String extractShortDescription(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return "";
        }
        int index = rawContent.indexOf(MORE_DELIMITER);
        if (index != -1) {
            return rawContent.substring(0, index).trim();
        }
        // Nếu không có delimiter, lấy tối đa 150 ký tự đầu tiên
        String plain = rawContent.replace("\n", " ").trim();
        if (plain.length() <= 150) {
            return plain;
        }
        return plain.substring(0, 150) + "...";
    }

    private String extractBodyContent(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return "";
        }
        int index = rawContent.indexOf(MORE_DELIMITER);
        if (index != -1) {
            return rawContent.substring(index + MORE_DELIMITER.length()).trim();
        }
        return rawContent.trim();
    }
}
