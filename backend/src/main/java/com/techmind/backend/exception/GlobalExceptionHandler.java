package com.techmind.backend.exception;

import com.techmind.backend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Intercepta las excepciones de todos los controllers de la app.
 * Aqui vive la tarjeta "Validacion y manejo de errores" (Dev 3):
 *  - Campos obligatorios / tipos de datos -> 400
 *  - JSON mal formado -> 400
 *  - Fallos internos (modelo o mock) -> 500
 *  - Cualquier otro error no previsto -> 500
 * Cada rama deja un log (requisito "Logs de errores").
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validacion fallida en la solicitud: {}", detalles);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Errores de validacion en la solicitud",
                detalles
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("JSON de entrada mal formado o con tipo de dato invalido: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "El cuerpo de la solicitud no es un JSON valido o tiene un tipo de dato incorrecto",
                List.of("Revisa que 'titulo' y 'texto' sean cadenas de texto validas")
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ModelProcessingException.class)
    public ResponseEntity<ErrorResponse> handleModelError(ModelProcessingException ex) {
        log.error("Error procesando el contenido: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrio un error al procesar el contenido",
                List.of()
        );
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        log.error("Error inesperado en la API: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrio un error inesperado en el servidor",
                List.of()
        );
        return ResponseEntity.internalServerError().body(error);
    }
}
