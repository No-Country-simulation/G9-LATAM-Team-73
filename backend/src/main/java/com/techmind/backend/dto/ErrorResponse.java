package com.techmind.backend.dto;

import java.time.Instant;
import java.util.List;

/**
 * Formato unico de respuesta para todos los errores (400 y 500),
 * para que sea predecible sin importar que endpoint falle.
 */
public class ErrorResponse {

    private final Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String mensaje;
    private List<String> detalles;

    public ErrorResponse(int status, String error, String mensaje, List<String> detalles) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.detalles = detalles;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<String> getDetalles() {
        return detalles;
    }
}
