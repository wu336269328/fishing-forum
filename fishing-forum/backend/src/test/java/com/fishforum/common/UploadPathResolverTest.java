package com.fishforum.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UploadPathResolverTest {

    @TempDir Path tempDir;

    @Test
    void resolvesBackendUploadsWhenAppStartsFromProjectRoot() throws Exception {
        Path uploads = tempDir.resolve("backend").resolve("uploads");
        Files.createDirectories(uploads);

        Path resolved = UploadPathResolver.resolve(tempDir, "./uploads/");

        assertThat(resolved).isEqualTo(uploads);
    }

    @Test
    void resolvesDirectUploadsWhenAppStartsFromBackendDirectory() throws Exception {
        Path backendDir = tempDir.resolve("backend");
        Path uploads = backendDir.resolve("uploads");
        Files.createDirectories(uploads);

        Path resolved = UploadPathResolver.resolve(backendDir, "./uploads/");

        assertThat(resolved).isEqualTo(uploads);
    }
}
