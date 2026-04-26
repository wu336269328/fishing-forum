package com.fishforum.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class FileTypeValidator {

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private static final Map<String, String> EXTENSION_BY_MIME = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif",
            "image/webp", ".webp");

    private FileTypeValidator() {
    }

    public static Result<?> validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return Result.error(400, "图片大小不超过10MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_MIME_TYPES.contains(contentType)) {
            return Result.error(400, "图片格式仅支持 PNG、JPG、GIF、WEBP");
        }
        if ("image/jpeg".equals(contentType) && !(".jpg".equals(extension) || ".jpeg".equals(extension))) {
            return Result.error(400, "图片扩展名与类型不匹配");
        }
        if (!"image/jpeg".equals(contentType) && !EXTENSION_BY_MIME.get(contentType).equals(extension)) {
            return Result.error(400, "图片扩展名与类型不匹配");
        }

        try {
            byte[] header = file.getInputStream().readNBytes(12);
            if (!hasExpectedMagicBytes(contentType, header)) {
                return Result.error(400, "文件内容不是有效图片");
            }
        } catch (IOException e) {
            return Result.error(400, "图片读取失败");
        }
        return Result.ok();
    }

    public static String safeImageExtension(MultipartFile file) {
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        return EXTENSION_BY_MIME.getOrDefault(contentType, ".png");
    }

    private static String getExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        return dot >= 0 ? originalName.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    private static boolean hasExpectedMagicBytes(String contentType, byte[] header) {
        if ("image/png".equals(contentType)) {
            return header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;
        }
        if ("image/jpeg".equals(contentType)) {
            return header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
        }
        if ("image/gif".equals(contentType)) {
            return header.length >= 6
                    && header[0] == 0x47
                    && header[1] == 0x49
                    && header[2] == 0x46
                    && header[3] == 0x38
                    && (header[4] == 0x37 || header[4] == 0x39)
                    && header[5] == 0x61;
        }
        if ("image/webp".equals(contentType)) {
            return header.length >= 12
                    && header[0] == 0x52
                    && header[1] == 0x49
                    && header[2] == 0x46
                    && header[3] == 0x46
                    && header[8] == 0x57
                    && header[9] == 0x45
                    && header[10] == 0x42
                    && header[11] == 0x50;
        }
        return false;
    }
}
