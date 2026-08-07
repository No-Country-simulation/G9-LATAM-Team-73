package com.techmind.backend.controller;

import com.techmind.backend.dto.ContenidoRequest;
import com.techmind.backend.dto.ContenidoResponse;
import com.techmind.backend.service.ContenidoProcessingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Expone los endpoints publicos. Depende de la INTERFAZ
 * ContenidoProcessingService, no de una clase concreta, para poder cambiar
 * de Mock a implementacion real sin tocar este archivo (ver Fase 2).
 */
@RestController
@RequestMapping("/contenido")
public class ContenidoController {

    private static final Logger log = LoggerFactory.getLogger(ContenidoController.class);

    private final ContenidoProcessingService contenidoProcessingService;

    public ContenidoController(ContenidoProcessingService contenidoProcessingService) {
        this.contenidoProcessingService = contenidoProcessingService;
    }

    @PostMapping
    public ResponseEntity<ContenidoResponse> procesarContenido(@Valid @RequestBody ContenidoRequest request) {
        log.info("Solicitud recibida - titulo: '{}', longitud del texto: {} caracteres",
                request.getTitulo(), request.getTexto().length());

        ContenidoResponse respuesta = contenidoProcessingService.procesar(request.getTitulo(), request.getTexto());

        log.info("Contenido clasificado como '{}' con probabilidad {}",
                respuesta.getCategoria(), respuesta.getProbabilidad());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
