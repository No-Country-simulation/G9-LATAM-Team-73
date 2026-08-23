package com.techmind.backend.ml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmind.backend.config.MlProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import ai.onnxruntime.OnnxMap;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Carga los modelos ONNX exportados por Ciencia de Datos (modelo_categoria.onnx y modelo_lenguaje.onnx)
 * y sus diccionarios de mapeo (mapa_categoria.json y mapa_lenguaje.json) para ejecutar inferencia nativa en Java.
 */
@Slf4j
@Service
public class OnnxModelService {


    private final MlProperties mlProperties;
    private final TextPreprocessor textPreprocessor;
    private final KeywordExtractor keywordExtractor;
    private final ObjectMapper objectMapper;

    private OrtEnvironment environment;
    private OrtSession categorySession;
    private OrtSession languageSession;

    private Map<Long, String> categoryMap = new HashMap<>();
    private Map<Long, String> languageMap = new HashMap<>();

    private boolean usingFallback;

    public OnnxModelService(
            MlProperties mlProperties,
            TextPreprocessor textPreprocessor,
            KeywordExtractor keywordExtractor,
            ObjectMapper objectMapper
    ) {
        this.mlProperties = mlProperties;
        this.textPreprocessor = textPreprocessor;
        this.keywordExtractor = keywordExtractor;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException, OrtException {
        ClassPathResource categoryModelRes = new ClassPathResource(mlProperties.getCategoryModelPath());
        ClassPathResource categoryMapRes = new ClassPathResource(mlProperties.getCategoryMapPath());

        if (categoryModelRes.exists()) {
            this.environment = OrtEnvironment.getEnvironment();
            this.categorySession = loadOnnxSession(categoryModelRes);
            this.categoryMap = loadMappingJson(categoryMapRes);

            ClassPathResource langModelRes = new ClassPathResource(mlProperties.getLanguageModelPath());
            ClassPathResource langMapRes = new ClassPathResource(mlProperties.getLanguageMapPath());
            if (langModelRes.exists()) {
                this.languageSession = loadOnnxSession(langModelRes);
                this.languageMap = loadMappingJson(langMapRes);
                log.info("ONNX language model loaded from classpath:{}", mlProperties.getLanguageModelPath());
            }

            this.usingFallback = false;
            log.info("ONNX category model loaded from classpath:{}", mlProperties.getCategoryModelPath());
        } else if (mlProperties.isFallbackEnabled()) {
            this.usingFallback = true;
            log.warn(
                    "ONNX category model missing at classpath:{}. Using keyword fallback classifier.",
                    mlProperties.getCategoryModelPath()
            );
        } else {
            throw new IllegalStateException(
                    "ONNX category model missing at classpath:" + mlProperties.getCategoryModelPath()
                            + " and fallback is disabled."
            );
        }
    }

    @PreDestroy
    void destroy() throws OrtException {
        if (categorySession != null) {
            categorySession.close();
        }
        if (languageSession != null) {
            languageSession.close();
        }
        if (environment != null) {
            environment.close();
        }
    }

    /**
     * Predice categorÃƒÆ’Ã‚Â­a, probabilidad y tags a partir de tÃƒÆ’Ã‚Â­tulo + texto.
     */
    public PredictionResult predict(String title, String text) {
        String cleaned = textPreprocessor.combineAndClean(title, text);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("El texto a clasificar estÃƒÆ’Ã‚Â¡ vacÃƒÆ’Ã‚Â­o tras el preprocesamiento.");
        }

        List<String> tags = keywordExtractor.extract(cleaned);
        if (usingFallback) {
            return predictWithFallback(cleaned, tags);
        }
        return predictWithOnnx(cleaned, tags);
    }

    public boolean isUsingFallback() {
        return usingFallback;
    }

