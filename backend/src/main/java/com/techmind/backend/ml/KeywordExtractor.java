package com.techmind.backend.ml;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Extrae etiquetas técnicas (lenguajes / frameworks) del texto limpio
 * para el campo {@code informacion_adicional} de la API.
 */
@Component
public class KeywordExtractor {

    private static final Map<String, Pattern> KEYWORD_PATTERNS = Map.ofEntries(
            entry("Java", "\\bjava\\b"),
            entry("Spring Boot", "\\bspring\\s*boot\\b|\\bspringboot\\b"),
            entry("API REST", "\\bapis?\\s*rest\\b|\\brest\\s*apis?\\b|\\brestful\\b"),
            entry("Python", "\\bpython\\b"),
            entry("JavaScript", "\\bjavascript\\b|\\bjs\\b"),
            entry("TypeScript", "\\btypescript\\b|\\bts\\b"),
            entry("React", "\\breact\\b"),
            entry("Angular", "\\bangular\\b"),
            entry("Vue", "\\bvue(?:\\.?js)?\\b"),
            entry("Node.js", "\\bnode\\.?js\\b|\\bnodejs\\b"),
            entry("Docker", "\\bdocker\\b"),
            entry("Kubernetes", "\\bkubernetes\\b|\\bk8s\\b"),
            entry("PostgreSQL", "\\bpostgresql\\b|\\bpostgres\\b"),
            entry("SQL", "\\bsql\\b"),
            entry("Machine Learning", "\\bmachine\\s*learning\\b|\\bml\\b"),
            entry("Deep Learning", "\\bdeep\\s*learning\\b"),
            entry("ONNX", "\\bonnx\\b"),
            entry("OCI", "\\boci\\b|\\boracle\\s*cloud\\b"),
            entry("Android", "\\bandroid\\b"),
            entry("iOS", "\\bios\\b|\\bswift\\b")
    );

    private static Map.Entry<String, Pattern> entry(String label, String regex) {
        return Map.entry(label, Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS));
    }

    public List<String> extract(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String haystack = text.toLowerCase(Locale.ROOT);
        Set<String> found = new LinkedHashSet<>();
        for (Map.Entry<String, Pattern> entry : KEYWORD_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(haystack).find()) {
                found.add(entry.getKey());
            }
        }
        return new ArrayList<>(found);
    }
}
