package com.techmind.backend.service;

import com.techmind.backend.dto.ContenidoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * FASE 1 (legado) - Implementacion simulada. Ya NO es la implementacion
 * activa (ContenidoService la reemplazo en Fase 2, ver @Primary alli).
 * Se conserva para poder probar el Controller de forma aislada, por
 * ejemplo con el perfil "mock" (ver nota en README) si BD u ONNX fallan
 * en un entorno puntual.
 */
@Service
public class MockContenidoService implements ContenidoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(MockContenidoService.class);
    private static final List<String> CATEGORIAS_SIMULADAS = List.of("Backend", "Frontend", "Full Stack");
    private final Random random = new Random();

    @Override
    public ContenidoResponse procesar(String titulo, String texto) {
        log.info("[MOCK] Procesando contenido simulado - titulo: '{}'", titulo);

        String categoria = detectarCategoriaSimulada(texto);
        double probabilidad = Math.round((0.75 + random.nextDouble() * 0.2) * 100.0) / 100.0;
        List<String> palabrasClave = extraerPalabrasSimuladas(texto);

        return new ContenidoResponse(categoria, probabilidad, palabrasClave);
    }

    private String detectarCategoriaSimulada(String texto) {
        String textoLower = texto.toLowerCase();
        if (textoLower.contains("spring") || textoLower.contains("api") || textoLower.contains("servidor")) {
            return "Backend";
        }
        if (textoLower.contains("react") || textoLower.contains("css") || textoLower.contains("html")) {
            return "Frontend";
        }
        return CATEGORIAS_SIMULADAS.get(random.nextInt(CATEGORIAS_SIMULADAS.size()));
    }

    private List<String> extraerPalabrasSimuladas(String texto) {
        return Arrays.stream(texto.split("[\\s,.;:]+"))
                .filter(palabra -> palabra.length() > 4)
                .distinct()
                .limit(5)
                .toList();
    }
}
