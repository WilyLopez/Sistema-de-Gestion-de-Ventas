package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.entity.Categoria;
import com.sgvi.sistema_ventas.service.interfaces.ICategoriaService;
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

import java.util.List;

/**
 * Controlador REST para gestión de categorías de productos.
 * Implementa requisito RF-004 del SRS: CRUD de Categorías.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Endpoints para gestión de categorías de productos")
public class CategoriaRestController {

    private final ICategoriaService categoriaService;

    /**
     * RF-004: Crear nueva categoría.
     * Valida que el nombre sea único.
     *
     * @param categoria Datos de la categoría
     * @return Categoría creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Crear categoría",
            description = "Crea una nueva categoría de productos. Valida nombre único. Solo administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada exitosamente",
                    content = @Content(schema = @Schema(implementation = Categoria.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o nombre duplicado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            )
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Categoria categoria) {
        try {
            log.info("POST /api/categorias - Crear categoría: {}", categoria.getNombre());

            if (categoriaService.existeNombre(categoria.getNombre())) {
                log.warn("Intento de crear categoría con nombre duplicado: {}", categoria.getNombre());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El nombre de categoría ya existe: " + categoria.getNombre()));
            }

            Categoria categoriaCreada = categoriaService.crear(categoria);

            log.info("Categoría creada exitosamente: {} - ID: {}",
                    categoriaCreada.getNombre(),
                    categoriaCreada.getIdCategoria());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(categoriaCreada);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear categoría: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear categoría: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear categoría. Intente nuevamente"));
        }
    }

    /**
     * RF-004: Actualizar categoría existente.
     * Valida nombre único si se modifica.
     *
     * @param id ID de la categoría
     * @param categoria Datos actualizados
     * @return Categoría actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza datos de una categoría existente. Solo administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o nombre duplicado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {

        try {
            log.info("PUT /api/categorias/{} - Actualizar categoría", id);

            Categoria categoriaExistente = categoriaService.obtenerPorId(id);

            if (!categoriaExistente.getNombre().equals(categoria.getNombre()) &&
                    categoriaService.existeNombre(categoria.getNombre())) {

                log.warn("Intento de actualizar con nombre duplicado: {}", categoria.getNombre());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El nombre de categoría ya existe: " + categoria.getNombre()));
            }

            Categoria categoriaActualizada = categoriaService.actualizar(id, categoria);

            log.info("Categoría actualizada exitosamente: {}", id);

            return ResponseEntity.ok(categoriaActualizada);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar categoría {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar categoría {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Categoría no encontrada con ID: " + id));
        }
    }

    /**
     * RF-004: Eliminar categoría (soft delete).
     * No permite eliminar si tiene productos asociados.
     *
     * @param id ID de la categoría
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar categoría",
            description = "Desactiva una categoría (soft delete). No permite eliminar si tiene productos asociados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede eliminar porque tiene productos asociados",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/categorias/{} - Eliminar categoría", id);

            if (categoriaService.tieneProductos(id)) {
                log.warn("Intento de eliminar categoría {} con productos asociados", id);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("No se puede eliminar la categoría porque tiene productos asociados"));
            }

            categoriaService.eliminar(id);

            log.info("Categoría eliminada exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al eliminar categoría {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Categoría no encontrada con ID: " + id));
        }
    }

    /**
     * RF-004: Obtener categoría por ID.
     *
     * @param id ID de la categoría
     * @return Categoría encontrada
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Obtiene los datos de una categoría específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/categorias/{}", id);
            Categoria categoria = categoriaService.obtenerPorId(id);
            return ResponseEntity.ok(categoria);

        } catch (Exception e) {
            log.error("Error al obtener categoría {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Categoría no encontrada con ID: " + id));
        }
    }

    /**
     * RF-004: Listar todas las categorías con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de categorías
     */
    @GetMapping
    @Operation(
            summary = "Listar categorías",
            description = "Lista todas las categorías con paginación. Ejemplo: ?page=0&size=20&sort=nombre,asc"
    )
    public ResponseEntity<Page<Categoria>> listarTodas(Pageable pageable) {
        log.info("GET /api/categorias - Listar categorías (página: {})", pageable.getPageNumber());
        Page<Categoria> categorias = categoriaService.listarTodas(pageable);
        return ResponseEntity.ok(categorias);
    }

    /**
     * RF-004: Listar solo categorías activas.
     * Útil para selects/dropdowns en formularios.
     *
     * @return Lista de categorías activas
     */
    @GetMapping("/activas")
    @Operation(
            summary = "Categorías activas",
            description = "Lista solo categorías activas ordenadas por nombre. Útil para formularios"
    )
    public ResponseEntity<List<Categoria>> listarActivas() {
        log.info("GET /api/categorias/activas");
        List<Categoria> categorias = categoriaService.listarActivas();
        return ResponseEntity.ok(categorias);
    }

    /**
     * RF-004: Buscar categorías por nombre.
     *
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de categorías que coinciden
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar categorías",
            description = "Busca categorías por nombre (búsqueda case-insensitive)"
    )
    public ResponseEntity<Page<Categoria>> buscarPorNombre(
            @RequestParam String nombre,
            Pageable pageable) {

        log.info("GET /api/categorias/buscar?nombre={}", nombre);
        Page<Categoria> categorias = categoriaService.buscarPorNombre(nombre, pageable);
        return ResponseEntity.ok(categorias);
    }

    /**
     * Verificar si una categoría tiene productos asociados.
     * Útil para validación en frontend antes de eliminar.
     *
     * @param id ID de la categoría
     * @return Objeto con booleano indicando si tiene productos
     */
    @GetMapping("/{id}/tiene-productos")
    @Operation(
            summary = "Verificar productos asociados",
            description = "Verifica si una categoría tiene productos asociados"
    )
    public ResponseEntity<?> tieneProductos(@PathVariable Long id) {
        try {
            log.info("GET /api/categorias/{}/tiene-productos", id);

            boolean tiene = categoriaService.tieneProductos(id);

            return ResponseEntity.ok(
                    java.util.Map.of(
                            "tieneProductos", tiene,
                            "mensaje", tiene ?
                                    "La categoría tiene productos asociados" :
                                    "La categoría no tiene productos asociados"
                    )
            );

        } catch (Exception e) {
            log.error("Error al verificar productos de categoría {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Categoría no encontrada con ID: " + id));
        }
    }

    /**
     * Verificar si un nombre de categoría ya existe.
     * Útil para validación en tiempo real en formularios.
     *
     * @param nombre Nombre a verificar
     * @return Objeto con booleano indicando si existe
     */
    @GetMapping("/existe-nombre")
    @Operation(
            summary = "Verificar nombre existente",
            description = "Verifica si un nombre de categoría ya está registrado"
    )
    public ResponseEntity<java.util.Map<String, Boolean>> existeNombre(@RequestParam String nombre) {
        log.info("GET /api/categorias/existe-nombre?nombre={}", nombre);
        boolean existe = categoriaService.existeNombre(nombre);
        return ResponseEntity.ok(java.util.Map.of("existe", existe));
    }
}