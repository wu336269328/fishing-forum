package com.fishforum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final String MIGRATION_PATTERN = "classpath*:migrations/*.sql";

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ensureMigrationTable();
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MIGRATION_PATTERN);
        Arrays.sort(resources, Comparator.comparing(resource -> {
            try {
                return resource.getFilename();
            } catch (Exception e) {
                return "";
            }
        }));

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String version = versionOf(filename);
            String sql = resource.getContentAsString(StandardCharsets.UTF_8);
            String checksum = checksum(sql);
            AppliedMigration applied = appliedMigration(version);
            if (applied != null) {
                if (!checksum.equals(applied.checksum())) {
                    throw new IllegalStateException("Migration checksum mismatch: " + filename);
                }
                log.info("Skipping already applied database migration {}", filename);
                continue;
            }
            transactionTemplate.executeWithoutResult(status -> {
                for (String statement : splitStatements(sql)) {
                    jdbcTemplate.execute(statement);
                }
                jdbcTemplate.update(
                        "INSERT INTO schema_migrations(version, filename, checksum, applied_at) VALUES (?, ?, ?, NOW())",
                        version, filename, checksum);
            });
            log.info("Applied database migration {}", filename);
        }
    }

    private void ensureMigrationTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    version VARCHAR(100) PRIMARY KEY,
                    filename VARCHAR(255) NOT NULL,
                    checksum VARCHAR(64) NOT NULL,
                    applied_at TIMESTAMP NOT NULL DEFAULT NOW()
                )
                """);
    }

    private AppliedMigration appliedMigration(String version) {
        List<AppliedMigration> rows = jdbcTemplate.query(
                "SELECT version, checksum FROM schema_migrations WHERE version = ?",
                (rs, rowNum) -> new AppliedMigration(rs.getString("version"), rs.getString("checksum")),
                version);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String versionOf(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Migration filename is required");
        }
        int dot = filename.indexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String checksum(String sql) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(sql.getBytes(StandardCharsets.UTF_8)));
    }

    static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String statement = current.toString().trim();
                statements.add(statement.substring(0, statement.length() - 1).trim());
                current.setLength(0);
            }
        }

        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }

    private record AppliedMigration(String version, String checksum) {
    }
}
