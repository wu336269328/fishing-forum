package com.fishforum.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPathResolver {

    private UploadPathResolver() {
    }

    public static Path resolve(String configuredPath) {
        return resolve(Paths.get(System.getProperty("user.dir")), configuredPath);
    }

    static Path resolve(Path baseDir, String configuredPath) {
        String path = (configuredPath == null || configuredPath.isBlank()) ? "./uploads/" : configuredPath;
        Path configured = Paths.get(path);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }
        Path direct = baseDir.resolve(configured).normalize();
        if (Files.exists(direct)) {
            return direct;
        }
        Path projectRootUpload = baseDir.resolve("backend").resolve(configured).normalize();
        if (Files.exists(projectRootUpload)) {
            return projectRootUpload;
        }
        return direct;
    }
}
