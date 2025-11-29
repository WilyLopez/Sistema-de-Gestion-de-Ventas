package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.venta.*;
import com.sgvi.sistema_ventas.model.entity.*;
import com.sgvi.sistema_ventas.repository.ClienteRepository;
import com.sgvi.sistema_ventas.repository.MetodoPagoRepository;
import com.sgvi.sistema_ventas.repository.ProductoRepository;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.service.interfaces.IVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de ventas.
 * Implementa requisitos RF-007, RF-008, RF-009 del SRS.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ventas", description = "Endpoints para gestión de ventas")
public class VentaRestController {

    private final IVentaService ventaService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * RF-007: Registrar nueva venta con sus detalles.
     * Calcula automáticamente subtotal, IGV y total.
     * Actualiza stock de productos automáticamente.
     *
     * @param createDTO DTO con datos de la venta y detalles
     * @return Venta creada con código generado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Registrar venta",
            description = "Registra nueva venta con detalles. Actualiza stock automáticamente y genera código único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Venta registrada exitosamente",
                    content = @Content(schema = @Schema(implementation = Venta.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o stock insuficiente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> registrarVenta(@Valid @RequestBody VentaCreateDTO createDTO) {
        try {
            log.info("POST /api/ventas - Registrar venta para cliente ID: {}", createDTO.getIdCliente());

            Venta venta = convertirAVenta(createDTO);
            List<DetalleVenta> detalles = convertirADetalles(createDTO);

            Venta ventaCreada = ventaService.registrarVenta(venta, detalles);

            // CONVERTIR A DTO ANTES DE DEVOLVER - ESTO EVITA EL LAZY INITIALIZATION
            VentaDTO ventaDTO = convertirAVentaDTO(ventaCreada);

            log.info("Venta registrada exitosamente: {}", ventaCreada.getCodigoVenta());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ventaDTO); // ← Devuelve el DTO, no la entidad

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al registrar venta: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (IllegalStateException e) {
            log.warn("Stock insuficiente al registrar venta: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al registrar venta: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al registrar venta. Intente nuevamente"));
        }
    }
    /**
     * RF-008: Obtener venta por ID con todos sus detalles.
     *
     * @param id ID de la venta
     * @return Venta encontrada
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener venta por ID",
            description = "Obtiene una venta específica con todos sus detalles"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada",
                    content = @Content(schema = @Schema(implementation = Venta.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/ventas/{}", id);
            Venta venta = ventaService.obtenerPorId(id);
            VentaDTO ventaDTO = convertirAVentaDTO(venta); // ← Convertir a DTO
            return ResponseEntity.ok(ventaDTO);

        } catch (Exception e) {
            log.error("Error al obtener venta {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Venta no encontrada con ID: " + id));
        }
    }


    /**
     * RF-008: Obtener venta por código único.
     *
     * @param codigo Código de la venta
     * @return Venta encontrada
     */
    @GetMapping("/codigo/{codigoVenta}")
    @Operation(
            summary = "Buscar por código",
            description = "Obtiene una venta por su código único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada"
            )
    })
    public ResponseEntity<?> obtenerPorCodigo(@PathVariable String codigoVenta) {
        try {
            log.info("GET /api/ventas/codigo/{}", codigoVenta);
            Venta venta = ventaService.obtenerPorCodigo(codigoVenta);
            VentaDTO ventaDTO = convertirAVentaDTO(venta); // ← Convertir a DTO
            return ResponseEntity.ok(ventaDTO);

        } catch (Exception e) {
            log.error("Error al obtener venta por código {}: {}", codigoVenta, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Venta no encontrada con código: " + codigoVenta));
        }
    }

    /**
     * RF-008: Listar todas las ventas con paginación.
     *
     * @param pageable Parámetros de paginación (page, size, sort)
     * @return Página de ventas
     */
    @GetMapping
    @Operation(
            summary = "Listar ventas",
            description = "Lista todas las ventas con paginación. Ejemplo: ?page=0&size=20&sort=fechaCreacion,desc"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ventas obtenida exitosamente"
            )
    })
    public ResponseEntity<Page<Venta>> listarTodas(Pageable pageable) {
        log.info("GET /api/ventas - Listar ventas (página: {})", pageable.getPageNumber());
        Page<Venta> ventas = ventaService.listarTodas(pageable);
        return ResponseEntity.ok(ventas);
    }

    /**
     * RF-008: Buscar ventas con múltiples filtros.
     * Todos los parámetros son opcionales.
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar ventas con filtros",
            description = "Busca ventas aplicando múltiples filtros opcionales"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda exitosa"
            )
    })
    public ResponseEntity<Page<VentaDTO>> buscarVentas(@ModelAttribute VentaBusquedaDTO filtros) {
        log.info("GET /api/ventas/buscar - Aplicando filtros DTO");
        Page<VentaDTO> ventas = ventaService.buscarVentasDTOConFiltros(filtros);
        return ResponseEntity.ok(ventas);
    }


    /**
     * RF-009: Anular una venta.
     * Solo permite anular ventas con menos de 24 horas y estado PAGADO.
     * Restaura automáticamente el stock de productos.
     *
     * @param id ID de la venta
     * @param request DTO con motivo de anulación
     * @return Respuesta sin contenido
     */
    @PutMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Anular venta",
            description = "Anula una venta (solo si tiene menos de 24h y estado PAGADO). Restaura stock automáticamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Venta anulada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede anular la venta (fuera de plazo o estado inválido)",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada"
            )
    })
    public ResponseEntity<?> anularVenta(
            @PathVariable Long id,
            @Valid @RequestBody AnularVentaRequest request) {

        try {
            log.info("PUT /api/ventas/{}/anular - Motivo: {}", id, request.getMotivo());

            ventaService.anularVenta(id, request.getMotivo());

            log.info("Venta {} anulada exitosamente", id);

            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            log.warn("No se puede anular venta {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al anular venta {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al anular venta"));
        }
    }

    /**
     * RF-009: Verificar si una venta puede ser anulada.
     * Útil para habilitar/deshabilitar botón de anular en frontend.
     *
     * @param id ID de la venta
     * @return Objeto con booleano indicando si puede anularse
     */
    @GetMapping("/{id}/puede-anularse")
    @Operation(
            summary = "Verificar si puede anularse",
            description = "Verifica si una venta cumple condiciones para ser anulada (< 24h y estado PAGADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación exitosa"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada"
            )
    })
    public ResponseEntity<?> puedeAnularse(@PathVariable Long id) {
        try {
            log.info("GET /api/ventas/{}/puede-anularse", id);
            boolean puede = ventaService.puedeAnularse(id);

            Map<String, Boolean> response = new HashMap<>();
            response.put("puedeAnularse", puede);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al verificar anulación de venta {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Venta no encontrada"));
        }
    }

    /**
     * RF-014: Obtener ventas por período de fechas.
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de ventas en el período
     */
    @GetMapping("/periodo")
    @Operation(
            summary = "Ventas por período",
            description = "Obtiene todas las ventas dentro de un rango de fechas"
    )
    public ResponseEntity<List<Venta>> obtenerPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/ventas/periodo - Desde: {} Hasta: {}", fechaInicio, fechaFin);

        List<Venta> ventas = ventaService.obtenerVentasPorPeriodo(fechaInicio, fechaFin);
        return ResponseEntity.ok(ventas);
    }

    /**
     * RF-014: Obtener estadísticas de ventas por período.
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Mapa con estadísticas (total, cantidad)
     */
    @GetMapping("/estadisticas")
    @Operation(
            summary = "Estadísticas de ventas",
            description = "Obtiene total vendido y cantidad de ventas en un período"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/ventas/estadisticas - Período: {} a {}", fechaInicio, fechaFin);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalVendido", ventaService.obtenerTotalVentas(fechaInicio, fechaFin));
        estadisticas.put("cantidadVentas", ventaService.contarVentas(fechaInicio, fechaFin));

        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas de ventas para un vendedor específico.
     * Solo accesible por el propio vendedor o un administrador.
     */
    @GetMapping("/vendedor/estadisticas")
    @PreAuthorize("#idUsuario == authentication.principal.id or hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Estadísticas de Ventas por Vendedor",
            description = "Obtiene total vendido y cantidad de ventas para un vendedor específico en un rango de fechas."
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasVendedor(
            @RequestParam Long idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        log.info("GET /api/ventas/vendedor/estadisticas - Vendedor ID: {}, Período: {} a {}", idUsuario, fechaInicio, fechaFin);

        Map<String, Object> estadisticas = ventaService.obtenerEstadisticasVendedor(idUsuario, fechaInicio, fechaFin);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Convierte VentaCreateDTO a entidad Venta.
     *
     * @param createDTO DTO de creación
     * @return Entidad Venta
     */
    private Venta convertirAVenta(VentaCreateDTO createDTO) {
        Venta venta = new Venta();

        Cliente cliente = clienteRepository.findById(createDTO.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        MetodoPago metodoPago = metodoPagoRepository.findById(createDTO.getIdMetodoPago())
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        Usuario usuario = usuarioRepository.findById(createDTO.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        venta.setCliente(cliente);
        venta.setMetodoPago(metodoPago);
        venta.setUsuario(usuario);
        venta.setObservaciones(createDTO.getObservaciones());

        if (createDTO.getSubtotal() != null) {
            venta.setSubtotal(createDTO.getSubtotal());
        }
        if (createDTO.getTotal() != null) {
            venta.setTotal(createDTO.getTotal());
        }

        return venta;
    }

    /**
     * Convierte lista de DetalleVentaDTO a entidades DetalleVenta.
     * Calcula subtotales de cada detalle.
     *
     * @param createDTO DTO de creación
     * @return Lista de DetalleVenta
     */
    private List<DetalleVenta> convertirADetalles(VentaCreateDTO createDTO) {
        return createDTO.getDetalles().stream()
                .map(detalleDTO -> {
                    detalleDTO.calcularSubtotal();

                    DetalleVenta detalle = new DetalleVenta();

                    // Obtener producto completo desde repo
                    Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    detalle.setProducto(producto);
                    detalle.setCantidad(detalleDTO.getCantidad());
                    detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
                    detalle.setDescuento(detalleDTO.getDescuento());
                    detalle.setSubtotal(detalleDTO.getSubtotal());

                    return detalle;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convierte entidad Venta a VentaDTO para evitar problemas de serialización.
     */
    private VentaDTO convertirAVentaDTO(Venta venta) {
        return VentaDTO.builder()
                .idVenta(venta.getIdVenta())
                .codigoVenta(venta.getCodigoVenta())
                .idCliente(venta.getCliente().getIdCliente())
                .nombreCliente(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido())
                .idUsuario(venta.getUsuario().getIdUsuario())
                .nombreUsuario(venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido())
                .fechaCreacion(venta.getFechaCreacion())
                .subtotal(venta.getSubtotal())
                .igv(venta.getIgv())
                .total(venta.getTotal())
                .idMetodoPago(venta.getMetodoPago().getIdMetodoPago())
                .nombreMetodoPago(venta.getMetodoPago().getNombre())
                .estado(venta.getEstado())
                .tipoComprobante(venta.getTipoComprobante())
                .observaciones(venta.getObservaciones())
                .detalles(convertirDetallesAVentaDTO(venta.getDetallesVenta()))
                //.comprobante(convertirComprobanteADTO(venta.getComprobante())) // Si tienes comprobante
                .build();
    }

    /**
     * Convierte lista de DetalleVenta a DetalleVentaDTO.
     */
    private List<DetalleVentaDTO> convertirDetallesAVentaDTO(List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return List.of();
        }

        return detalles.stream()
                .map(detalle -> DetalleVentaDTO.builder()
                        .idDetalle(detalle.getIdDetalle())
                        .idProducto(detalle.getProducto().getIdProducto())
                        .codigoProducto(detalle.getProducto().getCodigo())
                        .nombreProducto(detalle.getProducto().getNombre())
                        .marca(detalle.getProducto().getMarca())
                        .talla(detalle.getProducto().getTalla())
                        .color(detalle.getProducto().getColor())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .descuento(detalle.getDescuento())
                        .subtotal(detalle.getSubtotal())
                        .build())
                .collect(Collectors.toList());
    }

}