    private PredictionResult predictWithOnnx(String cleanedText, List<String> extractedTags) {
        try {
            String[] inputData = new String[]{cleanedText};
            long catLabelId = -1;
            double categoryProb = 0.89;
            String categoryRaw = "backend";

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(environment, inputData)) {
                Map<String, OnnxTensor> inputs = Map.of("input", inputTensor);
                try (OrtSession.Result result = categorySession.run(inputs)) {
                    catLabelId = extractLabelId(result);
                    categoryRaw = categoryMap.getOrDefault(catLabelId, "backend");
                    categoryProb = extractProbability(result, catLabelId);
                }
            }

            List<String> finalTags = new ArrayList<>(extractedTags);

            // Inferir lenguaje con modelo_lenguaje.onnx si estÃƒÆ’Ã‚Â¡ disponible
            if (languageSession != null) {
                try (OnnxTensor langTensor = OnnxTensor.createTensor(environment, inputData)) {
                    Map<String, OnnxTensor> inputs = Map.of("input", langTensor);
                    try (OrtSession.Result langResult = languageSession.run(inputs)) {
                        long langLabelId = extractLabelId(langResult);
                        String langPredicted = languageMap.get(langLabelId);
                        if (langPredicted != null && !langPredicted.isBlank()) {
                            boolean containsLang = finalTags.stream()
                                    .anyMatch(t -> t.equalsIgnoreCase(langPredicted));
                            if (!containsLang) {
                                finalTags.add(0, langPredicted);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Error running language model inference, continuing with category prediction: {}", ex.getMessage());
                }
            }

            return new PredictionResult(
                    normalizeCategory(categoryRaw),
                    categoryProb,
                    finalTags,
                    "onnx"
            );
        } catch (Exception ex) {
            log.error("Error al ejecutar inferencia ONNX, usando fallback", ex);
            return predictWithFallback(cleanedText, extractedTags);
        }
    }

    private PredictionResult predictWithFallback(String cleanedText, List<String> tags) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("backend", score(cleanedText,
                "spring", "java", "api", "rest", "hibernate", "jpa", "postgresql", "backend", "microservicio"));
        scores.put("frontend", score(cleanedText,
                "react", "angular", "vue", "css", "html", "javascript", "typescript", "frontend", "ui", "ux"));
        scores.put("full stack", score(cleanedText,
                "fullstack", "full stack", "mern", "mean", "end to end"));
        scores.put("data science", score(cleanedText,
                "machine learning", "data science", "pandas", "sklearn", "modelo", "dataset", "onnx", "nlp"));
        scores.put("devops", score(cleanedText,
                "docker", "kubernetes", "ci cd", "devops", "pipeline", "terraform", "oci"));
        scores.put("mobile", score(cleanedText,
                "android", "ios", "flutter", "react native", "mobile", "kotlin", "swift"));

        Map.Entry<String, Double> best = scores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .orElse(Map.entry("backend", 0.35));

        double raw = best.getValue();
        double probability = raw <= 0
                ? 0.35
                : Math.min(0.99, 0.55 + (raw / 10.0));

        return new PredictionResult(normalizeCategory(best.getKey()), probability, tags, "fallback");
    }

    private double score(String text, String... keywords) {
        double total = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                total += keyword.contains(" ") ? 1.5 : 1.0;
            }
        }
        return total;
    }

    private long extractLabelId(OrtSession.Result result) throws OrtException {
        for (Map.Entry<String, ? extends OnnxValue> entry : result) {
            Object value = entry.getValue().getValue();
            if (value instanceof long[] ids && ids.length > 0) {
                return ids[0];
            }
            if (value instanceof int[] ids && ids.length > 0) {
                return ids[0];
            }
            if (value instanceof String[] labelsArr && labelsArr.length > 0) {
                try {
                    return Long.parseLong(labelsArr[0]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    private double extractProbability(OrtSession.Result result, long labelId) throws OrtException {
        for (Map.Entry<String, ? extends OnnxValue> entry : result) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            if (!name.contains("prob") && !name.contains("score")) {
                continue;
            }
            Object value = entry.getValue().getValue();
            if (value instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof OnnxMap map) {
                    try {
                        Object mapVal = map.getValue(); log.info("[DIAGNOSTICO] mapVal tipo: {}", mapVal == null ? "null" : mapVal.getClass().getName());
                        if (mapVal instanceof Map<?, ?> m) { for (Map.Entry<?, ?> me : m.entrySet()) { log.info("[DIAGNOSTICO] llave={} (tipo {}) valor={}", me.getKey(), me.getKey().getClass().getName(), me.getValue()); }
                            Object probObj = m.get(labelId);
                            if (probObj == null) {
                                probObj = m.get(String.valueOf(labelId));
                            }
                            log.info("[DIAGNOSTICO] probObj final: {} para labelId={}", probObj, labelId); if (probObj instanceof Number number) {
                                return clamp(number.doubleValue());
                            }
                        }
                    } catch (OrtException ignored) {
                    }
                }
            } else if (value instanceof float[][] matrix && matrix.length > 0) {
                int idx = (int) labelId;
                if (idx >= 0 && idx < matrix[0].length) {
                    return clamp(matrix[0][idx]);
                }
            } else if (value instanceof float[] vector) {
                int idx = (int) labelId;
                if (idx >= 0 && idx < vector.length) {
                    return clamp(vector[idx]);
                }
            }
        }
        return 0.85;
    }

    private static double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "Backend";
        }
        String lower = category.trim().toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "backend" -> "Backend";
            case "frontend" -> "Frontend";
            case "full stack", "fullstack", "full-stack" -> "Full Stack";
            case "data science", "datascience", "ciencia de datos" -> "Data Science";
            case "devops" -> "DevOps";
            case "mobile" -> "Mobile";
            default -> Character.toUpperCase(category.charAt(0)) + category.substring(1);
        };
    }

    private OrtSession loadOnnxSession(ClassPathResource modelResource) throws IOException, OrtException {
        Path tempModel = Files.createTempFile("techmind-model-", ".onnx");
        try (InputStream in = modelResource.getInputStream()) {
            Files.copy(in, tempModel, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        tempModel.toFile().deleteOnExit();
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        return environment.createSession(tempModel.toString(), options);
    }

    private Map<Long, String> loadMappingJson(ClassPathResource resource) throws IOException {
        if (!resource.exists()) {
            return Map.of();
        }
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, new TypeReference<Map<Long, String>>() {});
        }
    }
}
