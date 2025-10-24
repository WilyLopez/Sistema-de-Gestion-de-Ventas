package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Categoria;
import com.sgvi.sistema_ventas.service.interfaces.ICategoriaService;
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
 * Controller REST para gestión de categorías.
 * RF-004: CRUD de Categorías
 *
 * @author Jhamil Suarez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Endpoints para gestión de categorías")
public class CategoriaRestController {

    private final ICategoriaService categoriaService;

    /**
     * RF-004: Crear categoría
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría")
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria) {
        log.info("POST /api/categorias - Crear categoría");
        Categoria categoriaCreada = categoriaService.crear(categoria);
        return new ResponseEntity<>(categoriaCreada, HttpStatus.CREATED);
    }

    /**
     * RF-004: Actualizar categoría
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente")
    public ResponseEntity<Categoria> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {
        log.info("PUT /api/categorias/{}", id);
        Categoria categoriaActualizada = categoriaService.actualizar(id, categoria);
        return ResponseEntity.ok(categoriaActualizada);
    }

    /**
     * RF-004: Eliminar categoría
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/categorias/{}", id);
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-004: Obtener categoría por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría", description = "Obtiene una categoría por ID")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/categorias/{}", id);
        Categoria categoria = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(categoria);
    }

    /**
     * RF-004: Listar categorías
     */
    @GetMapping
    @Operation(summary = "Listar categorías", description = "Lista todas las categorías")
    public ResponseEntity<Page<Categoria>> listarTodas(Pageable pageable) {
        log.info("GET /api/categorias");
        Page<Categoria> categorias = categoriaService.listarTodas(pageable);
        return ResponseEntity.ok(categorias);
    }

    /**
     * RF-004: Listar categorías activas
     */
    @GetMapping("/activas")
    @Operation(summary = "Categorías activas", description = "Lista solo categorías activas")
    public ResponseEntity<List<Categoria>> listarActivas() {
        log.info("GET /api/categorias/activas");
        List<Categoria> categorias = categoriaService.listarActivas();
        return ResponseEntity.ok(categorias);
    }
}
