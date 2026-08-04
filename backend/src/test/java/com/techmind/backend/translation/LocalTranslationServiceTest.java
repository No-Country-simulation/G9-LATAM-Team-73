package com.techmind.backend.translation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalTranslationServiceTest {

    private final LocalTranslationService service = new LocalTranslationService(new LanguageDetector());

    @Test
    void translateIfNeeded_keepsSpanishText() {
        String text = "Introducción a Spring Boot con conceptos básicos.";
        TranslationResult result = service.translateIfNeeded(text);

        assertThat(result.sourceLanguage()).isEqualTo("es");
        assertThat(result.translated()).isFalse();
        assertThat(result.translatedText()).isEqualTo(text);
    }

    @Test
    void translateIfNeeded_detectsEnglishWithoutTranslatingWhenOciDisabled() {
        String text = "This tutorial covers the basics for building REST APIs.";
        TranslationResult result = service.translateIfNeeded(text);

        assertThat(result.sourceLanguage()).isEqualTo("en");
        assertThat(result.translated()).isFalse();
        assertThat(result.translatedText()).isEqualTo(text);
    }
}
