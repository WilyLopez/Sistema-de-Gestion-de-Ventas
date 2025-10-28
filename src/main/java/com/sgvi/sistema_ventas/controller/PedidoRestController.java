package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import com.sgvi.sistema_ventas.model.entity.Pedido;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import com.sgvi.sistema_ventas.repository.ClienteRepository;
import com.sgvi.sistema_ventas.repository.ProductoRepository;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.service.interfaces.IPedidoService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de pedidos de clientes.
 * Implementa requisito RF-008 del SRS: Gestión de Pedidos.
 *
 * Funcionalidades:
 * - Creación y actualización de pedidos
 * - Gestión de estados del ciclo de vida
 * - Cancelación con motivos
 * - Búsqueda avanzada con filtros múltiples
 * - Consultas por cliente, usuario y estado
 *
 * @author Wilian Lopez
 * @version 1.1
 * @since 2024
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Endpoints para gestión completa del ciclo de vida de pedidos")
public class PedidoRestController {

    private final IPedidoService pedidoService;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    /**
     * RF-008: Crear un nuevo pedido de cliente.
     * Incluye validación de stock, cálculo de totales y generación de código único.
     *
     * @param pedidoData Datos del pedido con lista de productos
     * @return Pedido creado con código generado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Crear pedido",
            description = "Registra un nuevo pedido de cliente con sus detalles. Valida stock disponible y genera código único automáticamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o stock insuficiente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente, usuario o producto no encontrado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> crearPedido(@RequestBody Map<String, Object> pedidoData) {
        try {
            log.info("POST /api/pedidos - Crear pedido");

            // Extraer y construir el pedido
            Pedido pedido = construirPedido(pedidoData);
            List<DetallePedido> detalles = construirDetalles(pedidoData);

            // Validar que hay al menos un detalle
            if (detalles.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El pedido debe contener al menos un producto"));
            }

            Pedido pedidoCreado = pedidoService.crearPedido(pedido, detalles);

            log.info("Pedido creado exitosamente: {}", pedidoCreado.getCodigoPedido());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(pedidoCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear pedido: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear pedido: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear el pedido"));
        }
    }

    /**
     * RF-008: Actualizar estado de un pedido.
     * Valida transiciones de estado permitidas según reglas de negocio.
     *
     * @param id ID del pedido
     * @param body Nuevo estado y observaciones opcionales
     * @return Pedido con estado actualizado
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Actualizar estado del pedido",
            description = "Cambia el estado del pedido. Valida transiciones permitidas: PENDIENTE → CONFIRMADO → PREPARANDO → ENVIADO → ENTREGADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transición de estado no permitida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado"
            )
    })
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            log.info("PUT /api/pedidos/{}/estado - Actualizar estado", id);

            String estadoStr = body.get("estado");
            if (estadoStr == null || estadoStr.trim().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El estado es obligatorio"));
            }

            EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr);

            Pedido pedidoActualizado = pedidoService.actualizarEstado(id, nuevoEstado);

            // Agregar observaciones si existen
            String observaciones = body.get("observaciones");
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                pedidoActualizado.setObservaciones(observaciones);
            }

            log.info("Estado del pedido {} actualizado a {}", id, nuevoEstado);

            return ResponseEntity.ok(pedidoActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Pedido no encontrado o estado inválido - ID: {} - Error: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar estado del pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al actualizar estado del pedido"));
        }
    }

    /**
     * RF-008: Cancelar un pedido con motivo.
     * Solo puede cancelarse si está en estado no final.
     *
     * @param id ID del pedido
     * @param body Motivo de cancelación
     * @return Pedido cancelado
     */
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Cancelar pedido",
            description = "Cancela un pedido y registra el motivo. Solo permite cancelar pedidos en estados no finales"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido cancelado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El pedido no puede ser cancelado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado"
            )
    })
    public ResponseEntity<?> cancelarPedido(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            log.info("PUT /api/pedidos/{}/cancelar", id);

            String motivo = body.get("motivo");
            if (motivo == null || motivo.trim().length() < 10) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El motivo debe tener al menos 10 caracteres"));
            }

            Pedido pedidoCancelado = pedidoService.cancelarPedido(id, motivo);

            log.info("Pedido {} cancelado exitosamente", id);

            return ResponseEntity.ok(pedidoCancelado);

        } catch (IllegalArgumentException e) {
            log.warn("No se puede cancelar pedido {} - Error: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al cancelar pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al cancelar el pedido"));
        }
    }

    /**
     * RF-008: Obtener pedido por ID con detalles completos.
     *
     * @param id ID del pedido
     * @return Pedido con información completa
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener pedido por ID",
            description = "Obtiene información completa de un pedido incluyendo cliente, productos y estados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/pedidos/{} - Obtener pedido", id);

            Pedido pedido = pedidoService.obtenerPorId(id);

            return ResponseEntity.ok(pedido);

        } catch (IllegalArgumentException e) {
            log.warn("Pedido no encontrado: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Pedido no encontrado con ID: " + id));

        } catch (Exception e) {
            log.error("Error al obtener pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener el pedido"));
        }
    }

    /**
     * RF-008: Buscar pedidos con múltiples filtros opcionales.
     *
     * @param codigoPedido Código del pedido (opcional)
     * @param idCliente ID del cliente (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @param estado Estado del pedido (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de pedidos filtrados
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar pedidos con filtros",
            description = "Busca pedidos aplicando múltiples filtros opcionales. Todos los parámetros son opcionales y combinables"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda completada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos"
            )
    })
    public ResponseEntity<?> buscarConFiltros(
            @RequestParam(required = false) String codigoPedido,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        try {
            log.info("GET /api/pedidos/buscar - Filtros: codigo={}, cliente={}, estado={}",
                    codigoPedido, idCliente, estado);

            // Validar rango de fechas
            if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                    codigoPedido, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
            );

            log.debug("Búsqueda completada: {} pedidos encontrados", pedidos.getTotalElements());

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("Error al buscar pedidos: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar pedidos"));
        }
    }

    /**
     * RF-008: Obtener pedidos atrasados (fecha de entrega vencida).
     *
     * @return Lista de pedidos atrasados
     */
    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Pedidos atrasados",
            description = "Lista pedidos cuya fecha de entrega estimada ya venció y aún no están entregados"
    )
    public ResponseEntity<?> obtenerPedidosAtrasados() {
        try {
            log.info("GET /api/pedidos/atrasados");

            List<Pedido> pedidos = pedidoService.obtenerPedidosAtrasados();

            log.info("Pedidos atrasados encontrados: {}", pedidos.size());

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("Error al obtener pedidos atrasados: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener pedidos atrasados"));
        }
    }

    /**
     * RF-008: Obtener todos los pedidos de un cliente específico.
     *
     * @param idCliente ID del cliente
     * @param pageable Parámetros de paginación
     * @return Página de pedidos del cliente
     */
    @GetMapping("/cliente/{idCliente}")
    @Operation(
            summary = "Pedidos por cliente",
            description = "Lista todos los pedidos realizados por un cliente específico"
    )
    public ResponseEntity<?> obtenerPorCliente(
            @PathVariable Long idCliente,
            Pageable pageable) {

        try {
            log.info("GET /api/pedidos/cliente/{} - Pedidos del cliente", idCliente);

            Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                    null, idCliente, null, null, null, null, pageable
            );

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("Error al obtener pedidos del cliente {}: {}", idCliente, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener pedidos del cliente"));
        }
    }

    /**
     * RF-008: Obtener pedidos creados por un usuario específico.
     *
     * @param idUsuario ID del usuario
     * @param pageable Parámetros de paginación
     * @return Página de pedidos del usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Pedidos por usuario",
            description = "Lista todos los pedidos registrados por un usuario específico"
    )
    public ResponseEntity<?> obtenerPorUsuario(
            @PathVariable Long idUsuario,
            Pageable pageable) {

        try {
            log.info("GET /api/pedidos/usuario/{} - Pedidos del usuario", idUsuario);

            Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                    null, null, idUsuario, null, null, null, pageable
            );

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("Error al obtener pedidos del usuario {}: {}", idUsuario, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener pedidos del usuario"));
        }
    }

    /**
     * RF-008: Obtener pedidos filtrados por estado.
     *
     * @param estado Estado del pedido
     * @param pageable Parámetros de paginación
     * @return Página de pedidos con el estado especificado
     */
    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Pedidos por estado",
            description = "Lista pedidos filtrados por estado específico"
    )
    public ResponseEntity<?> obtenerPorEstado(
            @PathVariable EstadoPedido estado,
            Pageable pageable) {

        try {
            log.info("GET /api/pedidos/estado/{}", estado);

            Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                    null, null, null, estado, null, null, pageable
            );

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("Error al obtener pedidos por estado {}: {}", estado, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener pedidos por estado"));
        }
    }

    /**
     * RF-008: Generar código único para un nuevo pedido.
     *
     * @return Código de pedido generado
     */
    @GetMapping("/generar-codigo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Generar código de pedido",
            description = "Genera un código único para un nuevo pedido (formato: PED-YYYY-NNNNN)"
    )
    public ResponseEntity<Map<String, String>> generarCodigo() {
        try {
            log.info("GET /api/pedidos/generar-codigo");

            String codigo = pedidoService.generarCodigoPedido();

            Map<String, String> response = new HashMap<>();
            response.put("codigo", codigo);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al generar código de pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Listar todos los pedidos con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de todos los pedidos
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los pedidos",
            description = "Lista todos los pedidos del sistema con paginación"
    )
    public ResponseEntity<?> listarTodos(Pageable pageable) {
        try {
            log.info("GET /api/pedidos - Listar todos - Página: {}", pageable.getPageNumber());

            Page<Pedido> pedidos = pedidoService.buscarConFiltros(
                    null, null, null, null, null, null, pageable
            );

            log.debug("Total de pedidos: {}", pedidos.getTotalElements());

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("Error al listar pedidos: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al listar pedidos"));
        }
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    /**
     * Construye entidad Pedido desde el request
     */
    private Pedido construirPedido(Map<String, Object> data) {
        Long idCliente = getLong(data, "idCliente");
        Long idUsuario = getLong(data, "idUsuario");
        String direccionEnvio = getString(data, "direccionEnvio");
        String observaciones = getString(data, "observaciones");
        String fechaEntregaStr = getString(data, "fechaEntrega");

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idCliente));

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio(direccionEnvio);
        pedido.setObservaciones(observaciones);

        if (fechaEntregaStr != null && !fechaEntregaStr.isEmpty()) {
            pedido.setFechaEntrega(LocalDateTime.parse(fechaEntregaStr));
        }

        return pedido;
    }

    /**
     * Construye lista de DetallePedido desde el request
     */
    @SuppressWarnings("unchecked")
    private List<DetallePedido> construirDetalles(Map<String, Object> data) {
        List<DetallePedido> detalles = new ArrayList<>();

        Object detallesObj = data.get("detalles");
        if (detallesObj instanceof List) {
            List<Map<String, Object>> detallesList = (List<Map<String, Object>>) detallesObj;

            for (Map<String, Object> detalleMap : detallesList) {
                Long idProducto = getLong(detalleMap, "idProducto");
                Integer cantidad = getInteger(detalleMap, "cantidad");
                BigDecimal precioUnitario = getBigDecimal(detalleMap, "precioUnitario");

                Producto producto = productoRepository.findById(idProducto)
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

                DetallePedido detalle = new DetallePedido();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.calcularSubtotal();

                detalles.add(detalle);
            }
        }

        return detalles;
    }

    // Métodos utilitarios para extraer datos del Map
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}