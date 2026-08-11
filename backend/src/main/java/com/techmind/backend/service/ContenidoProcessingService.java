package com.techmind.backend.service;

import com.techmind.backend.dto.ContenidoResponse;

/**
 * Contrato entre el Controller (Dev 3) y quien procese el contenido.
 * Fase 1: la implementa MockContenidoService.
 * Fase 2: la implementa ContenidoService (real), que usara OnnxModelService
 * (Dev 2) y ContentRepository (Dev 1) para guardar el historial en Postgres.
 */
public interface ContenidoProcessingService {

    ContenidoResponse procesar(String titulo, String texto);
}
