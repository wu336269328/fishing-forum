package com.fishforum.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseMigrationRunnerTest {

    @Test
    void splitStatementsIgnoresBlankAndCommentOnlyChunks() {
        String sql = """
                -- add spot fields
                ALTER TABLE fishing_spots ADD COLUMN IF NOT EXISTS best_season VARCHAR(200);

                -- create wiki comments
                CREATE TABLE IF NOT EXISTS wiki_comments (
                    id BIGSERIAL PRIMARY KEY,
                    content TEXT NOT NULL
                );
                """;

        List<String> statements = DatabaseMigrationRunner.splitStatements(sql);

        assertEquals(2, statements.size());
        assertEquals("ALTER TABLE fishing_spots ADD COLUMN IF NOT EXISTS best_season VARCHAR(200)", statements.get(0));
        assertEquals("CREATE TABLE IF NOT EXISTS wiki_comments (\n    id BIGSERIAL PRIMARY KEY,\n    content TEXT NOT NULL\n)", statements.get(1));
    }
}
