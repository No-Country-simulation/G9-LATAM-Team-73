package com.techmind.backend.translation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageDetectorTest {

    private final LanguageDetector detector = new LanguageDetector();

    @Test
    void detect_spanishText() {
        String lang = detector.detect(
                "En este contenido se presentan los conceptos básicos para la creación de APIs."
        );
        assertThat(lang).isEqualTo("es");
    }

    @Test
    void detect_englishText() {
        String lang = detector.detect(
                "This tutorial covers the basics for building REST APIs using Spring Boot."
        );
        assertThat(lang).isEqualTo("en");
    }
}
