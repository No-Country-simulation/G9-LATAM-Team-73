package com.techmind.backend.ml;

import com.techmind.backend.translation.TranslationResult;
import com.techmind.backend.translation.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Orquesta traducción (EN→ES) + inferencia ONNX.
 * Punto de integración pensado para que Dev 3 lo consuma desde el controller.
 */
@Service
@RequiredArgsConstructor
public class ContentAnalysisService {

    private final TranslationService translationService;
    private final OnnxModelService onnxModelService;

    public AnalyzedContent analyze(String title, String text) {
        TranslationResult translation = translationService.translateIfNeeded(text);
        String textForModel = translation.translatedText() != null
                ? translation.translatedText()
                : translation.originalText();

        PredictionResult prediction = onnxModelService.predict(title, textForModel);

        return new AnalyzedContent(
                title,
                translation.originalText(),
                translation.translatedText(),
                translation.sourceLanguage(),
                translation.translated(),
                prediction.category(),
                prediction.probability(),
                prediction.tags(),
                prediction.engine()
        );
    }

    public record AnalyzedContent(
            String title,
            String originalText,
            String translatedText,
            String sourceLanguage,
            boolean translated,
            String category,
            double probability,
            java.util.List<String> tags,
            String engine
    ) {
    }
}
