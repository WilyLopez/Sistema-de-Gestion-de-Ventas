package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.StockInsuficienteException;
import com.sgvi.sistema_ventas.exception.VentaException;
import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import com.sgvi.sistema_ventas.repository.DetalleVentaRepository;
import com.sgvi.sistema_ventas.repository.VentaRepository;
import com.sgvi.sistema_ventas.service.interfaces.IInventarioService;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import com.sgvi.sistema_ventas.service.interfaces.IVentaService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementación del servicio de gestión de ventas.
 * Incluye transacciones, cálculos automáticos y actualización de inventario.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VentaServiceImpl implements IVentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final IProductoService productoService;
    private final IInventarioService inventarioService;

    private static final BigDecimal IGV_RATE = BigDecimal.valueOf(0.18); // 18%

    @Override
    public Venta registrarVenta(Venta venta, List<DetalleVenta> detalles) {
        log.info("Registrando nueva venta para cliente ID: {}", venta.getCliente().getIdCliente());

        // Validaciones
        validarVenta(venta, detalles);

        // Verificar stock disponible para todos los productos
        for (DetalleVenta detalle : detalles) {
            if (!productoService.verificarStock(detalle.getProducto().getIdProducto(), detalle.getCantidad())) {
                Producto producto = productoService.obtenerPorId(detalle.getProducto().getIdProducto());
                throw new StockInsuficienteException(
                        "Stock insuficiente para producto: " + producto.getNombre() +
                                ". Stock disponible: " + producto.getStock()
                );
            }
        }

        // Generar código de venta
        venta.setCodigoVenta(generarCodigoVenta());

        // Calcular totales
        BigDecimal[] totales = calcularTotales(detalles);
        venta.setSubtotal(totales[0]);
        venta.setIgv(totales[1]);
        venta.setTotal(totales[2]);

        // Establecer fecha y estado
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.PAGADO);
        venta.setFechaCreacion(LocalDateTime.now());
        venta.setFechaActualizacion(LocalDateTime.now());

        // Guardar venta
        Venta ventaGuardada = ventaRepository.save(venta);
        log.info("Venta guardada con código: {}", ventaGuardada.getCodigoVenta());

        // Guardar detalles y actualizar stock
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(ventaGuardada);
            detalle.calcularSubtotal();
            detalleVentaRepository.save(detalle);

            // Actualizar stock del producto
            productoService.actualizarStock(
                    detalle.getProducto().getIdProducto(),
                    -detalle.getCantidad() // Negativo porque es salida
            );

            // Registrar movimiento en inventario
            inventarioService.registrarSalida(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad(),
                    venta.getUsuario().getIdUsuario(),
                    ventaGuardada.getIdVenta()
            );
        }

        log.info("Venta registrada exitosamente: {}", ventaGuardada.getCodigoVenta());
        return ventaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorCodigo(String codigoVenta) {
        return ventaRepository.findByCodigoVenta(codigoVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con código: " + codigoVenta));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Venta> listarTodas(Pageable pageable) {
        return ventaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Venta> buscarConFiltros(String codigoVenta, Long idCliente, Long idUsuario,
                                        EstadoVenta estado, Long idMetodoPago,
                                        LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                        Pageable pageable) {
        return ventaRepository.buscarVentasConFiltros(
                codigoVenta, idCliente, idUsuario, estado, idMetodoPago,
                fechaInicio, fechaFin, pageable
        );
    }

    @Override
    public void anularVenta(Long idVenta, String motivo) {
        log.info("Anulando venta ID: {}", idVenta);

        if (!puedeAnularse(idVenta)) {
            throw new VentaException("La venta no puede ser anulada. Debe estar en estado PAGADO y tener menos de 24 horas");
        }

        Venta venta = obtenerPorId(idVenta);

        // Cambiar estado a anulado
        venta.setEstado(EstadoVenta.ANULADO);
        venta.setObservaciones("ANULADA: " + motivo);
        venta.setFechaActualizacion(LocalDateTime.now());

        ventaRepository.save(venta);

        // Revertir stock de todos los productos
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);
        for (DetalleVenta detalle : detalles) {
            productoService.actualizarStock(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad() // Positivo porque se devuelve al stock
            );

            // Registrar devolución en inventario
            inventarioService.registrarDevolucion(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad(),
                    venta.getUsuario().getIdUsuario(),
                    "Anulación de venta: " + venta.getCodigoVenta()
            );
        }

        log.info("Venta anulada exitosamente: {}", venta.getCodigoVenta());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeAnularse(Long idVenta) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusHours(24);
        return ventaRepository.findVentaAnulable(idVenta, fechaLimite).isPresent();
    }

    @Override
    public BigDecimal[] calcularTotales(List<DetalleVenta> detalles) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            detalle.calcularSubtotal();
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        return new BigDecimal[]{subtotal, igv, total};
    }

    @Override
    public String generarCodigoVenta() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = ventaRepository.count() + 1;
        return String.format("V-%s-%05d", fecha, count);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> obtenerVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findVentasPorPeriodo(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.getTotalVentasPorPeriodo(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.countVentasPorPeriodo(fechaInicio, fechaFin);
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    private void validarVenta(Venta venta, List<DetalleVenta> detalles) {
        if (venta.getCliente() == null || venta.getCliente().getIdCliente() == null) {
            throw new ValidationException("El cliente es obligatorio");
        }

        if (venta.getUsuario() == null || venta.getUsuario().getIdUsuario() == null) {
            throw new ValidationException("El vendedor es obligatorio");
        }

        if (venta.getMetodoPago() == null || venta.getMetodoPago().getIdMetodoPago() == null) {
            throw new ValidationException("El método de pago es obligatorio");
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new ValidationException("La venta debe tener al menos un producto");
        }

        for (DetalleVenta detalle : detalles) {
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new ValidationException("La cantidad debe ser mayor a cero");
            }

            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("El precio unitario debe ser mayor a cero");
            }
        }
    }
}

