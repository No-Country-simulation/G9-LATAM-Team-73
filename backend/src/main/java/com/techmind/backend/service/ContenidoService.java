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
import java.util.List;

/**
 * FASE 2 - Implementacion real de ContenidoProcessingService.
 *
 * Orquesta:
 *  1. ContentAnalysisService (Dev 2) -> traduce si hace falta + clasifica con ONNX
 *  2. Arma la respuesta publica en espanol (ContenidoResponse)
 *  3. Persiste el resultado con ContentRepository (Dev 1)
 *
 * @Primary reemplaza a MockContenidoService: a partir de ahora Spring
 * inyecta ESTA clase en ContenidoController. El controller no se toco.
 */
@Service
@Primary
public class ContenidoService implements ContenidoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ContenidoService.class);

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

        guardarHistorial(titulo, analizado);

        boolean indeterminado = "Indeterminado".equalsIgnoreCase(analizado.category());

        return new ContenidoResponse(
                analizado.category(),
                redondear(analizado.probability()),
                indeterminado ? List.of() : analizado.tags()
        );
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
