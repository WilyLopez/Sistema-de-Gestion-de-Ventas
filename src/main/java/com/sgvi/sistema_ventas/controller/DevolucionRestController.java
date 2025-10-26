package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.devolucion.*;
import com.sgvi.sistema_ventas.model.entity.Devolucion;
import com.sgvi.sistema_ventas.model.entity.DetalleDevolucion;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import com.sgvi.sistema_ventas.service.interfaces.IDevolucionService;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import com.sgvi.sistema_ventas.service.interfaces.IUsuarioService;
import com.sgvi.sistema_ventas.service.interfaces.IVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de devoluciones.
 * RF-013: Gestión de Devoluciones
 * RF-014: Reportes de Devoluciones
 *
 * @author Wilian Lopez
 * @version 2.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/devoluciones")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Devoluciones", description = "Endpoints para gestión de devoluciones de productos")
public class DevolucionRestController {

    private final IDevolucionService devolucionService;
    private final IVentaService ventaService;
    private final IUsuarioService usuarioService;
    private final IProductoService productoService;

    /**
     * RF-013: Crear solicitud de devolución
     * POST /api/devoluciones
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Crear solicitud de devolución",
            description = "Registra una nueva solicitud de devolución con validación de plazo y cantidades"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Devolución creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o fuera de plazo"),
            @ApiResponse(responseCode = "404", description = "Venta o producto no encontrado")
    })
    public ResponseEntity<DevolucionResponseDTO> crearDevolucion(
            @Valid @RequestBody CrearDevolucionRequestDTO request) {

        log.info("POST /api/devoluciones - Crear devolución para venta ID: {}", request.getIdVenta());

        // Obtener entidades
        Venta venta = ventaService.obtenerPorId(request.getIdVenta());
        Usuario usuario = usuarioService.obtenerPorId(request.getIdUsuario());

        // Construir entidad Devolucion
        Devolucion devolucion = Devolucion.builder()
                .venta(venta)
                .usuario(usuario)
                .motivo(request.getMotivo())
                .estado(EstadoDevolucion.PENDIENTE)
                .build();

        // Construir detalles
        List<DetalleDevolucion> detalles = request.getDetalles().stream()
                .map(dto -> DetalleDevolucion.builder()
                        .producto(productoService.obtenerPorId(dto.getIdProducto()))
                        .cantidad(dto.getCantidad())
                        .motivo(dto.getMotivo())
                        .build())
                .collect(Collectors.toList());

        // Crear devolución
        Devolucion devolucionCreada = devolucionService.crearDevolucion(devolucion, detalles);

        // Convertir a DTO de respuesta
        DevolucionResponseDTO response = convertirADevolucionResponseDTO(devolucionCreada);

        log.info("Devolución creada exitosamente con ID: {}", devolucionCreada.getIdDevolucion());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * RF-013: Aprobar devolución
     * PUT /api/devoluciones/{id}/aprobar
     */
    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Aprobar devolución",
            description = "Aprueba una solicitud de devolución pendiente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución aprobada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La devolución no puede ser aprobada en su estado actual"),
            @ApiResponse(responseCode = "404", description = "Devolución no encontrada")
    })
    public ResponseEntity<DevolucionResponseDTO> aprobarDevolucion(
            @Parameter(description = "ID de la devolución") @PathVariable Long id,
            @Valid @RequestBody AprobarDevolucionRequestDTO request) {

        log.info("PUT /api/devoluciones/{}/aprobar - Aprobar devolución", id);

        Devolucion devolucionAprobada = devolucionService.aprobarDevolucion(id, request.getIdUsuario());
        DevolucionResponseDTO response = convertirADevolucionResponseDTO(devolucionAprobada);

        log.info("Devolución {} aprobada exitosamente", id);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Rechazar devolución
     * PUT /api/devoluciones/{id}/rechazar
     */
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Rechazar devolución",
            description = "Rechaza una solicitud de devolución pendiente con motivo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución rechazada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La devolución no puede ser rechazada en su estado actual"),
            @ApiResponse(responseCode = "404", description = "Devolución no encontrada")
    })
    public ResponseEntity<DevolucionResponseDTO> rechazarDevolucion(
            @Parameter(description = "ID de la devolución") @PathVariable Long id,
            @Valid @RequestBody RechazarDevolucionRequestDTO request) {

        log.info("PUT /api/devoluciones/{}/rechazar - Rechazar devolución", id);

        Devolucion devolucionRechazada = devolucionService.rechazarDevolucion(
                id,
                request.getIdUsuario(),
                request.getMotivo()
        );

        DevolucionResponseDTO response = convertirADevolucionResponseDTO(devolucionRechazada);

        log.info("Devolución {} rechazada exitosamente", id);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Completar devolución
     * PUT /api/devoluciones/{id}/completar
     */
    @PutMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Completar devolución",
            description = "Completa una devolución aprobada (actualiza stock y procesa reembolso)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución completada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La devolución debe estar aprobada para completarse"),
            @ApiResponse(responseCode = "404", description = "Devolución no encontrada")
    })
    public ResponseEntity<DevolucionResponseDTO> completarDevolucion(
            @Parameter(description = "ID de la devolución") @PathVariable Long id,
            @Valid @RequestBody CompletarDevolucionRequestDTO request) {

        log.info("PUT /api/devoluciones/{}/completar - Completar devolución", id);

        Devolucion devolucionCompletada = devolucionService.completarDevolucion(id, request.getIdUsuario());
        DevolucionResponseDTO response = convertirADevolucionResponseDTO(devolucionCompletada);

        log.info("Devolución {} completada exitosamente", id);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Obtener devolución por ID
     * GET /api/devoluciones/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener devolución por ID",
            description = "Obtiene los detalles completos de una devolución"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución encontrada"),
            @ApiResponse(responseCode = "404", description = "Devolución no encontrada")
    })
    public ResponseEntity<DevolucionResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la devolución") @PathVariable Long id) {

        log.info("GET /api/devoluciones/{} - Obtener devolución", id);

        Devolucion devolucion = devolucionService.obtenerPorId(id);
        DevolucionResponseDTO response = convertirADevolucionResponseDTO(devolucion);

        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Obtener devoluciones por venta
     * GET /api/devoluciones/venta/{idVenta}
     */
    @GetMapping("/venta/{idVenta}")
    @Operation(
            summary = "Listar devoluciones de una venta",
            description = "Lista todas las devoluciones asociadas a una venta específica"
    )
    @ApiResponse(responseCode = "200", description = "Lista de devoluciones obtenida")
    public ResponseEntity<List<DevolucionResponseDTO>> obtenerPorVenta(
            @Parameter(description = "ID de la venta") @PathVariable Long idVenta) {

        log.info("GET /api/devoluciones/venta/{} - Obtener devoluciones de venta", idVenta);

        List<Devolucion> devoluciones = devolucionService.obtenerPorVenta(idVenta);
        List<DevolucionResponseDTO> response = devoluciones.stream()
                .map(this::convertirADevolucionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Buscar devoluciones con filtros
     * GET /api/devoluciones/buscar
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar devoluciones con filtros",
            description = "Busca devoluciones aplicando múltiples criterios de filtrado"
    )
    @ApiResponse(responseCode = "200", description = "Búsqueda completada")
    public ResponseEntity<Page<DevolucionResponseDTO>> buscarConFiltros(
            @Parameter(description = "ID de la venta") @RequestParam(required = false) Long idVenta,
            @Parameter(description = "ID del cliente") @RequestParam(required = false) Long idCliente,
            @Parameter(description = "ID del usuario") @RequestParam(required = false) Long idUsuario,
            @Parameter(description = "Estado de la devolución") @RequestParam(required = false) EstadoDevolucion estado,
            @Parameter(description = "Fecha inicial") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha final") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        log.info("GET /api/devoluciones/buscar - Buscar con filtros");

        Page<Devolucion> devoluciones = devolucionService.buscarConFiltros(
                idVenta, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
        );

        Page<DevolucionResponseDTO> response = devoluciones.map(this::convertirADevolucionResponseDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Verificar si venta está dentro del plazo
     * GET /api/devoluciones/verificar-plazo/{idVenta}
     */
    @GetMapping("/verificar-plazo/{idVenta}")
    @Operation(
            summary = "Verificar plazo de devolución",
            description = "Verifica si una venta está dentro del plazo de devolución (30 días)"
    )
    @ApiResponse(responseCode = "200", description = "Verificación completada")
    public ResponseEntity<VerificarPlazoResponseDTO> verificarPlazo(
            @Parameter(description = "ID de la venta") @PathVariable Long idVenta) {

        log.info("GET /api/devoluciones/verificar-plazo/{} - Verificar plazo", idVenta);

        Venta venta = ventaService.obtenerPorId(idVenta);
        boolean dentroPlazo = devolucionService.estaDentroPlazo(idVenta);

        LocalDateTime fechaVenta = venta.getFechaCreacion();
        LocalDateTime fechaLimite = fechaVenta.plusDays(30);
        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), fechaLimite);

        VerificarPlazoResponseDTO response = VerificarPlazoResponseDTO.builder()
                .dentroPlazo(dentroPlazo)
                .fechaVenta(fechaVenta)
                .fechaLimite(fechaLimite)
                .diasRestantes((int) diasRestantes)
                .mensaje(dentroPlazo
                        ? "La venta está dentro del plazo de devolución"
                        : "La venta está fuera del plazo de devolución")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * RF-013: Validar cantidad de devolución
     * GET /api/devoluciones/validar-cantidad
     */
    @GetMapping("/validar-cantidad")
    @Operation(
            summary = "Validar cantidad a devolver",
            description = "Valida si la cantidad a devolver es válida (no excede lo vendido ni lo ya devuelto)"
    )
    @ApiResponse(responseCode = "200", description = "Validación completada")
    public ResponseEntity<ValidarCantidadResponseDTO> validarCantidad(
            @Parameter(description = "ID de la venta") @RequestParam Long idVenta,
            @Parameter(description = "ID del producto") @RequestParam Long idProducto,
            @Parameter(description = "Cantidad a devolver") @RequestParam Integer cantidad) {

        log.info("GET /api/devoluciones/validar-cantidad - Venta: {}, Producto: {}, Cantidad: {}",
                idVenta, idProducto, cantidad);

        boolean esValida = devolucionService.validarCantidadDevolucion(idVenta, idProducto, cantidad);

        // Aquí podrías obtener más detalles del servicio si lo implementas
        ValidarCantidadResponseDTO response = ValidarCantidadResponseDTO.builder()
                .cantidadValida(esValida)
                .mensaje(esValida
                        ? "La cantidad es válida para devolución"
                        : "La cantidad excede lo disponible para devolución")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * RF-014: Obtener devoluciones por período
     * GET /api/devoluciones/periodo
     */
    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Devoluciones por período",
            description = "Lista todas las devoluciones en un rango de fechas específico"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<DevolucionResponseDTO>> obtenerPorPeriodo(
            @Parameter(description = "Fecha inicial") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha final") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/devoluciones/periodo - Desde: {} hasta: {}", fechaInicio, fechaFin);

        List<Devolucion> devoluciones = devolucionService.obtenerDevolucionesPorPeriodo(fechaInicio, fechaFin);
        List<DevolucionResponseDTO> response = devoluciones.stream()
                .map(this::convertirADevolucionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * RF-014: Análisis de motivos de devolución
     * GET /api/devoluciones/analisis-motivos
     */
    @GetMapping("/analisis-motivos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Análisis de motivos de devolución",
            description = "Analiza los motivos de devolución más frecuentes en un período"
    )
    @ApiResponse(responseCode = "200", description = "Análisis completado")
    public ResponseEntity<Map<String, Long>> analizarMotivos(
            @Parameter(description = "Fecha inicial") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha final") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/devoluciones/analisis-motivos - Análisis de motivos");

        Map<String, Long> analisis = devolucionService.analizarMotivosDevoluciones(fechaInicio, fechaFin);

        return ResponseEntity.ok(analisis);
    }

    /**
     * RF-014: Productos más devueltos
     * GET /api/devoluciones/productos-mas-devueltos
     */
    @GetMapping("/productos-mas-devueltos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Productos más devueltos",
            description = "Lista los productos con más devoluciones en un período"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<Map<String, Object>>> obtenerProductosMasDevueltos(
            @Parameter(description = "Fecha inicial") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha final") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @Parameter(description = "Límite de resultados") @RequestParam(defaultValue = "10") int limite) {

        log.info("GET /api/devoluciones/productos-mas-devueltos - Top {}", limite);

        List<Map<String, Object>> productos = devolucionService.obtenerProductosMasDevueltos(
                fechaInicio, fechaFin, limite
        );

        return ResponseEntity.ok(productos);
    }

    /**
     * Convierte una entidad Devolucion a DevolucionResponseDTO
     */
    private DevolucionResponseDTO convertirADevolucionResponseDTO(Devolucion devolucion) {
        List<DetalleDevolucionResponseDTO> detallesDTO = devolucion.getDetallesDevolucion().stream()
                .map(this::convertirADetalleDevolucionResponseDTO)
                .collect(Collectors.toList());

        return DevolucionResponseDTO.builder()
                .idDevolucion(devolucion.getIdDevolucion())
                .idVenta(devolucion.getVenta().getIdVenta())
                .idUsuario(devolucion.getUsuario().getIdUsuario())
                .nombreUsuario(devolucion.getUsuario().getNombre())
                .fechaDevolucion(devolucion.getFechaDevolucion())
                .motivo(devolucion.getMotivo())
                .estado(devolucion.getEstado())
                .estadoDescripcion(devolucion.getEstado().getDescripcion())
                .montoDevolucion(devolucion.getMontoDevolucion())
                .detalles(detallesDTO)
                .dentroPlazo(devolucionService.estaDentroPlazo(devolucion.getVenta().getIdVenta()))
                .badgeClass(devolucion.getEstado().getBadgeClass())
                .build();
    }

    /**
     * Convierte una entidad DetalleDevolucion a DetalleDevolucionResponseDTO
     */
    private DetalleDevolucionResponseDTO convertirADetalleDevolucionResponseDTO(DetalleDevolucion detalle) {
        BigDecimal precioUnitario = detalle.getProducto().getPrecioVenta();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(detalle.getCantidad()));

        return DetalleDevolucionResponseDTO.builder()
                .idDetalleDevolucion(detalle.getIdDetalleDevolucion())
                .idProducto(detalle.getProducto().getIdProducto())
                .nombreProducto(detalle.getProducto().getNombre())
                .codigoProducto(detalle.getProducto().getCodigo())
                .cantidad(detalle.getCantidad())
                .precioUnitario(precioUnitario)
                .subtotal(subtotal)
                .motivo(detalle.getMotivo())
                .build();
    }
}