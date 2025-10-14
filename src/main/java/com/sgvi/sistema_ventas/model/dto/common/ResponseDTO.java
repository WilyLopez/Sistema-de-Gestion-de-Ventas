package com.sgvi.sistema_ventas.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * DTO genérico para respuestas estándar de la API.
 * Envuelve los datos de respuesta, un mensaje y el estado de la operación.
 * @param <T> El tipo de dato que se está devolviendo (payload).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDTO<T> {

    /** Indica si la operación fue exitosa. */
    private boolean success;

    /** Mensaje de la operación (e.g., "Usuario creado exitosamente"). */
    private String message;

    /** El código de estado HTTP (e.g., 200, 201, 400). */
    private int status;

    /** Los datos que se devuelven al cliente. */
    private T data;

    /**
     * Crea un DTO de respuesta exitosa.
     * @param data Los datos a devolver.
     * @param message Mensaje descriptivo.
     * @param status Código de estado HTTP.
     * @return ResponseDTO configurado.
     */
    public static <T> ResponseDTO<T> success(T data, String message, HttpStatus status) {
        return ResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .status(status.value())
                .data(data)
                .build();
    }

    /**
     * Crea un DTO de respuesta con error.
     * @param message Mensaje de error.
     * @param status Código de estado HTTP.
     * @return ResponseDTO configurado.
     */
    public static ResponseDTO<?> error(String message, HttpStatus status) {
        return ResponseDTO.builder()
                .success(false)
                .message(message)
                .status(status.value())
                .data(null)
                .build();
    }
}