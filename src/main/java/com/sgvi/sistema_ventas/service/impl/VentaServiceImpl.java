package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.StockInsuficienteException;
import com.sgvi.sistema_ventas.exception.VentaException;
import com.sgvi.sistema_ventas.model.dto.venta.VentaBusquedaDTO;
import com.sgvi.sistema_ventas.model.dto.venta.VentaDTO;
import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import com.sgvi.sistema_ventas.model.enums.TipoComprobante;
import com.sgvi.sistema_ventas.repository.DetalleVentaRepository;
import com.sgvi.sistema_ventas.repository.VentaRepository;
import com.sgvi.sistema_ventas.service.interfaces.IInventarioService;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import com.sgvi.sistema_ventas.service.interfaces.IVentaService;
import com.sgvi.sistema_ventas.util.CodeGenerator;
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.NumberUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de ventas.
 * Maneja el ciclo completo de ventas incluyendo transacciones, cálculos
 * automáticos
 * de IGV y actualización de inventario.
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
    private final CodeGenerator codeGenerator;
    private final NumberUtil numberUtil;

    /**
     * Registra una nueva venta en el sistema.
     * Valida stock, calcula totales, genera código y actualiza inventario.
     *
     * @param venta    Datos principales de la venta
     * @param detalles Lista de productos y cantidades vendidas
     * @return Venta registrada con todos sus detalles
     * @throws ValidationException        Si los datos de la venta no son válidos
     * @throws StockInsuficienteException Si algún producto no tiene stock
     *                                    suficiente
     */
    @Override
    public Venta registrarVenta(Venta venta, List<DetalleVenta> detalles) {
        log.info("Registrando nueva venta para cliente ID: {}", venta.getCliente().getIdCliente());

        validarVenta(venta, detalles);

        verificarStockDisponible(detalles);

        venta.setCodigoVenta(generarCodigoVenta());

        BigDecimal[] totales = calcularTotales(detalles);
        venta.setSubtotal(totales[0]);
        venta.setIgv(totales[1]);
        venta.setTotal(totales[2]);

        venta.setEstado(EstadoVenta.PAGADO);
        venta.setFechaCreacion(LocalDateTime.now());
        venta.setFechaActualizacion(LocalDateTime.now());

        Venta ventaGuardada = ventaRepository.save(venta);
        log.info("Venta guardada con código: {}", ventaGuardada.getCodigoVenta());

        procesarDetallesVenta(ventaGuardada, detalles);

        log.info("Venta registrada exitosamente: {}", ventaGuardada.getCodigoVenta());
        return ventaGuardada;
    }

    /**
     * Obtiene una venta por su identificador.
     *
     * @param id Identificador de la venta
     * @return Venta encontrada
     * @throws ResourceNotFoundException Si la venta no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorId(Long id) {
        return ventaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    /**
     * Obtiene una venta por su código único.
     *
     * @param codigoVenta Código único de la venta
     * @return Venta encontrada
     * @throws ResourceNotFoundException Si la venta no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorCodigo(String codigoVenta) {
        return ventaRepository.findByCodigoVentaWithDetails(codigoVenta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con código: " + codigoVenta));
    }

    /**
     * Lista todas las ventas con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de ventas
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Venta> listarTodas(Pageable pageable) {
        return ventaRepository.findAll(pageable);
    }

    /**
     * Busca ventas aplicando múltiples filtros opcionales.
     *
     * 
     * @return Página de ventas que coinciden con los filtros
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> buscarVentasDTOConFiltros(VentaBusquedaDTO filtros) {
        Pageable pageable = PageRequest.of(
                filtros.getPagina(),
                filtros.getTamanio(),
                Sort.by(Sort.Direction.fromString(filtros.getDireccion()), filtros.getOrdenarPor()));

        String estadoStr = filtros.getEstado() != null ? filtros.getEstado().name() : null;

        Page<Object[]> resultados = ventaRepository.buscarVentasDTOConFiltrosNativo(
                filtros.getCodigoVenta(),
                filtros.getIdCliente(),
                filtros.getIdUsuario(),
                estadoStr,
                filtros.getIdMetodoPago(),
                filtros.getFechaDesde(),
                filtros.getFechaHasta(),
                pageable);

        return resultados.map(this::mapToVentaDTO);
    }

    /**
     * Anula una venta si cumple las condiciones establecidas.
     * Revierte el stock de todos los productos y registra la devolución en
     * inventario.
     *
     * @param idVenta ID de la venta a anular
     * @param motivo  Motivo de la anulación
     * @throws VentaException Si la venta no puede anularse
     */
    @Override
    public void anularVenta(Long idVenta, String motivo) {
        log.info("Anulando venta ID: {}", idVenta);

        if (!puedeAnularse(idVenta)) {
            throw new VentaException(Constants.ERR_VENTA_NO_ANULABLE +
                    ". Debe estar en estado PAGADO y tener menos de " +
                    Constants.HORAS_PLAZO_ANULACION_VENTA + " horas");
        }

        Venta venta = obtenerPorId(idVenta);

        venta.setEstado(EstadoVenta.ANULADO);
        venta.setObservaciones("ANULADA: " + motivo);
        venta.setFechaActualizacion(LocalDateTime.now());

        ventaRepository.save(venta);

        revertirStockVenta(venta);

        log.info("Venta anulada exitosamente: {}", venta.getCodigoVenta());
    }

    /**
     * Verifica si una venta puede ser anulada.
     * Una venta puede anularse si está en estado PAGADO y tiene menos de 24 horas.
     *
     * @param idVenta ID de la venta
     * @return true si la venta puede anularse
     */
    @Override
    @Transactional(readOnly = true)
    public boolean puedeAnularse(Long idVenta) {
        LocalDateTime fechaLimite = LocalDateTime.now()
                .minusHours(Constants.HORAS_PLAZO_ANULACION_VENTA);
        return ventaRepository.findVentaAnulable(idVenta, fechaLimite).isPresent();
    }

    /**
     * Calcula subtotal, IGV y total de una venta.
     * Utiliza la tasa de IGV definida en las constantes del sistema.
     *
     * @param detalles Lista de detalles de la venta
     * @return Array con [subtotal, igv, total]
     */
    @Override
    public BigDecimal[] calcularTotales(List<DetalleVenta> detalles) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            detalle.calcularSubtotal();
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        BigDecimal igv = numberUtil.calcularIGV(subtotal);
        BigDecimal total = subtotal.add(igv);

        return new BigDecimal[] {
                numberUtil.redondearMoneda(subtotal),
                numberUtil.redondearMoneda(igv),
                numberUtil.redondearMoneda(total)
        };
    }

    /**
     * Genera un código único para la venta.
     * Formato: V-YYYYMMDD-NNNNN
     *
     * @return Código generado
     */
    @Override
    public String generarCodigoVenta() {
        long count = ventaRepository.count();
        return codeGenerator.generarCodigoVenta(count);
    }

    /**
     * Obtiene todas las ventas de un periodo específico.
     *
     * @param fechaInicio Fecha inicial del periodo
     * @param fechaFin    Fecha final del periodo
     * @return Lista de ventas en el periodo
     */
    @Override
    @Transactional(readOnly = true)
    public List<Venta> obtenerVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findVentasPorPeriodo(fechaInicio, fechaFin);
    }

    /**
     * Calcula el total vendido en un periodo.
     *
     * @param fechaInicio Fecha inicial del periodo
     * @param fechaFin    Fecha final del periodo
     * @return Suma total de ventas en el periodo
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.getTotalVentasPorPeriodo(fechaInicio, fechaFin);
    }

    /**
     * Cuenta el número de ventas realizadas en un periodo.
     *
     * @param fechaInicio Fecha inicial del periodo
     * @param fechaFin    Fecha final del periodo
     * @return Cantidad de ventas en el periodo
     */
    @Override
    @Transactional(readOnly = true)
    public Long contarVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.countVentasPorPeriodo(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> obtenerEstadisticasVendedor(Long idUsuario, LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        if (idUsuario == null) {
            throw new ValidationException("El ID del usuario es obligatorio para obtener sus estadísticas.");
        }

        BigDecimal totalVendido = ventaRepository.getTotalVentasPorUsuarioYPeriodo(idUsuario, fechaInicio, fechaFin);
        Long cantidadVentas = ventaRepository.countVentasPorUsuarioYPeriodo(idUsuario, fechaInicio, fechaFin);

        java.util.Map<String, Object> estadisticas = new java.util.HashMap<>();
        estadisticas.put("totalVendido", totalVendido);
        estadisticas.put("cantidadVentas", cantidadVentas);

        return estadisticas;
    }

    /**
     * Verifica que todos los productos tengan stock suficiente.
     *
     * @param detalles Lista de productos a verificar
     * @throws StockInsuficienteException Si algún producto no tiene stock
     *                                    suficiente
     */
    private void verificarStockDisponible(List<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            if (!productoService.verificarStock(detalle.getProducto().getIdProducto(), detalle.getCantidad())) {
                Producto producto = productoService.obtenerPorId(detalle.getProducto().getIdProducto());
                throw new StockInsuficienteException(
                        Constants.ERR_STOCK_INSUFICIENTE + " para producto: " + producto.getNombre() +
                                ". Stock disponible: " + producto.getStock());
            }
        }
    }

    /**
     * Procesa y guarda los detalles de la venta, actualizando stock e inventario.
     *
     * @param venta    Venta guardada
     * @param detalles Lista de detalles a procesar
     */
    private void procesarDetallesVenta(Venta venta, List<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(venta);
            detalle.calcularSubtotal();
            detalleVentaRepository.save(detalle);

            productoService.actualizarStock(
                    detalle.getProducto().getIdProducto(),
                    -detalle.getCantidad());

            inventarioService.registrarSalida(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad(),
                    venta.getUsuario().getIdUsuario(),
                    venta.getIdVenta());
        }
    }

    /**
     * Revierte el stock de todos los productos de una venta anulada.
     *
     * @param venta Venta anulada
     */
    private void revertirStockVenta(Venta venta) {
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdVenta(venta.getIdVenta());
        for (DetalleVenta detalle : detalles) {
            productoService.actualizarStock(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad());

            inventarioService.registrarDevolucion(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad(),
                    venta.getUsuario().getIdUsuario(),
                    "Anulación de venta: " + venta.getCodigoVenta());
        }
    }

    /**
     * Valida que todos los datos de la venta sean correctos.
     *
     * @param venta    Datos de la venta
     * @param detalles Detalles de la venta
     * @throws ValidationException Si algún dato no es válido
     */
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

            if (detalle.getPrecioUnitario() == null
                    || numberUtil.esMenor(detalle.getPrecioUnitario(), BigDecimal.ZERO)
                    || numberUtil.sonIguales(detalle.getPrecioUnitario(), BigDecimal.ZERO)) {
                throw new ValidationException("El precio unitario debe ser mayor a cero");
            }
        }
    }

    private VentaDTO mapToVentaDTO(Object[] result) {
        return VentaDTO.builder()
                .idVenta(((Number) result[0]).longValue())
                .codigoVenta((String) result[1])
                .idCliente(((Number) result[2]).longValue())
                .nombreCliente((String) result[3])
                .idUsuario(((Number) result[4]).longValue())
                .nombreUsuario((String) result[5])
                .fechaCreacion(((java.sql.Timestamp) result[6]).toLocalDateTime())
                .subtotal((BigDecimal) result[7])
                .igv((BigDecimal) result[8])
                .total((BigDecimal) result[9])
                .idMetodoPago(((Number) result[10]).longValue())
                .nombreMetodoPago((String) result[11])
                .estado(convertirEstadoVenta((String) result[12]))
                .tipoComprobante(convertirTipoComprobante((String) result[13]))
                .observaciones((String) result[14])
                .build();
    }

    /**
     * Método auxiliar para convertir String a EstadoVenta de forma segura
     */
    private EstadoVenta convertirEstadoVenta(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return EstadoVenta.fromValor(valor.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("Valor no válido para EstadoVenta: {}, usando valor por defecto", valor);
            return EstadoVenta.PENDIENTE; // Valor por defecto
        }
    }

    /**
     * Método auxiliar para convertir String a TipoComprobante de forma segura
     */
    private TipoComprobante convertirTipoComprobante(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return TipoComprobante.fromValor(valor.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("Valor no válido para TipoComprobante: {}, usando valor por defecto", valor);
            return TipoComprobante.BOLETA; // Valor por defecto
        }
    }

}