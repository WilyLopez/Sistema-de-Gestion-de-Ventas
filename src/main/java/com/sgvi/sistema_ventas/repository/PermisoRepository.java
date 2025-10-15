package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Permiso en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas relacionadas con permisos.
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    /**
     * Busca un permiso por su nombre.
     *
     * @param nombre el nombre del permiso a buscar
     * @return Optional con el permiso encontrado o vacío si no existe
     */
    Optional<Permiso> findByNombre(String nombre);

    /**
     * Verifica si existe un permiso con el nombre especificado.
     *
     * @param nombre el nombre del permiso a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca permisos por módulo.
     *
     * @param modulo el módulo al que pertenecen los permisos
     * @return lista de permisos del módulo especificado
     */
    List<Permiso> findByModulo(String modulo);

    /**
     * Busca permisos cuyos nombres contengan el texto especificado.
     *
     * @param nombre texto a buscar en los nombres de permisos
     * @return lista de permisos que coinciden con el criterio
     */
    @Query("SELECT p FROM Permiso p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Permiso> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Obtiene la lista de módulos únicos existentes en los permisos.
     *
     * @return lista de nombres de módulos únicos
     */
    @Query("SELECT DISTINCT p.modulo FROM Permiso p ORDER BY p.modulo")
    List<String> findDistinctModulos();
}