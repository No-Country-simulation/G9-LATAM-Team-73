package com.techmind.backend.ml;

import com.techmind.backend.translation.TranslationResult;
import com.techmind.backend.translation.TranslationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentAnalysisServiceTest {

    @Mock
    private TranslationService translationService;

    @Mock
    private OnnxModelService onnxModelService;

    @InjectMocks
    private ContentAnalysisService contentAnalysisService;

    @Test
    void analyze_returnsCategoryProbabilityAndTags() {
        when(translationService.translateIfNeeded(anyString()))
                .thenReturn(new TranslationResult(
                        "Introduction to Spring Boot",
                        "Introducción a Spring Boot",
                        "en",
                        true
                ));

        when(onnxModelService.predict(eq("Introducción a Spring Boot"), eq("Introducción a Spring Boot")))
                .thenReturn(new PredictionResult(
                        "Backend",
                        0.89,
                        List.of("Java", "Spring Boot", "API REST"),
                        "onnx"
                ));

        ContentAnalysisService.AnalyzedContent result = contentAnalysisService.analyze(
                "Introducción a Spring Boot",
                "Introduction to Spring Boot"
        );

        assertThat(result.category()).isEqualTo("Backend");
        assertThat(result.probability()).isEqualTo(0.89);
        assertThat(result.tags()).containsExactly("Java", "Spring Boot", "API REST");
        assertThat(result.sourceLanguage()).isEqualTo("en");
        assertThat(result.translated()).isTrue();
        assertThat(result.engine()).isEqualTo("onnx");

        verify(translationService).translateIfNeeded("Introduction to Spring Boot");
        verify(onnxModelService).predict("Introducción a Spring Boot", "Introducción a Spring Boot");
    }
}
