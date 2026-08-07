package com.techmind.backend.dto;

import java.util.List;

/**
 * JSON de salida de POST /contenido:
 * { "categoria": "...", "probabilidad": 0.89, "informacionAdicional": [...] }
 */
public class ContenidoResponse {

    private String categoria;
    private double probabilidad;
    private List<String> informacionAdicional;

    public ContenidoResponse(String categoria, double probabilidad, List<String> informacionAdicional) {
        this.categoria = categoria;
        this.probabilidad = probabilidad;
        this.informacionAdicional = informacionAdicional;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getProbabilidad() {
        return probabilidad;
    }

    public List<String> getInformacionAdicional() {
        return informacionAdicional;
    }
}
