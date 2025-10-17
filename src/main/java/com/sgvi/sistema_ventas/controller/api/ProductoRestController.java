package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de productos.
 * RF-005: CRUD de Productos
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Endpoints para gestión de productos")
public class ProductoRestController {

    private final IProductoService productoService;

    /**
     * RF-005: Crear producto
     * POST /api/productos
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el sistema")
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        log.info("POST /api/productos - Crear producto: {}", producto.getCodigo());
        Producto productoCreado = productoService.crear(producto);
        return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);
    }

    /**
     * RF-005: Actualizar producto
     * PUT /api/productos/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto producto) {
        log.info("PUT /api/productos/{} - Actualizar producto", id);
        Producto productoActualizado = productoService.actualizar(id, producto);
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * RF-005: Eliminar producto (soft delete)
     * DELETE /api/productos/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar producto", description = "Desactiva un producto del sistema")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/productos/{} - Eliminar producto", id);
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-005: Obtener producto por ID
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto", description = "Obtiene un producto por su ID")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/productos/{} - Obtener producto", id);
        Producto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * RF-005: Listar todos los productos con paginación
     * GET /api/productos?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Listar productos", description = "Lista todos los productos con paginación")
    public ResponseEntity<Page<Producto>> listarTodos(Pageable pageable) {
        log.info("GET /api/productos - Listar productos (página: {})", pageable.getPageNumber());
        Page<Producto> productos = productoService.listarTodos(pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Buscar productos por texto
     * GET /api/productos/buscar?texto=camiseta
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos", description = "Busca productos por nombre o descripción")
    public ResponseEntity<Page<Producto>> buscar(
            @RequestParam String texto,
            Pageable pageable) {
        log.info("GET /api/productos/buscar?texto={}", texto);
        Page<Producto> productos = productoService.buscar(texto, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-011: Obtener productos con stock bajo
     * GET /api/productos/stock-bajo
     */
    @GetMapping("/stock-bajo")
    @Operation(summary = "Productos con stock bajo", description = "Lista productos con stock menor o igual al mínimo")
    public ResponseEntity<List<Producto>> obtenerStockBajo() {
        log.info("GET /api/productos/stock-bajo");
        List<Producto> productos = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-011: Obtener productos agotados
     * GET /api/productos/agotados
     */
    @GetMapping("/agotados")
    @Operation(summary = "Productos agotados", description = "Lista productos con stock = 0")
    public ResponseEntity<List<Producto>> obtenerAgotados() {
        log.info("GET /api/productos/agotados");
        List<Producto> productos = productoService.obtenerProductosAgotados();
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Obtener producto por código
     * GET /api/productos/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar por código", description = "Obtiene un producto por su código único")
    public ResponseEntity<Producto> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("GET /api/productos/codigo/{}", codigo);
        Producto producto = productoService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(producto);
    }
}
