package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.producto.ActualizarStockRequest;
import com.sgvi.sistema_ventas.model.dto.producto.ProductoCreateDTO;
import com.sgvi.sistema_ventas.model.dto.producto.ProductoUpdateDTO;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.enums.Genero;
import com.sgvi.sistema_ventas.service.interfaces.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de productos.
 * Implementa requisito RF-005 del SRS: CRUD de Productos.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Endpoints para gestión de productos de ropa")
public class ProductoRestController {

    private final IProductoService productoService;

    /**
     * RF-005: Crear nuevo producto.
     * Valida código único, precios y stock.
     *
     * @param createDTO DTO con datos del producto
     * @return Producto creado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Crear producto",
            description = "Crea un nuevo producto en el catálogo. Valida código único y precios"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o código duplicado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            )
    })
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoCreateDTO createDTO) {
        try {
            log.info("POST /api/productos - Crear producto: {}", createDTO.getCodigo());

            if (productoService.existeCodigo(createDTO.getCodigo())) {
                log.warn("Intento de crear producto con código duplicado: {}", createDTO.getCodigo());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El código de producto ya existe: " + createDTO.getCodigo()));
            }

            if (createDTO.getPrecioVenta().compareTo(createDTO.getPrecioCompra()) < 0) {
                log.warn("Precio de venta menor que precio de compra para producto: {}", createDTO.getCodigo());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El precio de venta no puede ser menor al precio de compra"));
            }

            Producto producto = convertirAProducto(createDTO);
            Producto productoCreado = productoService.crear(producto);

            log.info("Producto creado exitosamente: {} - ID: {}", productoCreado.getCodigo(), productoCreado.getIdProducto());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(productoCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear producto: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear producto: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear producto. Intente nuevamente"));
        }
    }

    /**
     * RF-005: Actualizar producto existente.
     * Permite actualización parcial de campos.
     *
     * @param id ID del producto
     * @param updateDTO DTO con datos a actualizar
     * @return Producto actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza datos de un producto existente. Permite actualización parcial"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateDTO updateDTO) {

        try {
            log.info("PUT /api/productos/{} - Actualizar producto", id);

            if (updateDTO.getPrecioVenta() != null && updateDTO.getPrecioCompra() != null) {
                if (updateDTO.getPrecioVenta().compareTo(updateDTO.getPrecioCompra()) < 0) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El precio de venta no puede ser menor al precio de compra"));
                }
            }

            Producto producto = productoService.obtenerPorId(id);
            aplicarActualizacion(producto, updateDTO);

            Producto productoActualizado = productoService.actualizar(id, producto);

            log.info("Producto actualizado exitosamente: {}", id);

            return ResponseEntity.ok(productoActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con ID: " + id));
        }
    }

    /**
     * RF-005: Eliminar producto (soft delete).
     * Marca el producto como inactivo sin eliminarlo físicamente.
     *
     * @param id ID del producto
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar producto",
            description = "Desactiva un producto del sistema (soft delete). Solo administradores"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/productos/{} - Eliminar producto", id);

            productoService.eliminar(id);

            log.info("Producto eliminado exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al eliminar producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con ID: " + id));
        }
    }

    /**
     * RF-005: Obtener producto por ID.
     *
     * @param id ID del producto
     * @return Producto encontrado
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Obtiene los datos completos de un producto"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/productos/{}", id);
            Producto producto = productoService.obtenerPorId(id);
            return ResponseEntity.ok(producto);

        } catch (Exception e) {
            log.error("Error al obtener producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con ID: " + id));
        }
    }

    /**
     * RF-005: Obtener producto por código único.
     *
     * @param codigo Código del producto
     * @return Producto encontrado
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(
            summary = "Buscar por código",
            description = "Obtiene un producto por su código único (SKU)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorCodigo(@PathVariable String codigo) {
        try {
            log.info("GET /api/productos/codigo/{}", codigo);
            Producto producto = productoService.obtenerPorCodigo(codigo);
            return ResponseEntity.ok(producto);

        } catch (Exception e) {
            log.error("Error al obtener producto por código {}: {}", codigo, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con código: " + codigo));
        }
    }

    /**
     * RF-005: Listar todos los productos con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de productos
     */
    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Lista todos los productos con paginación. Ejemplo: ?page=0&size=20&sort=nombre,asc"
    )
    public ResponseEntity<Page<Producto>> listarTodos(Pageable pageable) {
        log.info("GET /api/productos - Listar productos (página: {})", pageable.getPageNumber());
        Page<Producto> productos = productoService.listarTodos(pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Buscar productos por texto.
     * Busca en nombre, código y descripción.
     *
     * @param texto Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de productos que coinciden
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar productos",
            description = "Busca productos por nombre, código o descripción"
    )
    public ResponseEntity<Page<Producto>> buscar(
            @RequestParam String texto,
            Pageable pageable) {

        log.info("GET /api/productos/buscar?texto={}", texto);
        Page<Producto> productos = productoService.buscar(texto, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Filtrar productos por categoría.
     *
     * @param idCategoria ID de la categoría
     * @param pageable Parámetros de paginación
     * @return Página de productos de la categoría
     */
    @GetMapping("/categoria/{idCategoria}")
    @Operation(
            summary = "Filtrar por categoría",
            description = "Lista productos de una categoría específica"
    )
    public ResponseEntity<Page<Producto>> filtrarPorCategoria(
            @PathVariable Long idCategoria,
            Pageable pageable) {

        log.info("GET /api/productos/categoria/{}", idCategoria);
        Page<Producto> productos = productoService.filtrarPorCategoria(idCategoria, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Filtrar productos por género.
     *
     * @param genero Género del producto
     * @param pageable Parámetros de paginación
     * @return Página de productos del género
     */
    @GetMapping("/genero/{genero}")
    @Operation(
            summary = "Filtrar por género",
            description = "Lista productos de un género específico (HOMBRE, MUJER, UNISEX, NINO, NINA)"
    )
    public ResponseEntity<Page<Producto>> filtrarPorGenero(
            @PathVariable Genero genero,
            Pageable pageable) {

        log.info("GET /api/productos/genero/{}", genero);
        Page<Producto> productos = productoService.filtrarPorGenero(genero, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Filtrar productos por marca.
     *
     * @param marca Marca del producto
     * @param pageable Parámetros de paginación
     * @return Página de productos de la marca
     */
    @GetMapping("/marca/{marca}")
    @Operation(
            summary = "Filtrar por marca",
            description = "Lista productos de una marca específica"
    )
    public ResponseEntity<Page<Producto>> filtrarPorMarca(
            @PathVariable String marca,
            Pageable pageable) {

        log.info("GET /api/productos/marca/{}", marca);
        Page<Producto> productos = productoService.filtrarPorMarca(marca, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-005: Filtrar productos por rango de precio.
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param pageable Parámetros de paginación
     * @return Página de productos en el rango
     */
    @GetMapping("/precio")
    @Operation(
            summary = "Filtrar por rango de precio",
            description = "Lista productos dentro de un rango de precios"
    )
    public ResponseEntity<Page<Producto>> filtrarPorRangoPrecio(
            @RequestParam BigDecimal precioMin,
            @RequestParam BigDecimal precioMax,
            Pageable pageable) {

        log.info("GET /api/productos/precio?precioMin={}&precioMax={}", precioMin, precioMax);
        Page<Producto> productos = productoService.filtrarPorRangoPrecio(precioMin, precioMax, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-011: Obtener productos con stock bajo.
     * Lista productos con stock <= stock mínimo.
     *
     * @return Lista de productos con stock bajo
     */
    @GetMapping("/stock-bajo")
    @Operation(
            summary = "Productos con stock bajo",
            description = "Lista productos con stock menor o igual al stock mínimo (alerta)"
    )
    public ResponseEntity<List<Producto>> obtenerStockBajo() {
        log.info("GET /api/productos/stock-bajo");
        List<Producto> productos = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-011: Obtener productos agotados.
     * Lista productos con stock = 0.
     *
     * @return Lista de productos agotados
     */
    @GetMapping("/agotados")
    @Operation(
            summary = "Productos agotados",
            description = "Lista productos con stock en cero"
    )
    public ResponseEntity<List<Producto>> obtenerAgotados() {
        log.info("GET /api/productos/agotados");
        List<Producto> productos = productoService.obtenerProductosAgotados();
        return ResponseEntity.ok(productos);
    }

    /**
     * RF-012: Actualizar stock de producto manualmente.
     * Registra movimiento de inventario (entrada/salida/ajuste).
     *
     * @param id ID del producto
     * @param request DTO con cantidad, tipo de movimiento y motivo
     * @return Producto con stock actualizado
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Actualizar stock manualmente",
            description = "Actualiza el stock de un producto registrando movimiento de inventario. Solo administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cantidad inválida o stock insuficiente"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<?> actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarStockRequest request) {

        try {
            log.info("PATCH /api/productos/{}/stock - Tipo: {}, Cantidad: {}",
                    id, request.getTipoMovimiento(), request.getCantidad());

            Integer cantidadFinal = request.getCantidad();

            switch (request.getTipoMovimiento()) {
                case ENTRADA:
                    cantidadFinal = Math.abs(cantidadFinal);
                    break;
                case SALIDA:
                    cantidadFinal = -Math.abs(cantidadFinal);
                    break;
                case AJUSTE:
                    break;
            }

            Producto productoActualizado = productoService.actualizarStock(id, cantidadFinal);

            log.info("Stock actualizado exitosamente para producto {}: Nuevo stock = {}",
                    id, productoActualizado.getStock());

            return ResponseEntity.ok(productoActualizado);

        } catch (IllegalStateException e) {
            log.warn("Stock insuficiente para producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Cantidad inválida para producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar stock del producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con ID: " + id));
        }
    }

    /**
     * RF-012: Verificar disponibilidad de stock.
     *
     * @param id ID del producto
     * @param cantidad Cantidad requerida
     * @return Objeto con disponibilidad y stock actual
     */
    @GetMapping("/{id}/verificar-stock")
    @Operation(
            summary = "Verificar disponibilidad de stock",
            description = "Verifica si hay stock suficiente para una cantidad específica"
    )
    public ResponseEntity<?> verificarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {

        try {
            log.info("GET /api/productos/{}/verificar-stock?cantidad={}", id, cantidad);

            boolean disponible = productoService.verificarStock(id, cantidad);
            Producto producto = productoService.obtenerPorId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("disponible", disponible);
            response.put("stockActual", producto.getStock());
            response.put("cantidadSolicitada", cantidad);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al verificar stock del producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con ID: " + id));
        }
    }

    /**
     * Obtener margen de ganancia de un producto.
     *
     * @param id ID del producto
     * @return Margen de ganancia en porcentaje
     */
    @GetMapping("/{id}/margen-ganancia")
    @Operation(
            summary = "Calcular margen de ganancia",
            description = "Calcula el margen de ganancia del producto en porcentaje"
    )
    public ResponseEntity<?> calcularMargenGanancia(@PathVariable Long id) {
        try {
            log.info("GET /api/productos/{}/margen-ganancia", id);

            BigDecimal margen = productoService.calcularMargenGanancia(id);

            Map<String, Object> response = new HashMap<>();
            response.put("idProducto", id);
            response.put("margenGanancia", margen);
            response.put("porcentaje", margen.toString() + "%");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al calcular margen de ganancia del producto {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Producto no encontrado con ID: " + id));
        }
    }

    /**
     * Convierte ProductoCreateDTO a entidad Producto.
     *
     * @param createDTO DTO de creación
     * @return Entidad Producto
     */
    private Producto convertirAProducto(ProductoCreateDTO createDTO) {
        Producto producto = new Producto();
        producto.setCodigo(createDTO.getCodigo());
        producto.setNombre(createDTO.getNombre());
        producto.setMarca(createDTO.getMarca());
        producto.setTalla(createDTO.getTalla());
        producto.setColor(createDTO.getColor());
        producto.setMaterial(createDTO.getMaterial());
        producto.setGenero(createDTO.getGenero());
        producto.setPrecioCompra(createDTO.getPrecioCompra());
        producto.setPrecioVenta(createDTO.getPrecioVenta());
        producto.setStock(createDTO.getStock());
        producto.setStockMinimo(createDTO.getStockMinimo());
        producto.setDescripcion(createDTO.getDescripcion());
        producto.setImagenUrl(createDTO.getImagenUrl());
        producto.setIdCategoria(createDTO.getIdCategoria());
        producto.setIdProveedor(createDTO.getIdProveedor());
        return producto;
    }

    /**
     * Aplica actualización parcial de ProductoUpdateDTO a entidad Producto.
     *
     * @param producto Producto existente
     * @param updateDTO DTO con campos a actualizar
     */
    private void aplicarActualizacion(Producto producto, ProductoUpdateDTO updateDTO) {
        if (updateDTO.getNombre() != null) {
            producto.setNombre(updateDTO.getNombre());
        }
        if (updateDTO.getMarca() != null) {
            producto.setMarca(updateDTO.getMarca());
        }
        if (updateDTO.getTalla() != null) {
            producto.setTalla(updateDTO.getTalla());
        }
        if (updateDTO.getColor() != null) {
            producto.setColor(updateDTO.getColor());
        }
        if (updateDTO.getMaterial() != null) {
            producto.setMaterial(updateDTO.getMaterial());
        }
        if (updateDTO.getGenero() != null) {
            producto.setGenero(updateDTO.getGenero());
        }
        if (updateDTO.getPrecioCompra() != null) {
            producto.setPrecioCompra(updateDTO.getPrecioCompra());
        }
        if (updateDTO.getPrecioVenta() != null) {
            producto.setPrecioVenta(updateDTO.getPrecioVenta());
        }
        if (updateDTO.getStockMinimo() != null) {
            producto.setStockMinimo(updateDTO.getStockMinimo());
        }
        if (updateDTO.getDescripcion() != null) {
            producto.setDescripcion(updateDTO.getDescripcion());
        }
        if (updateDTO.getImagenUrl() != null) {
            producto.setImagenUrl(updateDTO.getImagenUrl());
        }
        if (updateDTO.getEstado() != null) {
            producto.setEstado(updateDTO.getEstado());
        }
        if (updateDTO.getIdCategoria() != null) {
            producto.setIdCategoria(updateDTO.getIdCategoria());
        }
        if (updateDTO.getIdProveedor() != null) {
            producto.setIdProveedor(updateDTO.getIdProveedor());
        }
    }
}