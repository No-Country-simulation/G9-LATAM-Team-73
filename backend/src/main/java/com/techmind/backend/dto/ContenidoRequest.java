package com.techmind.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON de entrada de POST /contenido, tal como lo pide la guia del
 * hackathon (campos en espanol: titulo, texto).
 *
 * Nota de integracion con Dev 1 (ContentEntity):
 * la entidad de base de datos usa nombres en ingles (title, originalText).
 * Ese mapeo Request(es) -> Entity(en) se hace en la Fase 2, dentro del
 * ContenidoService real, para no mezclar el contrato publico de la API
 * (que debe quedar en espanol, segun el enunciado) con el modelo de datos
 * interno.
 */
public class ContenidoRequest {

    @NotBlank(message = "El campo 'titulo' es obligatorio y no puede estar vacio")
    @Size(max = 255, message = "El campo 'titulo' no puede superar 255 caracteres")
    private String titulo;

    @NotBlank(message = "El campo 'descripcion' es obligatorio y no puede estar vacio")
    @Size(min = 10, max = 5000, message = "El campo 'descripcion' debe tener entre 10 y 5000 caracteres")
    private String texto;

    public ContenidoRequest() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
