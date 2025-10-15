package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.model.entity.Inventario;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import com.sgvi.sistema_ventas.repository.InventarioRepository;
import com.sgvi.sistema_ventas.repository.ProductoRepository;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.service.interfaces.IInventarioService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de inventario.
 * Registra todos los movimientos de stock del sistema.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventarioServiceImpl implements IInventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Inventario registrarMovimiento(Inventario movimiento) {
        log.info("Registrando movimiento de inventario tipo: {}", movimiento.getTipoMovimiento());

        validarMovimiento(movimiento);

        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.calcularStockNuevo();

        Inventario movimientoGuardado = inventarioRepository.save(movimiento);
        log.info("Movimiento registrado con ID: {}", movimientoGuardado.getIdMovimiento());

        return movimientoGuardado;
    }

    @Override
    public Inventario registrarEntrada(Long idProducto, Integer cantidad, Long idUsuario, String observacion) {
        log.info("Registrando ENTRADA de {} unidades para producto ID: {}", cantidad, idProducto);

        Producto producto = obtenerProducto(idProducto);
        Usuario usuario = obtenerUsuario(idUsuario);

        Inventario movimiento = Inventario.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .stockAnterior(producto.getStock())
                .usuario(usuario)
                .observacion(observacion)
                .build();

        movimiento.calcularStockNuevo();

        // Actualizar stock del producto
        producto.setStock(movimiento.getStockNuevo());
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);

        return registrarMovimiento(movimiento);
    }

    @Override
    public Inventario registrarSalida(Long idProducto, Integer cantidad, Long idUsuario, Long idVenta) {
        log.info("Registrando SALIDA de {} unidades para producto ID: {}", cantidad, idProducto);

        Producto producto = obtenerProducto(idProducto);
        Usuario usuario = obtenerUsuario(idUsuario);

        // Validar stock suficiente
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Disponible: " + producto.getStock() + ", Solicitado: " + cantidad
            );
        }

        Inventario movimiento = Inventario.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.SALIDA)
                .cantidad(cantidad)
                .stockAnterior(producto.getStock())
                .usuario(usuario)
                .observacion("Venta ID: " + idVenta)
                .build();

        movimiento.calcularStockNuevo();

        // NO actualizamos el stock aquí porque ya se actualiza en VentaService
        // Este método solo registra el movimiento

        return registrarMovimiento(movimiento);
    }

    @Override
    public Inventario registrarAjuste(Long idProducto, Integer nuevoStock, Long idUsuario, String observacion) {
        log.info("Registrando AJUSTE para producto ID: {}. Nuevo stock: {}", idProducto, nuevoStock);

        Producto producto = obtenerProducto(idProducto);
        Usuario usuario = obtenerUsuario(idUsuario);

        if (nuevoStock < 0) {
            throw new ValidationException("El nuevo stock no puede ser negativo");
        }

        Inventario movimiento = Inventario.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.AJUSTE)
                .cantidad(nuevoStock)
                .stockAnterior(producto.getStock())
                .stockNuevo(nuevoStock)
                .usuario(usuario)
                .observacion(observacion)
                .build();

        // Actualizar stock del producto
        producto.setStock(nuevoStock);
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);

        return registrarMovimiento(movimiento);
    }

    @Override
    public Inventario registrarDevolucion(Long idProducto, Integer cantidad, Long idUsuario, String observacion) {
        log.info("Registrando DEVOLUCIÓN de {} unidades para producto ID: {}", cantidad, idProducto);

        Producto producto = obtenerProducto(idProducto);
        Usuario usuario = obtenerUsuario(idUsuario);

        Inventario movimiento = Inventario.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.DEVOLUCION)
                .cantidad(cantidad)
                .stockAnterior(producto.getStock())
                .usuario(usuario)
                .observacion(observacion)
                .build();

        movimiento.calcularStockNuevo();

        // Actualizar stock del producto
        producto.setStock(movimiento.getStockNuevo());
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);

        return registrarMovimiento(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inventario> obtenerMovimientosPorProducto(Long idProducto, Pageable pageable) {
        return inventarioRepository.findByProductoId(idProducto, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> obtenerTrazabilidad(Long idProducto) {
        return inventarioRepository.findTrazabilidadProducto(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inventario> buscarMovimientosConFiltros(Long idProducto, TipoMovimiento tipoMovimiento,
                                                        Long idUsuario, LocalDateTime fechaInicio,
                                                        LocalDateTime fechaFin, Pageable pageable) {
        return inventarioRepository.buscarMovimientosConFiltros(
                idProducto, tipoMovimiento, idUsuario, fechaInicio, fechaFin, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> obtenerMovimientosPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return inventarioRepository.findMovimientosPorPeriodo(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerTotalEntradas(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return inventarioRepository.getTotalEntradasProducto(idProducto, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerTotalSalidas(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return inventarioRepository.getTotalSalidasProducto(idProducto, fechaInicio, fechaFin);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Producto obtenerProducto(Long idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));
    }

    private Usuario obtenerUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));
    }

    private void validarMovimiento(Inventario movimiento) {
        if (movimiento.getProducto() == null) {
            throw new ValidationException("El producto es obligatorio");
        }

        if (movimiento.getTipoMovimiento() == null) {
            throw new ValidationException("El tipo de movimiento es obligatorio");
        }

        if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
            throw new ValidationException("La cantidad debe ser mayor a cero");
        }

        if (!movimiento.validarCantidad()) {
            throw new ValidationException("Cantidad inválida para el tipo de movimiento");
        }
    }
}
