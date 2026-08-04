package com.techmind.backend.translation;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Estrategia local cuando OCI Language no está habilitado:
 * detecta idioma y deja el texto sin traducir (pasa el original).
 * Útil para desarrollo y demos sin credenciales OCI.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "techmind.oci.language", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalTranslationService implements TranslationService {


    private final LanguageDetector languageDetector;

    public LocalTranslationService(LanguageDetector languageDetector) {
        this.languageDetector = languageDetector;
    }

    @Override
    public TranslationResult translateIfNeeded(String text) {
        String sourceLanguage = languageDetector.detect(text);
        if ("und".equals(sourceLanguage)) {
            sourceLanguage = "es";
        }
        if ("en".equals(sourceLanguage)) {
            log.debug("English text detected but OCI Language is disabled; skipping translation.");
        }
        return new TranslationResult(text, text, sourceLanguage, false);
    }
}
