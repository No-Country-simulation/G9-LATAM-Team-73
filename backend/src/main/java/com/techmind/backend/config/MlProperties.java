package com.techmind.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "techmind.ml")
public class MlProperties {

    /**
     * Classpath-relative path to the ONNX category model under {@code src/main/resources}.
     */
    private String categoryModelPath = "model/modelo_categoria.onnx";

    /**
     * Classpath-relative path to the category mapping JSON.
     */
    private String categoryMapPath = "model/mapa_categoria.json";

    /**
     * Classpath-relative path to the ONNX language model under {@code src/main/resources}.
     */
    private String languageModelPath = "model/modelo_lenguaje.onnx";

    /**
     * Classpath-relative path to the language mapping JSON.
     */
    private String languageMapPath = "model/mapa_lenguaje.json";

    /**
     * Legacy model path for backward compatibility.
     */
    private String modelPath = "model/modelo_categoria.onnx";

    /**
     * Legacy labels path for backward compatibility.
     */
    private String labelsPath = "model/labels.json";

    /**
     * When true and the ONNX file is missing, use the keyword heuristic classifier.
     */
    private boolean fallbackEnabled = true;

    public String getCategoryModelPath() {
        return categoryModelPath;
    }

    public void setCategoryModelPath(String categoryModelPath) {
        this.categoryModelPath = categoryModelPath;
    }

    public String getCategoryMapPath() {
        return categoryMapPath;
    }

    public void setCategoryMapPath(String categoryMapPath) {
        this.categoryMapPath = categoryMapPath;
    }

    public String getLanguageModelPath() {
        return languageModelPath;
    }

    public void setLanguageModelPath(String languageModelPath) {
        this.languageModelPath = languageModelPath;
    }

    public String getLanguageMapPath() {
        return languageMapPath;
    }

    public void setLanguageMapPath(String languageMapPath) {
        this.languageMapPath = languageMapPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getLabelsPath() {
        return labelsPath;
    }

    public void setLabelsPath(String labelsPath) {
        this.labelsPath = labelsPath;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public void setFallbackEnabled(boolean fallbackEnabled) {
        this.fallbackEnabled = fallbackEnabled;
    }
}
