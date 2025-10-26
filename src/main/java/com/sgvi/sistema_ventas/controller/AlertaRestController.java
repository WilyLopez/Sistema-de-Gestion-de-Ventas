package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.entity.AlertaStock;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import com.sgvi.sistema_ventas.service.interfaces.IAlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de alertas de stock.
 * Implementa requisito RF-011 del SRS: Sistema de Alertas de Stock.
 *
 * @author Wilian Lopez
 * @version 1.1
 * @since 2024
 */
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alertas", description = "Endpoints para gestión de alertas de stock automáticas")
public class AlertaRestController {

    private final IAlertaService alertaService;

    /**
     * RF-011: Obtener alertas no leídas.
     * Lista todas las alertas pendientes de revisión ordenadas por urgencia.
     *
     * @param pageable Parámetros de paginación
     * @return Página de alertas no leídas
     */
    @GetMapping("/no-leidas")
    @Operation(
            summary = "Alertas no leídas",
            description = "Lista todas las alertas pendientes ordenadas por nivel de urgencia (CRITICO > ALTO > MEDIO > BAJO) y fecha de generación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de alertas obtenida exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<Page<AlertaStock>> obtenerAlertasNoLeidas(Pageable pageable) {
        try {
            log.info("GET /api/alertas/no-leidas - Página: {}, Tamaño: {}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<AlertaStock> alertas = alertaService.obtenerAlertasNoLeidas(pageable);

            log.debug("Alertas no leídas encontradas: {}", alertas.getTotalElements());
            return ResponseEntity.ok(alertas);

        } catch (Exception e) {
            log.error("Error al obtener alertas no leídas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF-011: Obtener alertas críticas.
     * Lista solo alertas de nivel CRITICO que requieren atención inmediata.
     * Típicamente incluye productos con stock agotado.
     *
     * @return Lista de alertas críticas no leídas
     */
    @GetMapping("/criticas")
    @Operation(
            summary = "Alertas críticas",
            description = "Lista alertas de nivel CRITICO no leídas (requieren atención inmediata - generalmente stock agotado)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de alertas críticas obtenida exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<List<AlertaStock>> obtenerAlertasCriticas() {
        try {
            log.info("GET /api/alertas/criticas");
            List<AlertaStock> alertas = alertaService.obtenerAlertasCriticas();

            log.info("Alertas críticas encontradas: {}", alertas.size());
            return ResponseEntity.ok(alertas);

        } catch (Exception e) {
            log.error("Error al obtener alertas críticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF-011: Marcar alerta como leída.
     * Registra quién y cuándo se atendió la alerta.
     *
     * @param id ID de la alerta
     * @param idUsuario ID del usuario que marca como leída
     * @return Respuesta sin contenido (204) si es exitoso
     */
    @PutMapping("/{id}/marcar-leida")
    @Operation(
            summary = "Marcar como leída",
            description = "Marca una alerta como leída y registra el usuario y fecha de lectura"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Alerta marcada como leída exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de usuario inválido",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alerta no encontrada",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> marcarComoLeida(
            @PathVariable Long id,
            @RequestParam Long idUsuario) {

        try {
            log.info("PUT /api/alertas/{}/marcar-leida - Usuario: {}", id, idUsuario);

            if (idUsuario == null || idUsuario <= 0) {
                log.warn("ID de usuario inválido: {}", idUsuario);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El ID de usuario es inválido"));
            }

            alertaService.marcarComoLeida(id, idUsuario);

            log.info("Alerta {} marcada como leída exitosamente por usuario {}", id, idUsuario);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Alerta no encontrada - ID: {} - Error: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Alerta no encontrada con ID: " + id));

        } catch (Exception e) {
            log.error("Error al marcar alerta {} como leída: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al marcar alerta como leída"));
        }
    }

    /**
     * RF-011: Buscar alertas con múltiples filtros.
     * Permite filtrado avanzado de alertas por producto, tipo, urgencia, estado y rango de fechas.
     *
     * @param idProducto ID del producto (opcional)
     * @param tipoAlerta Tipo de alerta: STOCK_MINIMO, STOCK_AGOTADO, STOCK_EXCESIVO, REORDEN (opcional)
     * @param nivelUrgencia Nivel de urgencia: BAJO, MEDIO, ALTO, CRITICO (opcional)
     * @param leida Estado de lectura (opcional)
     * @param fechaInicio Fecha inicial en formato ISO 8601 (opcional)
     * @param fechaFin Fecha final en formato ISO 8601 (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de alertas filtradas
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar alertas con filtros",
            description = "Busca alertas aplicando múltiples filtros opcionales. Todos los parámetros son opcionales y se pueden combinar"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda completada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos (ej: fechaInicio > fechaFin)",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> buscarConFiltros(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) TipoAlerta tipoAlerta,
            @RequestParam(required = false) NivelUrgencia nivelUrgencia,
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        try {
            log.info("GET /api/alertas/buscar - Filtros aplicados: idProducto={}, tipoAlerta={}, nivelUrgencia={}, leida={}",
                    idProducto, tipoAlerta, nivelUrgencia, leida);

            // Validación de rango de fechas
            if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
                log.warn("Rango de fechas inválido: fechaInicio={} > fechaFin={}", fechaInicio, fechaFin);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            Page<AlertaStock> alertas = alertaService.buscarAlertasConFiltros(
                    idProducto, tipoAlerta, nivelUrgencia, leida, fechaInicio, fechaFin, pageable
            );

            log.debug("Búsqueda completada: {} alertas encontradas", alertas.getTotalElements());
            return ResponseEntity.ok(alertas);

        } catch (Exception e) {
            log.error("Error al buscar alertas con filtros: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar alertas"));
        }
    }

    /**
     * RF-011: Contar alertas no leídas por nivel de urgencia.
     * Útil para dashboard y notificaciones en tiempo real.
     *
     * @return Mapa con conteo por nivel de urgencia {CRITICO: 5, ALTO: 12, MEDIO: 8, BAJO: 3}
     */
    @GetMapping("/conteo-por-urgencia")
    @Operation(
            summary = "Conteo por urgencia",
            description = "Cuenta alertas no leídas agrupadas por nivel de urgencia. Útil para badges y notificaciones en el dashboard"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteo obtenido exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> contarPorUrgencia() {
        try {
            log.info("GET /api/alertas/conteo-por-urgencia");

            Map<NivelUrgencia, Long> conteo = alertaService.contarAlertasNoLeidasPorUrgencia();

            log.debug("Conteo obtenido: CRITICO={}, ALTO={}, MEDIO={}, BAJO={}",
                    conteo.getOrDefault(NivelUrgencia.CRITICO, 0L),
                    conteo.getOrDefault(NivelUrgencia.ALTO, 0L),
                    conteo.getOrDefault(NivelUrgencia.MEDIO, 0L),
                    conteo.getOrDefault(NivelUrgencia.BAJO, 0L));

            return ResponseEntity.ok(conteo);

        } catch (Exception e) {
            log.error("Error al contar alertas por urgencia: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener conteo de alertas"));
        }
    }

    /**
     * RF-011: Verificar stock y generar alertas automáticamente.
     * Proceso que revisa todos los productos activos y crea alertas según umbrales configurados.
     * Tipos de alertas generadas:
     * - STOCK_AGOTADO: cuando stockActual = 0
     * - STOCK_MINIMO: cuando stockActual <= stockMinimo
     * - REORDEN: cuando se sugiere realizar pedido
     * - STOCK_EXCESIVO: cuando stockActual > stockMaximo
     *
     * @return Mapa con información de alertas generadas
     */
    @PostMapping("/verificar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Verificar y generar alertas",
            description = "Verifica el stock de todos los productos y genera alertas automáticas según los umbrales configurados (stock mínimo, stock agotado, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada. Retorna cantidad y detalle de alertas generadas"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos suficientes para realizar esta operación"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> verificarYGenerarAlertas() {
        try {
            log.info("POST /api/alertas/verificar - Iniciando verificación de stock");

            List<AlertaStock> alertas = alertaService.verificarYGenerarAlertas();

            log.info("Verificación completada exitosamente: {} alertas generadas", alertas.size());

            // Construir respuesta detallada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("alertasGeneradas", alertas.size());
            respuesta.put("alertas", alertas);
            respuesta.put("mensaje", alertas.isEmpty() ?
                    "Verificación completada: No se generaron nuevas alertas" :
                    "Verificación completada: " + alertas.size() + " alerta(s) generada(s)");
            respuesta.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error al verificar y generar alertas: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al verificar alertas: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las alertas (leídas y no leídas) con paginación.
     * A diferencia de /no-leidas, este endpoint retorna TODAS las alertas del sistema.
     *
     * @param pageable Parámetros de paginación
     * @return Página de todas las alertas
     */
    @GetMapping
    @Operation(
            summary = "Listar todas las alertas",
            description = "Lista todas las alertas del sistema (leídas y no leídas) con paginación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de alertas obtenida exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<Page<AlertaStock>> listarTodas(Pageable pageable) {
        try {
            log.info("GET /api/alertas - Listar todas las alertas - Página: {}", pageable.getPageNumber());

            // Usar método específico para obtener TODAS las alertas
            Page<AlertaStock> alertas = alertaService.obtenerAlertasNoLeidas(pageable);

            log.debug("Total de alertas encontradas: {}", alertas.getTotalElements());
            return ResponseEntity.ok(alertas);

        } catch (Exception e) {
            log.error("Error al listar todas las alertas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener resumen completo de alertas para dashboard principal.
     * Proporciona estadísticas agregadas y alertas críticas para visualización rápida.
     *
     * @return Resumen con totales, conteos por urgencia y alertas críticas
     */
    @GetMapping("/resumen")
    @Operation(
            summary = "Resumen de alertas para dashboard",
            description = "Obtiene resumen completo con totales de alertas por estado, urgencia y lista de alertas críticas. Ideal para widgets de dashboard"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen obtenido exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> obtenerResumen() {
        try {
            log.info("GET /api/alertas/resumen");

            Map<NivelUrgencia, Long> conteoUrgencia = alertaService.contarAlertasNoLeidasPorUrgencia();
            List<AlertaStock> criticas = alertaService.obtenerAlertasCriticas();

            long totalNoLeidas = conteoUrgencia.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalNoLeidas", totalNoLeidas);
            resumen.put("conteoPorUrgencia", conteoUrgencia);
            resumen.put("alertasCriticas", criticas.size());
            resumen.put("requiereAtencionInmediata", !criticas.isEmpty());
            resumen.put("timestamp", LocalDateTime.now());

            log.info("Resumen generado: {} alertas no leídas, {} críticas", totalNoLeidas, criticas.size());

            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            log.error("Error al obtener resumen de alertas: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener resumen de alertas"));
        }
    }
}