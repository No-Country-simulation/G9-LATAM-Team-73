package com.techmind.backend.ml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextPreprocessorTest {

    private TextPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new TextPreprocessor();
    }

    @Test
    void clean_removesPunctuationDigitsAndNormalizesSpaces() {
        String result = preprocessor.clean("  Introducción a Spring Boot 3!!!  APIs-REST 2024  ");

        assertThat(result).isEqualTo("introducción a spring boot apis rest");
    }

    @Test
    void combineAndClean_joinsTitleAndBody() {
        String result = preprocessor.combineAndClean(
                "Introducción a Spring Boot",
                "En este contenido se presentan conceptos de APIs REST."
        );

        assertThat(result).contains("introducción", "spring", "boot", "apis", "rest");
    }

    @Test
    void clean_returnsEmptyForNullOrBlank() {
        assertThat(preprocessor.clean(null)).isEmpty();
        assertThat(preprocessor.clean("   ")).isEmpty();
    }
}
