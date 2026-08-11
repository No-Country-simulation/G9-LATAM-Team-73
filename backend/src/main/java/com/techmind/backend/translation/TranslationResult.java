package com.techmind.backend.translation;

/**
 * Resultado de la detección / traducción de idioma.
 */
public record TranslationResult(
        String originalText,
        String translatedText,
        String sourceLanguage,
        boolean translated
) {
}
