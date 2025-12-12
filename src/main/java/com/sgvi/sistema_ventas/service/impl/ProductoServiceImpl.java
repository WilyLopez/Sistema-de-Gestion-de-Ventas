package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.StockInsuficienteException;
import com.sgvi.sistema_ventas.exception.ValidationException;
import com.sgvi.sistema_ventas.model.dto.producto.ProductoDTO;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.enums.Genero;
import com.sgvi.sistema_ventas.repository.ProductoRepository;
import com.sgvi.sistema_ventas.service.interfaces.IAlertaService;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.NumberUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final IAlertaService alertaService;
    private final NumberUtil numberUtil;

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

    @Override
    public Producto actualizar(Long id, Producto producto) {
        log.info("Actualizando producto con ID: {}", id);
        Producto productoExistente = obtenerPorId(id);
        if (!productoExistente.getCodigo().equals(producto.getCodigo()) && existeCodigo(producto.getCodigo())) {
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
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MSG_RECURSO_NO_ENCONTRADO + " con código: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarTodos(Pageable pageable) {
        return productoRepository.findAllProjectedBy(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Producto> listarTodosOriginal(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscar(String texto, Long idCategoria, BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        Specification<Producto> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (texto != null && !texto.trim().isEmpty()) {
                String likePattern = "%" + texto.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("codigo")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("descripcion")), likePattern)
                ));
            }
            if (idCategoria != null) {
                predicates.add(criteriaBuilder.equal(root.get("idCategoria"), idCategoria));
            }
            if (precioMin != null && precioMax != null) {
                predicates.add(criteriaBuilder.between(root.get("precioVenta"), precioMin, precioMax));
            } else if (precioMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("precioVenta"), precioMin));
            } else if (precioMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("precioVenta"), precioMax));
            }
            predicates.add(criteriaBuilder.isTrue(root.get("estado")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return productoRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorCategoria(Long idCategoria, Pageable pageable) {
        List<Producto> productos = productoRepository.findByIdCategoria(idCategoria).stream().toList();
        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorGenero(Genero genero, Pageable pageable) {
        List<Producto> productos = productoRepository.findByGenero(genero.getValor()).stream().toList();
        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorMarca(String marca, Pageable pageable) {
        List<Producto> productos = productoRepository.findByMarca(marca).stream().toList();
        return new PageImpl<>(productos, pageable, productos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> filtrarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        List<Producto> productos = productoRepository.findByPrecioVentaBetween(precioMin, precioMax).stream().toList();
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
            throw new StockInsuficienteException(Constants.ERR_STOCK_INSUFICIENTE + ". Stock actual: " + stockAnterior + ", cantidad solicitada: " + Math.abs(cantidad));
        }
        producto.setStock(nuevoStock);
        producto.setFechaActualizacion(LocalDateTime.now());
        Producto productoActualizado = productoRepository.save(producto);
        verificarYGenerarAlertas(productoActualizado);
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
        return numberUtil.calcularMargenGanancia(producto.getPrecioVenta(), producto.getPrecioCompra());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }

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

    private void validarPrecios(BigDecimal precioCompra, BigDecimal precioVenta) {
        if (precioVenta == null || numberUtil.esMenor(precioVenta, BigDecimal.ZERO) || numberUtil.sonIguales(precioVenta, BigDecimal.ZERO)) {
            throw new ValidationException("El precio de venta debe ser mayor a cero");
        }
        if (precioCompra != null && numberUtil.esMenor(precioCompra, BigDecimal.ZERO)) {
            throw new ValidationException("El precio de compra no puede ser negativo");
        }
        if (precioCompra != null && numberUtil.esMenor(precioVenta, precioCompra)) {
            throw new ValidationException("El precio de venta no puede ser menor al precio de compra");
        }
    }

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