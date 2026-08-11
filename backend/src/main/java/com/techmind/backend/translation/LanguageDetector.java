package com.techmind.backend.translation;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Heurística liviana de idioma (es / en) para decidir si hace falta traducción.
 */
@Component
public class LanguageDetector {

    private static final Set<String> SPANISH_MARKERS = Set.of(
            "el", "la", "los", "las", "de", "del", "que", "en", "un", "una", "para", "con",
            "por", "como", "este", "esta", "contenido", "tutorial", "introducción", "introduccion",
            "aplicación", "aplicacion", "usando", "utilizando", "conceptos", "básicos", "basicos"
    );

    private static final Set<String> ENGLISH_MARKERS = Set.of(
            "the", "and", "for", "with", "this", "that", "from", "into", "using", "guide",
            "introduction", "tutorial", "content", "application", "basics", "concepts", "build",
            "create", "rest", "api"
    );

    private static final Pattern TOKEN = Pattern.compile("[\\p{L}]+", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * @return {@code "es"}, {@code "en"} o {@code "und"} (indeterminado)
     */
    public String detect(String text) {
        if (text == null || text.isBlank()) {
            return "und";
        }

        int spanish = 0;
        int english = 0;
        var matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group();
            if (SPANISH_MARKERS.contains(token)) {
                spanish++;
            }
            if (ENGLISH_MARKERS.contains(token)) {
                english++;
            }
        }

        if (spanish == 0 && english == 0) {
            return "und";
        }
        if (english > spanish) {
            return "en";
        }
        if (spanish > english) {
            return "es";
        }
        return "und";
    }
}
