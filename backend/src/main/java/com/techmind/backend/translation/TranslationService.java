package com.techmind.backend.translation;

/**
 * Contrato para traducir textos técnicos (EN → ES) antes de clasificarlos.
 */
public interface TranslationService {

    /**
     * Detecta el idioma y, si es inglés, traduce a español.
     */
    TranslationResult translateIfNeeded(String text);
}
