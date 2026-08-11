package com.techmind.backend.ml;

import java.util.List;

/**
 * Resultado de la inferencia del modelo de clasificación.
 */
public record PredictionResult(
        String category,
        double probability,
        List<String> tags,
        String engine
) {
}
