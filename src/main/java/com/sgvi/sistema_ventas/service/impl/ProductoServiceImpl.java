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
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.NumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de productos.
 * Incluye validaciones de precios, control de stock y generación automática de alertas.
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
    private final NumberUtil numberUtil;

    /**
     * Crea un nuevo producto en el sistema.
     * Establece valores por defecto, valida precios y genera alertas si es necesario.
     *
     * @param producto Datos del producto a crear
     * @return Producto creado con ID asignado
     * @throws ValidationException Si los datos del producto no son válidos
     * @throws DuplicateResourceException Si el código del producto ya existe
     */
    @Override
    public Producto crear(Producto producto) {
        log.info("Creando producto: {}", producto.getCodigo());

        validarProductoNuevo(producto);

        producto.setEstado(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(Constants.STOCK_MINIMO_DEFAULT);
        }

        Producto productoCreado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", productoCreado.getIdProducto());

        verificarYGenerarAlertas(productoCreado);

        return productoCreado;
    }

    /**
     * Actualiza los datos de un producto existente.
     * Valida cambios en código y precios.
     *
     * @param id Identificador del producto
     * @param producto Nuevos datos del producto
     * @return Producto actualizado
     * @throws ResourceNotFoundException Si el producto no existe
     * @throws DuplicateResourceException Si el nuevo código ya existe
     */
    @Override
    public Producto actualizar(Long id, Producto producto) {
        log.info("Actualizando producto con ID: {}", id);

        Producto productoExistente = obtenerPorId(id);

        if (!productoExistente.getCodigo().equals(producto.getCodigo())
                && existeCodigo(producto.getCodigo())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        validarPrecios(producto.getPrecioCompra(), producto.getPrecioVenta());

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

    /**
     * Realiza una eliminación lógica del producto.
     * Cambia el estado a inactivo sin eliminar el registro de la base de datos.
     *
     * @param id Identificador del producto a eliminar
     * @throws ResourceNotFoundException Si el producto no existe
     */
    @Override
    public void eliminar(Long id) {
        log.info("Eliminando (soft delete) producto con ID: {}", id);

        Producto producto = obtenerPorId(id);
        producto.setEstado(false);
        producto.setFechaActualizacion(LocalDateTime.now());

        productoRepository.save(producto);
        log.info("Producto eliminado exitosamente: {}", id);
    }

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id Identificador del producto
     * @return Producto encontrado
     * @throws ResourceNotFoundException Si el producto no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    /**
     * Obtiene un producto por su código único.
     *
     * @param codigo Código del producto
     * @return Producto encontrado
     * @throws ResourceNotFoundException Si el producto no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con código: " + codigo));
    }

    /**
     * Lista todos los productos con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarTodos(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    /**
     * Busca productos por nombre o descripción con paginación.
     *
     * @param texto Texto a buscar
     * @param pageable Configuración de paginación
     * @return Página de productos que coinciden con la búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscar(String texto, Pageable pageable) {
        List<Producto> productos = productoRepository
                .findByNombreOrDescripcionContainingIgnoreCase(texto)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    /**
     * Filtra productos por categoría con paginación.
     *
     * @param idCategoria ID de la categoría
     * @param pageable Configuración de paginación
     * @return Página de productos de la categoría
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorCategoria(Long idCategoria, Pageable pageable) {
        List<Producto> productos = productoRepository.findByIdCategoria(idCategoria)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    /**
     * Filtra productos por género con paginación.
     *
     * @param genero Género del producto
     * @param pageable Configuración de paginación
     * @return Página de productos del género especificado
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorGenero(Genero genero, Pageable pageable) {
        List<Producto> productos = productoRepository.findByGenero(genero.getValor())
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    /**
     * Filtra productos por marca con paginación.
     *
     * @param marca Marca del producto
     * @param pageable Configuración de paginación
     * @return Página de productos de la marca especificada
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorMarca(String marca, Pageable pageable) {
        List<Producto> productos = productoRepository.findByMarca(marca)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    /**
     * Filtra productos por rango de precio con paginación.
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param pageable Configuración de paginación
     * @return Página de productos dentro del rango de precio
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        List<Producto> productos = productoRepository.findByPrecioVentaBetween(
                        precioMin,
                        precioMax)
                .stream()
                .toList();

        return new PageImpl<>(productos, pageable, productos.size());
    }

    /**
     * Obtiene todos los productos con stock bajo o igual al stock mínimo.
     *
     * @return Lista de productos con stock bajo
     */
    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    /**
     * Obtiene todos los productos con stock en cero.
     *
     * @return Lista de productos agotados
     */
    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosAgotados() {
        return productoRepository.findProductosAgotados();
    }

    /**
     * Actualiza el stock de un producto sumando o restando la cantidad especificada.
     * Genera alertas automáticamente si el stock queda bajo o se agota.
     *
     * @param idProducto ID del producto
     * @param cantidad Cantidad a sumar (positivo) o restar (negativo)
     * @return Producto con stock actualizado
     * @throws ResourceNotFoundException Si el producto no existe
     * @throws StockInsuficienteException Si no hay stock suficiente para la operación
     */
    @Override
    public Producto actualizarStock(Long idProducto, Integer cantidad) {
        log.info("Actualizando stock del producto ID: {} en cantidad: {}", idProducto, cantidad);

        Producto producto = obtenerPorId(idProducto);
        int stockAnterior = producto.getStock();
        int nuevoStock = stockAnterior + cantidad;

        if (nuevoStock < 0) {
            throw new StockInsuficienteException(
                    Constants.ERR_STOCK_INSUFICIENTE + ". Stock actual: " + stockAnterior +
                            ", cantidad solicitada: " + Math.abs(cantidad)
            );
        }

        producto.setStock(nuevoStock);
        producto.setFechaActualizacion(LocalDateTime.now());

        Producto productoActualizado = productoRepository.save(producto);

        verificarYGenerarAlertas(productoActualizado);

        log.info("Stock actualizado. Anterior: {}, Nuevo: {}", stockAnterior, nuevoStock);

        return productoActualizado;
    }

    /**
     * Verifica si un producto tiene stock suficiente para una cantidad requerida.
     *
     * @param idProducto ID del producto
     * @param cantidadRequerida Cantidad a verificar
     * @return true si hay stock suficiente
     * @throws ResourceNotFoundException Si el producto no existe
     */
    @Override
    @Transactional(readOnly = true)
    public boolean verificarStock(Long idProducto, Integer cantidadRequerida) {
        Producto producto = obtenerPorId(idProducto);
        return producto.getStock() >= cantidadRequerida;
    }

    /**
     * Calcula el margen de ganancia de un producto en porcentaje.
     * Formula: ((precioVenta - precioCompra) / precioCompra) * 100
     *
     * @param idProducto ID del producto
     * @return Margen de ganancia en porcentaje
     * @throws ResourceNotFoundException Si el producto no existe
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularMargenGanancia(Long idProducto) {
        Producto producto = obtenerPorId(idProducto);
        return numberUtil.calcularMargenGanancia(producto.getPrecioVenta(), producto.getPrecioCompra());
    }

    /**
     * Verifica si ya existe un producto con el código especificado.
     *
     * @param codigo Código del producto a verificar
     * @return true si el código ya existe
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }

    /**
     * Valida todos los datos de un producto nuevo antes de crearlo.
     *
     * @param producto Datos del producto a validar
     * @throws ValidationException Si algún dato no es válido
     * @throws DuplicateResourceException Si el código ya existe
     */
    private void validarProductoNuevo(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new ValidationException("El código del producto es obligatorio");
        }

        if (existeCodigo(producto.getCodigo())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del producto es obligatorio");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new ValidationException("El stock debe ser mayor o igual a cero");
        }

        validarPrecios(producto.getPrecioCompra(), producto.getPrecioVenta());
    }

    /**
     * Valida que los precios de compra y venta sean correctos.
     * El precio de venta debe ser mayor a cero y mayor o igual al precio de compra.
     *
     * @param precioCompra Precio de compra del producto
     * @param precioVenta Precio de venta del producto
     * @throws ValidationException Si los precios no son válidos
     */
    private void validarPrecios(BigDecimal precioCompra, BigDecimal precioVenta) {
        if (precioVenta == null || numberUtil.esMenor(precioVenta, BigDecimal.ZERO)
                || numberUtil.sonIguales(precioVenta, BigDecimal.ZERO)) {
            throw new ValidationException("El precio de venta debe ser mayor a cero");
        }

        if (precioCompra != null && numberUtil.esMenor(precioCompra, BigDecimal.ZERO)) {
            throw new ValidationException("El precio de compra no puede ser negativo");
        }

        if (precioCompra != null && numberUtil.esMenor(precioVenta, precioCompra)) {
            throw new ValidationException("El precio de venta no puede ser menor al precio de compra");
        }
    }

    /**
     * Verifica el stock actual y genera alertas si es necesario.
     *
     * @param producto Producto a verificar
     */
    private void verificarYGenerarAlertas(Producto producto) {
        if (producto.getStock() <= producto.getStockMinimo()) {
            if (producto.getStock() == 0) {
                alertaService.generarAlertaStockAgotado(producto.getIdProducto());
            } else if (producto.getStock() <= Constants.STOCK_CRITICO) {
                alertaService.generarAlertaStockBajo(producto.getIdProducto());
            } else {
                alertaService.generarAlertaStockBajo(producto.getIdProducto());
            }
        }
    }
}