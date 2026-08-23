package com.techmind.backend.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmind.backend.config.MlProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del servicio de inferencia ONNX y clasificador de respaldo.
 */
@ExtendWith(MockitoExtension.class)
class OnnxModelServiceTest {

    private OnnxModelService onnxModelService;

    @BeforeEach
    void setUp() throws Exception {
        MlProperties properties = new MlProperties();
        properties.setCategoryModelPath("model/modelo_categoria.onnx");
        properties.setCategoryMapPath("model/mapa_categoria.json");
        properties.setLanguageModelPath("model/modelo_lenguaje.onnx");
        properties.setLanguageMapPath("model/mapa_lenguaje.json");
        properties.setFallbackEnabled(true);

        onnxModelService = new OnnxModelService(
                properties,
                new TextPreprocessor(),
                new KeywordExtractor(),
                new ObjectMapper()
        );
        onnxModelService.init();
    }

    @Test
    void predict_backendContent_returnsCategoryProbabilityAndTags() {
        PredictionResult result = onnxModelService.predict(
                "Introducción a Spring Boot",
                "En este contenido se presentan los conceptos básicos para la creación de APIs REST utilizando Java y Spring Boot."
        );

        assertThat(result.category()).isEqualTo("Backend");
        assertThat(result.probability()).isBetween(0.0, 1.0);
        assertThat(result.probability()).isGreaterThan(0.5);
        assertThat(result.tags()).contains("Java", "Spring Boot", "API REST");
    }

    @Test
    void predict_frontendContent_returnsFrontendCategory() {
        PredictionResult result = onnxModelService.predict(
                "Componentes en React",
                "Aprende a construir interfaces de usuario con React, TypeScript y CSS."
        );

        assertThat(result.category()).isEqualTo("Frontend");
        assertThat(result.probability()).isBetween(0.0, 1.0);
        assertThat(result.tags()).contains("React", "TypeScript");
    }

    @Test
    void predict_fallbackMode_whenModelMissing() throws Exception {
        MlProperties fallbackProps = new MlProperties();
        fallbackProps.setCategoryModelPath("model/non_existent.onnx");
        fallbackProps.setFallbackEnabled(true);

        OnnxModelService fallbackService = new OnnxModelService(
                fallbackProps,
                new TextPreprocessor(),
                new KeywordExtractor(),
                new ObjectMapper()
        );
        fallbackService.init();

        assertThat(fallbackService.isUsingFallback()).isTrue();

        PredictionResult result = fallbackService.predict("Spring", "Desarrollo backend Java");
        assertThat(result.category()).isEqualTo("Backend");
        assertThat(result.engine()).isEqualTo("fallback");
    }

    @Test
    void predict_genericNonTechnicalContent_returnsIndeterminado() {
        PredictionResult result = onnxModelService.predict(
                "Receta de cocina",
                "Para preparar arroz con pollo necesitamos arroz, pollo, agua, sal y algunas verduras."
        );

        assertThat(result.category()).isEqualTo("Indeterminado");
        assertThat(result.probability()).isLessThan(0.33);
        assertThat(result.tags()).isEmpty();
    }

    @Test
    void predict_blankText_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> onnxModelService.predict("   ", "!!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vacío");
    }
}
