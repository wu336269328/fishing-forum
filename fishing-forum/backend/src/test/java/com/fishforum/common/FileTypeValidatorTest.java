package com.fishforum.common;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class FileTypeValidatorTest {

    @Test
    void acceptsPngJpegGifAndWebpMagicBytes() {
        assertThat(FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "fish.png", "image/png",
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})).getCode()).isEqualTo(200);
        assertThat(FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "fish.jpg", "image/jpeg",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0})).getCode()).isEqualTo(200);
        assertThat(FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "fish.gif", "image/gif",
                new byte[] {0x47, 0x49, 0x46, 0x38, 0x39, 0x61})).getCode()).isEqualTo(200);
        assertThat(FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "fish.webp", "image/webp",
                new byte[] {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50})).getCode()).isEqualTo(200);
    }

    @Test
    void rejectsSvgEvenWhenBrowserMarksItAsImage() {
        Result<?> result = FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "payload.svg", "image/svg+xml",
                "<svg onload=alert(1)>".getBytes()));

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("图片格式");
    }

    @Test
    void rejectsMismatchedExtensionAndContent() {
        Result<?> result = FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "payload.png", "image/png",
                "<script>alert(1)</script>".getBytes()));

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("文件内容");
    }

    @Test
    void rejectsOversizedImages() {
        byte[] oversized = new byte[10 * 1024 * 1024 + 1];
        oversized[0] = (byte) 0x89;
        oversized[1] = 0x50;
        oversized[2] = 0x4E;
        oversized[3] = 0x47;
        Result<?> result = FileTypeValidator.validateImage(new MockMultipartFile(
                "file", "huge.png", "image/png", oversized));

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("大小");
    }
}
