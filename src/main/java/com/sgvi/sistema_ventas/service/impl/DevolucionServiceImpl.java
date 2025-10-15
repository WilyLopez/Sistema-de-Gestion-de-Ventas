package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.model.entity.Devolucion;
import com.sgvi.sistema_ventas.model.entity.DetalleDevolucion;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import com.sgvi.sistema_ventas.repository.DetalleDevolucionRepository;
import com.sgvi.sistema_ventas.repository.VentaRepository;
import com.sgvi.sistema_ventas.service.interfaces.IDevolucionService;
import com.sgvi.sistema_ventas.service.interfaces.IInventarioService;
import com.sgvi.sistema_ventas.repository.DevolucionRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de gestión de devoluciones.
 * Incluye validación de plazos y cantidades.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DevolucionServiceImpl implements IDevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final DetalleDevolucionRepository detalleDevolucionRepository;
    private final VentaRepository ventaRepository;
    private final IInventarioService inventarioService;

    private static final int DIAS_PLAZO_DEVOLUCION = 30;

    @Override
    public Devolucion crearDevolucion(Devolucion devolucion, List<DetalleDevolucion> detalles) {
        log.info("Creando devolución para venta ID: {}", devolucion.getVenta().getIdVenta());

        // Validar que la venta existe y está dentro del plazo
        if (!estaDentroPlazo(devolucion.getVenta().getIdVenta())) {
            throw new IllegalArgumentException("La venta está fuera del plazo de devolución (30 días)");
        }

        // Validar cantidades
        for (DetalleDevolucion detalle : detalles) {
            if (!validarCantidadDevolucion(
                    devolucion.getVenta().getIdVenta(),
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad())) {
                throw new ValidationException(
                        "Cantidad de devolución inválida para producto ID: " + detalle.getProducto().getIdProducto()
                );
            }
        }

        devolucion.setFechaDevolucion(LocalDateTime.now());
        devolucion.setEstado(EstadoDevolucion.PENDIENTE);

        Devolucion devolucionGuardada = devolucionRepository.save(devolucion);

        // Guardar detalles
        for (DetalleDevolucion detalle : detalles) {
            detalle.setDevolucion(devolucionGuardada);
            detalleDevolucionRepository.save(detalle);
        }

        devolucionGuardada.calcularMontoDevolucion();
        devolucionRepository.save(devolucionGuardada);

        log.info("Devolución creada exitosamente con ID: {}", devolucionGuardada.getIdDevolucion());
        return devolucionGuardada;
    }

    @Override
    public Devolucion aprobarDevolucion(Long idDevolucion, Long idUsuario) {
        log.info("Aprobando devolución ID: {}", idDevolucion);

        Devolucion devolucion = obtenerPorId(idDevolucion);

        if (!devolucion.puedeModificarse()) {
            throw new IllegalArgumentException("La devolución no puede ser aprobada en su estado actual");
        }

        devolucion.setEstado(EstadoDevolucion.APROBADA);

        Devolucion devolucionActualizada = devolucionRepository.save(devolucion);
        log.info("Devolución aprobada exitosamente");

        return devolucionActualizada;
    }

    @Override
    public Devolucion rechazarDevolucion(Long idDevolucion, Long idUsuario, String motivo) {
        log.info("Rechazando devolución ID: {}", idDevolucion);

        Devolucion devolucion = obtenerPorId(idDevolucion);

        if (!devolucion.puedeModificarse()) {
            throw new IllegalArgumentException("La devolución no puede ser rechazada en su estado actual");
        }

        devolucion.setEstado(EstadoDevolucion.RECHAZADA);
        devolucion.setMotivo(devolucion.getMotivo() + " | RECHAZO: " + motivo);

        Devolucion devolucionActualizada = devolucionRepository.save(devolucion);
        log.info("Devolución rechazada exitosamente");

        return devolucionActualizada;
    }

    @Override
    public Devolucion completarDevolucion(Long idDevolucion, Long idUsuario) {
        log.info("Completando devolución ID: {}", idDevolucion);

        Devolucion devolucion = obtenerPorId(idDevolucion);

        if (!devolucion.puedeProcesarse()) {
            throw new IllegalArgumentException("La devolución debe estar en estado APROBADA para completarse");
        }

        devolucion.setEstado(EstadoDevolucion.COMPLETADA);
        devolucionRepository.save(devolucion);

        // Actualizar stock de productos devueltos
        List<DetalleDevolucion> detalles = detalleDevolucionRepository.findByDevolucionId(idDevolucion);
        for (DetalleDevolucion detalle : detalles) {
            detalle.actualizarStockProducto();

            // Registrar movimiento en inventario
            inventarioService.registrarDevolucion(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad(),
                    idUsuario,
                    "Devolución ID: " + idDevolucion
            );
        }

        log.info("Devolución completada exitosamente");
        return devolucion;
    }

    @Override
    @Transactional(readOnly = true)
    public Devolucion obtenerPorId(Long id) {
        return devolucionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Devolución no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Devolucion> obtenerPorVenta(Long idVenta) {
        return devolucionRepository.findByVentaId(idVenta);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Devolucion> buscarConFiltros(Long idVenta, Long idCliente, Long idUsuario,
                                             EstadoDevolucion estado, LocalDateTime fechaInicio,
                                             LocalDateTime fechaFin, Pageable pageable) {
        return devolucionRepository.buscarDevolucionesConFiltros(
                idVenta, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDentroPlazo(Long idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        LocalDateTime fechaLimite = venta.getFechaVenta().plusDays(DIAS_PLAZO_DEVOLUCION);
        return LocalDateTime.now().isBefore(fechaLimite);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarCantidadDevolucion(Long idVenta, Long idProducto, Integer cantidadDevolver) {
        // Obtener cantidad vendida
        Integer cantidadVendida = detalleDevolucionRepository.getCantidadVendida(idVenta, idProducto)
                .orElse(0);

        if (cantidadVendida == 0) {
            return false;
        }

        // Obtener cantidad ya devuelta
        Integer cantidadYaDevuelta = detalleDevolucionRepository.getCantidadYaDevuelta(idVenta, idProducto);

        // Validar que no exceda la cantidad disponible para devolver
        Integer cantidadDisponible = cantidadVendida - cantidadYaDevuelta;
        return cantidadDevolver > 0 && cantidadDevolver <= cantidadDisponible;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Devolucion> obtenerDevolucionesPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return devolucionRepository.findDevolucionesPorPeriodo(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> analizarMotivosDevoluciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Object[]> resultados = devolucionRepository.getDevolucionesPorMotivo(fechaInicio, fechaFin);
        Map<String, Long> mapa = new HashMap<>();

        for (Object[] resultado : resultados) {
            String motivo = (String) resultado[0];
            Long cantidad = (Long) resultado[1];
            mapa.put(motivo, cantidad);
        }

        return mapa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerProductosMasDevueltos(LocalDateTime fechaInicio,
                                                                  LocalDateTime fechaFin, int limite) {
        List<Object[]> resultados = devolucionRepository.getProductosMasDevueltos(
                fechaInicio, fechaFin, org.springframework.data.domain.PageRequest.of(0, limite)
        );

        List<Map<String, Object>> lista = new java.util.ArrayList<>();
        for (Object[] resultado : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("producto", resultado[0]);
            item.put("vecesDevuelto", resultado[1]);
            lista.add(item);
        }

        return lista;
    }
}