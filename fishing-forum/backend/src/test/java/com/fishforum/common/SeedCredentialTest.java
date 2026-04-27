package com.fishforum.common;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SeedCredentialTest {

    private static final Pattern ADMIN_HASH = Pattern.compile("'admin',\\s*'([^']+)'");

    @Test
    void dataSqlAdminPasswordMatchesDocumentedCredential() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/data.sql"));

        String hash = ADMIN_HASH.matcher(sql).results()
                .findFirst()
                .orElseThrow()
                .group(1);

        assertThat(new BCryptPasswordEncoder().matches("admin123", hash)).isTrue();
    }
}
