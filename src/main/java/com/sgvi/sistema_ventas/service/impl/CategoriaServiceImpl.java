package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.BusinessException;
import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.dto.producto.CategoriaDTO;
import com.sgvi.sistema_ventas.model.entity.Categoria;
import com.sgvi.sistema_ventas.repository.CategoriaRepository;
import com.sgvi.sistema_ventas.service.interfaces.ICategoriaService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de categorías.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoriaServiceImpl implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public Categoria crear(Categoria categoria) {
        log.info("Creando categoría: {}", categoria.getNombre());

        validarCategoriaNueva(categoria);

        categoria.setEstado(true);
        categoria.setFechaCreacion(LocalDateTime.now());
        categoria.setFechaActualizacion(LocalDateTime.now());

        Categoria categoriaCreada = categoriaRepository.save(categoria);
        log.info("Categoría creada exitosamente con ID: {}", categoriaCreada.getIdCategoria());

        return categoriaCreada;
    }

    @Override
    public Categoria actualizar(Long id, Categoria categoria) {
        log.info("Actualizando categoría con ID: {}", id);

        Categoria categoriaExistente = obtenerPorId(id);

        // Validar nombre único si cambió
        if (!categoriaExistente.getNombre().equals(categoria.getNombre())
                && existeNombre(categoria.getNombre())) {
            throw new DuplicateResourceException("El nombre de categoría ya existe: " + categoria.getNombre());
        }

        categoriaExistente.setNombre(categoria.getNombre());
        categoriaExistente.setDescripcion(categoria.getDescripcion());
        categoriaExistente.setFechaActualizacion(LocalDateTime.now());

        Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
        log.info("Categoría actualizada exitosamente: {}", id);

        return categoriaActualizada;
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando categoría con ID: {}", id);

        if (tieneProductos(id)) {
            throw new BusinessException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        Categoria categoria = obtenerPorId(id);
        categoria.setEstado(false);
        categoria.setFechaActualizacion(LocalDateTime.now());

        categoriaRepository.save(categoria);
        log.info("Categoría eliminada exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarTodas(Pageable pageable) {
        return categoriaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByEstado(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> buscarPorNombre(String nombre, Pageable pageable) {
        List<Categoria> categorias = categoriaRepository.findByNombreContainingIgnoreCase(nombre);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), categorias.size());
        return new PageImpl<>(categorias.subList(start, end), pageable, categorias.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneProductos(Long id) {
        Long count = categoriaRepository.countProductosActivosByCategoria(id);
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre) {
        return categoriaRepository.existsByNombre(nombre);
    }

    @Override
    public List<CategoriaDTO> listarTodasConCantidad() {
        return categoriaRepository.findAllWithProductCount();
    }

    @Override
    public List<CategoriaDTO> buscarPorNombreConCantidad(String nombre) {
        return categoriaRepository.findByNombreWithProductCount(nombre);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarCategoriaNueva(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre de la categoría es obligatorio");
        }

        if (existeNombre(categoria.getNombre())) {
            throw new DuplicateResourceException("El nombre de categoría ya existe: " + categoria.getNombre());
        }
    }

}