package com.techmind.backend.ml;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Preprocesamiento de texto alineado con {@code limpiar_texto} del notebook TechMind:
 * minúsculas, sin puntuación, sin dígitos y espacios colapsados.
 */
@Component
public class TextPreprocessor {

    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}\\s]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    public String clean(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String cleaned = text.toLowerCase(Locale.ROOT);
        cleaned = NON_WORD.matcher(cleaned).replaceAll(" ");
        cleaned = DIGITS.matcher(cleaned).replaceAll(" ");
        cleaned = MULTI_SPACE.matcher(cleaned).replaceAll(" ").strip();
        return cleaned;
    }

    /**
     * Combina título y cuerpo como en el notebook ({@code texto_completo}).
     */
    public String combineAndClean(String title, String body) {
        String combined = (title == null ? "" : title) + " " + (body == null ? "" : body);
        return clean(combined);
    }
}
