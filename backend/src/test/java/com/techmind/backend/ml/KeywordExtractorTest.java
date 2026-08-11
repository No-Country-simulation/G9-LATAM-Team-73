package com.techmind.backend.ml;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordExtractorTest {

    private final KeywordExtractor extractor = new KeywordExtractor();

    @Test
    void extract_findsTechnicalTags() {
        List<String> tags = extractor.extract(
                "introduccion a spring boot con java y api rest usando postgresql"
        );

        assertThat(tags).contains("Java", "Spring Boot", "API REST", "PostgreSQL");
    }

    @Test
    void extract_returnsEmptyWhenNoKeywords() {
        assertThat(extractor.extract("hola mundo de prueba")).isEmpty();
    }
}
