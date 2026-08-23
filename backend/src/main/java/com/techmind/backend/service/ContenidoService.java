package com.techmind.backend.service;

import com.techmind.backend.dto.ContenidoResponse;
import com.techmind.backend.exception.ModelProcessingException;
import com.techmind.backend.ml.ContentAnalysisService;
import com.techmind.backend.model.ContentEntity;
import com.techmind.backend.repository.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * FASE 2 - Implementacion real de ContenidoProcessingService.
 *
 * Orquesta:
 *  1. ContentAnalysisService (Dev 2) -> traduce si hace falta + clasifica con ONNX
 *  2. Aplica un umbral de confianza: si la probabilidad es muy baja, la
 *     categoria se reporta como "Indeterminado" en vez de forzar una de
 *     las 3 clases del modelo (idea propuesta por Miguel en el equipo).
 *  3. Arma la respuesta publica en espanol (ContenidoResponse)
 *  4. Persiste el resultado con ContentRepository (Dev 1)
 *
 * @Primary reemplaza a MockContenidoService: a partir de ahora Spring
 * inyecta ESTA clase en ContenidoController. El controller no se toco.
 */
@Service
@Primary
public class ContenidoService implements ContenidoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ContenidoService.class);

    /**
     * Debajo de este umbral, la clasificacion se considera poco confiable
     * (texto ambiguo o sin vocabulario tecnico reconocible) y se reporta
     * como "Indeterminado" en vez de la categoria cruda del modelo.
     * Valor acordado con el equipo tras el analisis de Miguel sobre como
     * se reparte la probabilidad en textos ambiguos.
     */
    private static final double UMBRAL_CONFIANZA = 0.33;
    private static final String CATEGORIA_INDETERMINADA = "Indeterminado";

    private final ContentAnalysisService contentAnalysisService;
    private final ContentRepository contentRepository;

    public ContenidoService(ContentAnalysisService contentAnalysisService, ContentRepository contentRepository) {
        this.contentAnalysisService = contentAnalysisService;
        this.contentRepository = contentRepository;
    }

    @Override
    public ContenidoResponse procesar(String titulo, String texto) {
        ContentAnalysisService.AnalyzedContent analizado;
        try {
            analizado = contentAnalysisService.analyze(titulo, texto);
        } catch (Exception ex) {
            // Cualquier fallo de traduccion/ONNX se traduce a 500
            // (lo captura GlobalExceptionHandler, ya armado en Fase 1).
            throw new ModelProcessingException("Fallo al analizar el contenido", ex);
        }

        // Guardamos en el historial la categoria REAL del modelo (sin el
        // umbral aplicado), para que Data Science pueda seguir analizando
        // el comportamiento crudo del modelo.
        guardarHistorial(titulo, analizado);

        String categoriaFinal = aplicarUmbralConfianza(analizado.category(), analizado.probability());

        return new ContenidoResponse(
                categoriaFinal,
                redondear(analizado.probability()),
                analizado.tags()
        );
    }

    /**
     * Si la probabilidad no alcanza el umbral minimo, la categoria se
     * reporta como Indeterminado en la respuesta publica, sin tocar el
     * historial ni el resultado original del modelo.
     */
    private String aplicarUmbralConfianza(String categoriaOriginal, double probabilidad) {
        if (probabilidad < UMBRAL_CONFIANZA) {
            log.info("Probabilidad {} por debajo del umbral {}. Categoria '{}' reportada como '{}'.",
                    probabilidad, UMBRAL_CONFIANZA, categoriaOriginal, CATEGORIA_INDETERMINADA);
            return CATEGORIA_INDETERMINADA;
        }
        return categoriaOriginal;
    }

    /**
     * Traduce el resultado del analisis (nombres en ingles, propios del
     * dominio de Dev 2) a la entidad de persistencia de Dev 1 (tambien en
     * ingles) y lo guarda. Este es el unico lugar del proyecto donde se
     * "cruzan" los tres mundos: contrato publico en espanol, analisis de
     * Dev 2, y entidad de Dev 1.
     */
    private void guardarHistorial(String titulo, ContentAnalysisService.AnalyzedContent analizado) {
        try {
            ContentEntity entity = ContentEntity.builder()
                    .title(titulo)
                    .originalText(analizado.originalText())
                    .translatedText(analizado.translatedText())
                    .category(analizado.category())
                    .probability(BigDecimal.valueOf(analizado.probability()).setScale(4, RoundingMode.HALF_UP))
                    .sourceLanguage(analizado.sourceLanguage())
                    .processedAt(LocalDateTime.now())
                    .build();

            contentRepository.save(entity);
        } catch (Exception ex) {
            // No dejamos que un fallo al GUARDAR tumbe la respuesta al usuario;
            // el analisis ya se hizo bien, solo se pierde el historial de esta
            // llamada. Se loguea como error para que quede visible en logs/OCI.
            log.error("No se pudo guardar el historial en la base de datos: {}", ex.getMessage(), ex);
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}