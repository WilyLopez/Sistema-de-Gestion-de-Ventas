package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.model.dto.common.PageResponseDTO;
import com.sgvi.sistema_ventas.model.dto.common.ResponseDTO;
import com.sgvi.sistema_ventas.model.dto.venta.*;
import com.sgvi.sistema_ventas.model.entity.*;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import com.sgvi.sistema_ventas.model.enums.TipoComprobante;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import com.sgvi.sistema_ventas.repository.*;
import com.sgvi.sistema_ventas.service.interfaces.IVentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation para la gestión de ventas.
 * Implementa la lógica de negocio según RF-007, RF-008, RF-009
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VentaServiceImpl implements IVentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    private static final BigDecimal IGV_PORCENTAJE = BigDecimal.valueOf(0.18);
    private static final int PLAZO_ANULACION_HORAS = 24;

    @Override
    @Transactional
    public ResponseDTO<VentaDTO> registrarVenta(VentaCreateDTO ventaCreateDTO, Long idUsuario) {
        try {
            log.info("Registrando nueva venta para usuario: {}", idUsuario);

            // RF-007: Validaciones previas
            ResponseDTO<Boolean> validacionStock = validarStockSuficiente(ventaCreateDTO.getDetalles());
            if (!validacionStock.isSuccess()) {
                return ResponseDTO.error(validacionStock.getMessage());
            }

            // Obtener entidades relacionadas
            Cliente cliente = clienteRepository.findById(ventaCreateDTO.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            MetodoPago metodoPago = metodoPagoRepository.findById(ventaCreateDTO.getIdMetodoPago())
                    .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

            // RF-007: Calcular totales
            ResponseDTO<BigDecimal> subtotalResponse = calcularSubtotal(ventaCreateDTO.getDetalles());
            BigDecimal subtotal = subtotalResponse.getData();
            BigDecimal igv = calcularIGV(subtotal).getData();
            BigDecimal total = calcularTotal(subtotal, igv).getData();

            // Crear venta
            Venta venta = Venta.builder()
                    .codigoVenta(generarCodigoVenta())
                    .cliente(cliente)
                    .usuario(usuario)
                    .subtotal(subtotal)
                    .igv(igv)
                    .total(total)
                    .metodoPago(metodoPago)
                    .estado(EstadoVenta.PAGADO)
                    .tipoComprobante(TipoComprobante.fromValor(ventaCreateDTO.getTipoComprobante()))
                    .observaciones(ventaCreateDTO.getObservaciones())
                    .build();

            // RF-007: Agregar detalles de venta
            for (DetalleVentaDTO detalleDTO : ventaCreateDTO.getDetalles()) {
                Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleDTO.getIdProducto()));

                DetalleVenta detalle = DetalleVenta.builder()
                        .venta(venta)
                        .producto(producto)
                        .cantidad(detalleDTO.getCantidad())
                        .precioUnitario(detalleDTO.getPrecioUnitario())
                        .descuento(detalleDTO.getDescuento())
                        .build();
                detalle.calcularSubtotal();

                venta.agregarDetalle(detalle);

                // RF-007: Actualizar stock y registrar movimiento
                actualizarStockProducto(producto, detalle.getCantidad(), TipoMovimiento.SALIDA, venta, usuario, "Venta registrada");
            }

            // Guardar venta
            Venta ventaGuardada = ventaRepository.save(venta);

            // RF-007: Generar comprobante
            generarComprobanteAutomatico(ventaGuardada);

            log.info("Venta registrada exitosamente: {}", ventaGuardada.getCodigoVenta());
            return ResponseDTO.success(convertirAVentaDTO(ventaGuardada), "Venta registrada exitosamente");

        } catch (Exception e) {
            log.error("Error al registrar venta: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al registrar venta: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResponseDTO<VentaResumenDTO>> obtenerVentas(VentaBusquedaDTO filtros, Pageable pageable) {
        try {
            log.info("Buscando ventas con filtros");

            Page<Venta> ventasPage = ventaRepository.buscarVentasConFiltros(
                    filtros.getCodigoVenta(),
                    filtros.getIdCliente(),
                    filtros.getIdUsuario(),
                    filtros.getEstado(),
                    filtros.getIdMetodoPago(),
                    filtros.getFechaDesde(),
                    filtros.getFechaHasta(),
                    pageable
            );

            List<VentaResumenDTO> ventasDTO = ventasPage.getContent().stream()
                    .map(this::convertirAVentaResumenDTO)
                    .collect(Collectors.toList());

            PageResponseDTO<VentaResumenDTO> response = PageResponseDTO.of(
                    ventasDTO,
                    ventasPage.getNumber(),
                    ventasPage.getSize(),
                    ventasPage.getTotalElements()
            );

            return ResponseDTO.success(response, "Ventas obtenidas exitosamente");

        } catch (Exception e) {
            log.error("Error al obtener ventas: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al obtener ventas: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO<Void> anularVenta(Long idVenta, String motivo, Long idUsuario) {
        try {
            log.info("Anulando venta: {} por usuario: {}", idVenta, idUsuario);

            // RF-009: Validar que la venta puede ser anulada
            LocalDateTime fechaLimite = LocalDateTime.now().minusHours(PLAZO_ANULACION_HORAS);
            Venta venta = ventaRepository.findVentaAnulable(idVenta, fechaLimite)
                    .orElseThrow(() -> new RuntimeException("La venta no puede ser anulada. Verifique el estado o el plazo de 24 horas."));

            // RF-009: Revertir stock
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            for (DetalleVenta detalle : venta.getDetallesVenta()) {
                actualizarStockProducto(
                        detalle.getProducto(),
                        detalle.getCantidad(),
                        TipoMovimiento.ENTRADA,
                        venta,
                        usuario,
                        "Anulación de venta: " + motivo
                );
            }

            // Actualizar estado de la venta
            venta.setEstado(EstadoVenta.ANULADO);
            venta.setObservaciones("ANULADA - " + motivo + " - " + LocalDateTime.now());
            ventaRepository.save(venta);

            log.info("Venta anulada exitosamente: {}", idVenta);
            return ResponseDTO.success(null, "Venta anulada exitosamente");

        } catch (Exception e) {
            log.error("Error al anular venta: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al anular venta: " + e.getMessage());
        }
    }

    // Métodos auxiliares privados
    private String generarCodigoVenta() {
        String prefijo = "V-" + LocalDateTime.now().getYear() + "-";
        long numeroVentas = ventaRepository.count();
        return prefijo + String.format("%05d", numeroVentas + 1);
    }

    private void actualizarStockProducto(Producto producto, Integer cantidad, TipoMovimiento tipo, Venta venta, Usuario usuario, String observacion) {
        int stockAnterior = producto.getStock();
        int stockNuevo = tipo.calcularNuevoStock(stockAnterior, cantidad);

        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // Registrar movimiento en inventario
        Inventario movimiento = Inventario.builder()
                .producto(producto)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .usuario(usuario)
                .venta(venta)
                .observacion(observacion)
                .build();

        inventarioRepository.save(movimiento);
    }

    private void generarComprobanteAutomatico(Venta venta) {
        String serie = venta.getTipoComprobante().getSerie();
        String ultimoNumero = comprobanteRepository.findLastNumeroBySerie(serie).orElse("00000000");
        String nuevoNumero = String.format("%08d", Integer.parseInt(ultimoNumero) + 1);

        Comprobante comprobante = Comprobante.builder()
                .venta(venta)
                .tipo(venta.getTipoComprobante())
                .serie(serie)
                .numero(nuevoNumero)
                .total(venta.getTotal())
                .igv(venta.getIgv())
                .build();

        comprobanteRepository.save(comprobante);
    }

    // Implementación de otros métodos del interface...
    @Override
    public ResponseDTO<BigDecimal> calcularSubtotal(List<DetalleVentaDTO> detalles) {
        try {
            BigDecimal subtotal = detalles.stream()
                    .map(DetalleVentaDTO::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return ResponseDTO.success(subtotal);
        } catch (Exception e) {
            return ResponseDTO.error("Error al calcular subtotal: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<BigDecimal> calcularIGV(BigDecimal subtotal) {
        try {
            BigDecimal igv = subtotal.multiply(IGV_PORCENTAJE);
            return ResponseDTO.success(igv);
        } catch (Exception e) {
            return ResponseDTO.error("Error al calcular IGV: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<BigDecimal> calcularTotal(BigDecimal subtotal, BigDecimal igv) {
        try {
            BigDecimal total = subtotal.add(igv);
            return ResponseDTO.success(total);
        } catch (Exception e) {
            return ResponseDTO.error("Error al calcular total: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> validarStockSuficiente(List<DetalleVentaDTO> detalles) {
        try {
            for (DetalleVentaDTO detalle : detalles) {
                Producto producto = productoRepository.findById(detalle.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getIdProducto()));

                if (producto.getStock() < detalle.getCantidad()) {
                    return ResponseDTO.error("Stock insuficiente para el producto: " + producto.getNombre());
                }
            }
            return ResponseDTO.success(true);
        } catch (Exception e) {
            return ResponseDTO.error("Error al validar stock: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> validarVentaAnulable(Long idVenta) {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusHours(PLAZO_ANULACION_HORAS);
            boolean anulable = ventaRepository.findVentaAnulable(idVenta, fechaLimite).isPresent();
            return ResponseDTO.success(anulable);
        } catch (Exception e) {
            return ResponseDTO.error("Error al validar venta anulable: " + e.getMessage());
        }
    }

    // Métodos de conversión DTO
    private VentaDTO convertirAVentaDTO(Venta venta) {
        List<DetalleVentaDTO> detallesDTO = venta.getDetallesVenta().stream()
                .map(this::convertirADetalleVentaDTO)
                .collect(Collectors.toList());

        ComprobanteDTO comprobanteDTO = null;
        if (venta.getComprobante() != null) {
            comprobanteDTO = convertirAComprobanteDTO(venta.getComprobante());
        }

        return VentaDTO.builder()
                .idVenta(venta.getIdVenta())
                .codigoVenta(venta.getCodigoVenta())
                .idCliente(venta.getCliente().getIdCliente())
                .nombreCliente(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido())
                .idUsuario(venta.getUsuario().getIdUsuario())
                .nombreUsuario(venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido())
                .fechaVenta(venta.getFechaVenta())
                .subtotal(venta.getSubtotal())
                .igv(venta.getIgv())
                .total(venta.getTotal())
                .idMetodoPago(venta.getMetodoPago().getIdMetodoPago())
                .metodoPago(venta.getMetodoPago().getNombre())
                .estado(venta.getEstado())
                .tipoComprobante(venta.getTipoComprobante())
                .observaciones(venta.getObservaciones())
                .fechaCreacion(venta.getFechaCreacion())
                .detalles(detallesDTO)
                .comprobante(comprobanteDTO)
                .build();
    }

    private DetalleVentaDTO convertirADetalleVentaDTO(DetalleVenta detalle) {
        return DetalleVentaDTO.builder()
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
                .build();
    }

    private ComprobanteDTO convertirAComprobanteDTO(Comprobante comprobante) {
        return ComprobanteDTO.builder()
                .idComprobante(comprobante.getIdComprobante())
                .tipo(comprobante.getTipo().getValor())
                .serie(comprobante.getSerie())
                .numero(comprobante.getNumero())
                .numeroCompleto(comprobante.getNumeroCompleto())
                .fechaEmision(comprobante.getFechaEmision())
                .rucEmisor(comprobante.getRucEmisor())
                .razonSocialEmisor(comprobante.getRazonSocialEmisor())
                .direccionEmisor(comprobante.getDireccionEmisor())
                .igv(comprobante.getIgv())
                .total(comprobante.getTotal())
                .estado(comprobante.getEstado())
                .build();
    }

    private VentaResumenDTO convertirAVentaResumenDTO(Venta venta) {
        return VentaResumenDTO.builder()
                .idVenta(venta.getIdVenta())
                .codigoVenta(venta.getCodigoVenta())
                .nombreCliente(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido())
                .nombreUsuario(venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido())
                .fechaVenta(venta.getFechaVenta())
                .total(venta.getTotal())
                .estado(venta.getEstado())
                .metodoPago(venta.getMetodoPago().getNombre())
                .comprobante(venta.getComprobante() != null ? venta.getComprobante().getNumeroCompleto() : "PENDIENTE")
                .cantidadProductos(venta.getDetallesVenta().size())
                .build();
    }

    // Implementación de otros métodos del interface (simplificados por espacio)
    @Override
    public ResponseDTO<VentaDTO> obtenerVentaPorId(Long idVenta) {
        // Implementación
        return null;
    }

    @Override
    public ResponseDTO<VentaDTO> obtenerVentaPorCodigo(String codigoVenta) {
        // Implementación
        return null;
    }

    @Override
    public ResponseDTO<List<VentaResumenDTO>> obtenerVentasPorCliente(Long idCliente) {
        // Implementación
        return null;
    }

    // ... otros métodos
}