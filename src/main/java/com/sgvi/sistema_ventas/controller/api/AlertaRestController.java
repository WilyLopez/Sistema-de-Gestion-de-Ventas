package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.AlertaStock;
import com.sgvi.sistema_ventas.service.interfaces.IAlertaService;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de alertas de stock.
 * RF-011: Sistema de Alertas de Stock
 *
 * @author Rosita Bustamante
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alertas", description = "Endpoints para gestión de alertas de stock")
public class AlertaRestController {

    private final IAlertaService alertaService;

    /**
     * RF-011: Obtener alertas no leídas
     * GET /api/alertas/no-leidas
     */
    @GetMapping("/no-leidas")
    @Operation(summary = "Alertas no leídas", description = "Lista todas las alertas no leídas")
    public ResponseEntity<Page<AlertaStock>> obtenerAlertasNoLeidas(Pageable pageable) {
        log.info("GET /api/alertas/no-leidas");
        Page<AlertaStock> alertas = alertaService.obtenerAlertasNoLeidas(pageable);
        return ResponseEntity.ok(alertas);
    }

    /**
     * RF-011: Obtener alertas críticas
     * GET /api/alertas/criticas
     */
    @GetMapping("/criticas")
    @Operation(summary = "Alertas críticas", description = "Lista alertas de nivel crítico no leídas")
    public ResponseEntity<List<AlertaStock>> obtenerAlertasCriticas() {
        log.info("GET /api/alertas/criticas");
        List<AlertaStock> alertas = alertaService.obtenerAlertasCriticas();
        return ResponseEntity.ok(alertas);
    }

    /**
     * RF-011: Marcar alerta como leída
     * PUT /api/alertas/{id}/marcar-leida
     */
    @PutMapping("/{id}/marcar-leida")
    @Operation(summary = "Marcar como leída", description = "Marca una alerta como leída")
    public ResponseEntity<Void> marcarComoLeida(
            @PathVariable Long id,
            @RequestParam Long idUsuario) {
        log.info("PUT /api/alertas/{}/marcar-leida", id);
        alertaService.marcarComoLeida(id, idUsuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-011: Buscar alertas con filtros
     * GET /api/alertas/buscar
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar alertas", description = "Busca alertas con filtros")
    public ResponseEntity<Page<AlertaStock>> buscarConFiltros(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) TipoAlerta tipoAlerta,
            @RequestParam(required = false) NivelUrgencia nivelUrgencia,
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        log.info("GET /api/alertas/buscar");

        Page<AlertaStock> alertas = alertaService.buscarAlertasConFiltros(
                idProducto, tipoAlerta, nivelUrgencia, leida, fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(alertas);
    }

    /**
     * RF-011: Contar alertas por nivel de urgencia
     * GET /api/alertas/conteo-por-urgencia
     */
    @GetMapping("/conteo-por-urgencia")
    @Operation(summary = "Conteo por urgencia", description = "Cuenta alertas no leídas por nivel de urgencia")
    public ResponseEntity<Map<NivelUrgencia, Long>> contarPorUrgencia() {
        log.info("GET /api/alertas/conteo-por-urgencia");
        Map<NivelUrgencia, Long> conteo = alertaService.contarAlertasNoLeidasPorUrgencia();
        return ResponseEntity.ok(conteo);
    }

    /**
     * RF-011: Verificar y generar alertas
     * POST /api/alertas/verificar
     */
    @PostMapping("/verificar")
    @Operation(summary = "Verificar alertas", description = "Verifica stock y genera alertas automáticas")
    public ResponseEntity<List<AlertaStock>> verificarYGenerarAlertas() {
        log.info("POST /api/alertas/verificar");
        List<AlertaStock> alertas = alertaService.verificarYGenerarAlertas();
        return ResponseEntity.ok(alertas);
    }
}
