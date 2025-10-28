package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.dto.producto.CategoriaDTO;
import com.sgvi.sistema_ventas.model.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de categorías.
 * Define los contratos según RF-004: CRUD de Categorías
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface ICategoriaService {

    /**
     * RF-004: Crear una nueva categoría
     * @param categoria Categoría a crear
     * @return Categoría creada
     */
    Categoria crear(Categoria categoria);

    /**
     * RF-004: Actualizar categoría existente
     * @param id ID de la categoría
     * @param categoria Datos actualizados
     * @return Categoría actualizada
     */
    Categoria actualizar(Long id, Categoria categoria);

    /**
     * RF-004: Eliminar categoría
     * @param id ID de la categoría
     */
    void eliminar(Long id);

    /**
     * RF-004: Obtener categoría por ID
     * @param id ID de la categoría
     * @return Categoría encontrada
     */
    Categoria obtenerPorId(Long id);

    /**
     * RF-004: Listar todas las categorías
     * @param pageable Parámetros de paginación
     * @return Página de categorías
     */
    Page<Categoria> listarTodas(Pageable pageable);

    /**
     * RF-004: Listar categorías activas
     * @return Lista de categorías activas
     */
    List<Categoria> listarActivas();

    /**
     * RF-004: Buscar categorías por nombre
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de categorías
     */
    Page<Categoria> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * Verificar si categoría tiene productos asociados
     * @param id ID de la categoría
     * @return true si tiene productos
     */
    boolean tieneProductos(Long id);

    /**
     * Verificar si nombre ya existe
     * @param nombre Nombre a verificar
     * @return true si existe
     */
    boolean existeNombre(String nombre);

    List<CategoriaDTO> listarTodasConCantidad();
    List<CategoriaDTO> buscarPorNombreConCantidad(String nombre);
}
