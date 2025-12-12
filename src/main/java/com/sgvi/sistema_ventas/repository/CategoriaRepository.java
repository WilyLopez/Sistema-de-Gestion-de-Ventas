package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.dto.producto.CategoriaDTO;
import com.sgvi.sistema_ventas.model.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Categoria en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas relacionadas con categorías.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su nombre.
     *
     * @param nombre el nombre de la categoría a buscar
     * @return Optional con la categoría encontrada o vacío si no existe
     */
    Optional<Categoria> findByNombre(String nombre);

    /**
     * Verifica si existe una categoría con el nombre especificado.
     *
     * @param nombre el nombre de la categoría a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca categorías por estado activo/inactivo.
     *
     * @param estado true para categorías activas, false para inactivas
     * @return lista de categorías que coinciden con el estado
     */
    List<Categoria> findByEstado(Boolean estado);

    /**
     * Busca categorías cuyos nombres contengan el texto especificado.
     *
     * @param nombre texto a buscar en los nombres de categorías
     * @return lista de categorías que coinciden con el criterio
     */
    @Query("SELECT c FROM Categoria c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Categoria> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Cuenta la cantidad de productos activos en una categoría.
     *
     * @param idCategoria el identificador de la categoría
     * @return número de productos activos en la categoría
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.idCategoria = :idCategoria AND p.estado = true")
    Long countProductosActivosByCategoria(@Param("idCategoria") Long idCategoria);

    @Query(value = """
           SELECT new com.sgvi.sistema_ventas.model.dto.producto.CategoriaDTO(
               c.idCategoria,
               c.nombre,
               c.descripcion,
               c.estado,
               c.fechaCreacion,
               COUNT(p)
           )
           FROM Categoria c
           LEFT JOIN Producto p ON p.idCategoria = c.idCategoria AND p.estado = true
           GROUP BY c.idCategoria, c.nombre, c.descripcion, c.estado, c.fechaCreacion
           """,
           countQuery = "SELECT COUNT(c) FROM Categoria c")
    Page<CategoriaDTO> findAllWithProductCount(Pageable pageable);

    // Busqueda con filtro por nombre opcional (ignore case)
    @Query("""
           SELECT new com.sgvi.sistema_ventas.model.dto.producto.CategoriaDTO(
               c.idCategoria,
               c.nombre,
               c.descripcion,
               c.estado,
               c.fechaCreacion,
               COUNT(p)
           )
           FROM Categoria c
           LEFT JOIN Producto p ON p.idCategoria = c.idCategoria AND p.estado = true
           WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
           GROUP BY c.idCategoria, c.nombre, c.descripcion, c.estado, c.fechaCreacion
           """)
    List<CategoriaDTO> findByNombreWithProductCount(@Param("nombre") String nombre);
}