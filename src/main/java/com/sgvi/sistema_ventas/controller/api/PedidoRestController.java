package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import com.sgvi.sistema_ventas.model.entity.Pedido;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import com.sgvi.sistema_ventas.service.interfaces.IPedidoService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de pedidos.
 * Gestión de pedidos de clientes y estado del ciclo de vida.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Endpoints para gestión de pedidos de clientes")
public class PedidoRestController {

    private final IPedidoService pedidoService;

    /**
     * Crear un nuevo pedido
     * POST /api/pedidos
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Crear pedido", description = "Registra un nuevo pedido de cliente")
    public ResponseEntity<Pedido> crearPedido(@RequestBody Map<String, Object> pedidoData) {
        log.info("POST /api/pedidos - Crear pedido");

        // Extraer pedido y detalles del request
        Pedido pedido = extraerPedido(pedidoData);
        List<DetallePedido> detalles = extraerDetallesPedido(pedidoData);

        Pedido pedidoCreado = pedidoService.crearPedido(pedido, detalles);
        return new ResponseEntity<>(pedidoCreado, HttpStatus.CREATED);
    }

    /**
     * Actualizar estado de un pedido
     * PUT /api/pedidos/{id}/estado
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Actualizar estado", description = "Cambia el estado de un pedido")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        log.info("PUT /api/pedidos/{}/estado - Actualizar estado", id);

        EstadoPedido nuevoEstado = EstadoPedido.valueOf(body.get("estado"));
        Pedido pedidoActualizado = pedidoService.actualizarEstado(id, nuevoEstado);

        return ResponseEntity.ok(pedidoActualizado);
    }

    /**
     * Cancelar un pedido
     * PUT /api/pedidos/{id}/cancelar
     */
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Cancelar pedido", description = "Cancela un pedido y registra el motivo")
    public ResponseEntity<Pedido> cancelarPedido(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        log.info("PUT /api/pedidos/{}/cancelar - Cancelar pedido", id);

        String motivo = body.get("motivo");
        Pedido pedidoCancelado = pedidoService.cancelarPedido(id, motivo);

        return ResponseEntity.ok(pedidoCancelado);
    }

    /**
     * Obtener pedido por ID
     * GET /api/pedidos/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido", description = "Obtiene un pedido por su ID")
    public ResponseEntity<Pedido> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/pedidos/{} - Obtener pedido", id);
        Pedido pedido = pedidoService.obtenerPorId(id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Buscar pedidos con filtros
     * GET /api/pedidos/buscar
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar pedidos", description = "Busca pedidos con múltiples filtros")
    public ResponseEntity<Page<Pedido>> buscarConFiltros(
            @RequestParam(required = false) String codigoPedido,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        log.info("GET /api/pedidos/buscar - Buscar con filtros");

        Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                codigoPedido, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtener pedidos atrasados
     * GET /api/pedidos/atrasados
     */
    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Pedidos atrasados", description = "Lista pedidos con fecha de entrega vencida")
    public ResponseEntity<List<Pedido>> obtenerPedidosAtrasados() {
        log.info("GET /api/pedidos/atrasados - Obtener pedidos atrasados");
        List<Pedido> pedidos = pedidoService.obtenerPedidosAtrasados();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtener pedidos por cliente
     * GET /api/pedidos/cliente/{idCliente}
     */
    @GetMapping("/cliente/{idCliente}")
    @Operation(summary = "Pedidos por cliente", description = "Lista todos los pedidos de un cliente")
    public ResponseEntity<Page<Pedido>> obtenerPorCliente(
            @PathVariable Long idCliente,
            Pageable pageable) {

        log.info("GET /api/pedidos/cliente/{} - Pedidos del cliente", idCliente);

        Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                null, idCliente, null, null, null, null, pageable
        );

        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtener pedidos por usuario
     * GET /api/pedidos/usuario/{idUsuario}
     */
    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Pedidos por usuario", description = "Lista todos los pedidos creados por un usuario")
    public ResponseEntity<Page<Pedido>> obtenerPorUsuario(
            @PathVariable Long idUsuario,
            Pageable pageable) {

        log.info("GET /api/pedidos/usuario/{} - Pedidos del usuario", idUsuario);

        Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                null, null, idUsuario, null, null, null, pageable
        );

        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtener pedidos por estado
     * GET /api/pedidos/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Pedidos por estado", description = "Lista pedidos filtrados por estado")
    public ResponseEntity<Page<Pedido>> obtenerPorEstado(
            @PathVariable EstadoPedido estado,
            Pageable pageable) {

        log.info("GET /api/pedidos/estado/{} - Pedidos con estado", estado);

        Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                null, null, null, estado, null, null, pageable
        );

        return ResponseEntity.ok(pedidos);
    }

    /**
     * Generar código de pedido
     * GET /api/pedidos/generar-codigo
     */
    @GetMapping("/generar-codigo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Generar código", description = "Genera un código único para un nuevo pedido")
    public ResponseEntity<Map<String, String>> generarCodigo() {
        log.info("GET /api/pedidos/generar-codigo - Generar código de pedido");
        String codigo = pedidoService.generarCodigoPedido();
        return ResponseEntity.ok(Map.of("codigo", codigo));
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    /**
     * Extrae objeto Pedido del request
     */
    private Pedido extraerPedido(Map<String, Object> data) {
        // TODO: Implementar extracción completa cuando tengas DTOs
        // Por ahora, retorna objeto básico
        Pedido pedido = new Pedido();
        // Aquí irían las conversiones de data a entidad
        return pedido;
    }

    /**
     * Extrae lista de DetallePedido del request
     */
    private List<DetallePedido> extraerDetallesPedido(Map<String, Object> data) {
        // TODO: Implementar extracción cuando tengas DTOs
        return List.of();
    }
}