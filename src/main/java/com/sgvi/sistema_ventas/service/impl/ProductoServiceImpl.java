package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.StockInsuficienteException;
import com.sgvi.sistema_ventas.exception.ValidationException;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.enums.Genero;
import com.sgvi.sistema_ventas.repository.ProductoRepository;
import com.sgvi.sistema_ventas.service.interfaces.IAlertaService;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de productos.
 * Incluye validaciones y generación automática de alertas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final IAlertaService alertaService;

    @Override
    public Producto crear(Producto producto) {
        log.info("Creando producto: {}", producto.getCodigo());

        validarProductoNuevo(producto);

        producto.setEstado(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(5);
        }

        Producto productoCreado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", productoCreado.getIdProducto());

        // Verificar si se debe generar alerta
        if (productoCreado.getStock() <= productoCreado.getStockMinimo()) {
            alertaService.generarAlertaStockBajo(productoCreado.getIdProducto());
        }

        return productoCreado;
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        log.info("Actualizando producto con ID: {}", id);

        Producto productoExistente = obtenerPorId(id);

        // Validar código único si cambió
        if (!productoExistente.getCodigo().equals(producto.getCodigo())
                && existeCodigo(producto.getCodigo())) {
            throw new DuplicateResourceException("El código de producto ya existe: " + producto.getCodigo());
        }

        validarPrecios(producto.getPrecioCompra(), producto.getPrecioVenta());

        // Actualizar campos
        productoExistente.setCodigo(producto.getCodigo());
        productoExistente.setNombre(producto.getNombre());
        productoExistente.setMarca(producto.getMarca());
        productoExistente.setTalla(producto.getTalla());
        productoExistente.setColor(producto.getColor());
        productoExistente.setMaterial(producto.getMaterial());
        productoExistente.setGenero(producto.getGenero());
        productoExistente.setPrecioCompra(producto.getPrecioCompra());
        productoExistente.setPrecioVenta(producto.getPrecioVenta());
        productoExistente.setStockMinimo(producto.getStockMinimo());
        productoExistente.setDescripcion(producto.getDescripcion());
        productoExistente.setImagenUrl(producto.getImagenUrl());
        productoExistente.setIdCategoria(producto.getIdCategoria());
        productoExistente.setIdProveedor(producto.getIdProveedor());
        productoExistente.setFechaActualizacion(LocalDateTime.now());

        Producto productoActualizado = productoRepository.save(productoExistente);
        log.info("Producto actualizado exitosamente: {}", id);

        return productoActualizado;
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando (soft delete) producto con ID: {}", id);

        Producto producto = obtenerPorId(id);
        producto.setEstado(false);
        producto.setFechaActualizacion(LocalDateTime.now());

        productoRepository.save(producto);
        log.info("Producto eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarTodos(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscar(String texto, Pageable pageable) {
        // Implementación simplificada
        List<Producto> productos = productoRepository
                .findByNombreOrDescripcionContainingIgnoreCase(texto)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorCategoria(Long idCategoria, Pageable pageable) {
        List<Producto> productos = productoRepository.findByIdCategoria(idCategoria)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorGenero(Genero genero, Pageable pageable) {
        List<Producto> productos = productoRepository.findByGenero(genero.getValor())
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorMarca(String marca, Pageable pageable) {
        List<Producto> productos = productoRepository.findByMarca(marca)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        List<Producto> productos = productoRepository.findByPrecioVentaBetween(
                        precioMin.doubleValue(),
                        precioMax.doubleValue())
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }


    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosAgotados() {
        return productoRepository.findProductosAgotados();
    }

    @Override
    public Producto actualizarStock(Long idProducto, Integer cantidad) {
        log.info("Actualizando stock del producto ID: {} en cantidad: {}", idProducto, cantidad);

        Producto producto = obtenerPorId(idProducto);
        int stockAnterior = producto.getStock();
        int nuevoStock = stockAnterior + cantidad;

        if (nuevoStock < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Stock actual: " + stockAnterior + ", cantidad solicitada: " + Math.abs(cantidad)
            );
        }

        producto.setStock(nuevoStock);
        producto.setFechaActualizacion(LocalDateTime.now());

        Producto productoActualizado = productoRepository.save(producto);

        // Verificar alertas
        if (nuevoStock <= producto.getStockMinimo()) {
            if (nuevoStock == 0) {
                alertaService.generarAlertaStockAgotado(idProducto);
            } else {
                alertaService.generarAlertaStockBajo(idProducto);
            }
        }

        log.info("Stock actualizado. Anterior: {}, Nuevo: {}", stockAnterior, nuevoStock);

        return productoActualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarStock(Long idProducto, Integer cantidadRequerida) {
        Producto producto = obtenerPorId(idProducto);
        return producto.getStock() >= cantidadRequerida;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularMargenGanancia(Long idProducto) {
        Producto producto = obtenerPorId(idProducto);

        if (producto.getPrecioCompra() == null || producto.getPrecioCompra().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal ganancia = producto.getPrecioVenta().subtract(producto.getPrecioCompra());
        BigDecimal margen = ganancia.divide(producto.getPrecioCompra(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return margen.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    private void validarProductoNuevo(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new ValidationException("El código del producto es obligatorio");
        }

        if (existeCodigo(producto.getCodigo())) {
            throw new DuplicateResourceException("El código de producto ya existe: " + producto.getCodigo());
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del producto es obligatorio");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new ValidationException("El stock debe ser mayor o igual a cero");
        }

        validarPrecios(producto.getPrecioCompra(), producto.getPrecioVenta());
    }

    private void validarPrecios(BigDecimal precioCompra, BigDecimal precioVenta) {
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("El precio de venta debe ser mayor a cero");
        }

        if (precioCompra != null && precioCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El precio de compra no puede ser negativo");
        }

        if (precioCompra != null && precioVenta.compareTo(precioCompra) < 0) {
            throw new ValidationException("El precio de venta no puede ser menor al precio de compra");
        }
    }
}
